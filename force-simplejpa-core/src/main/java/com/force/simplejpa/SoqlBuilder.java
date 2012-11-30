/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.jackson.map.BeanPropertyDefinition;

/**
 * Builder for generating SOQL to retrieve a particular type of entity. The type of entity for which SOQL is desired is
 * specified in the constructor for the builder.
 * <p/>
 * This builder transforms an SOQL template by performing wildcard substitution and optionally adding pagination
 * information (in the form of LIMIT and OFFSET clauses).
 * <p/>
 * The SOQL template can contain wildcard specifications. The wildcard specifications are replaced with a concrete list
 * of fields from the associated entityDescriptor. Wildcard specifications can look like this:
 * <pre>
 * [prefix]*
 * </pre>
 * For example, the following are valid wildcard specifications:
 * <pre>
 * *
 * Modules.*
 * </pre>
 *
 * @author davidbuccola
 */
final class SoqlBuilder {
    private static final int DEFAULT_DEPTH = 5;
    private static final Pattern SPLIT_AT_LITERAL_PATTERN = Pattern.compile("([^\'\"]+)(.*)");
    private static final Pattern WILDCARD_PATTERN = Pattern.compile("([^\\*\\s]*?)\\*(\\{(\\w*)\\})?");

    private static final Map<CacheKey, String> cachedWildcardSubstitutions = new ConcurrentHashMap<CacheKey, String>();

    private EntityDescriptor rootDescriptor;
    private String soqlTemplate;
    private int offset = 0;
    private int limit = 0;
    private int depth = DEFAULT_DEPTH;

    public SoqlBuilder(EntityDescriptor rootDescriptor) {
        this.rootDescriptor = rootDescriptor;
    }

    public SoqlBuilder soqlTemplate(String soqlTemplate) {
        Validate.notEmpty(soqlTemplate, "No soqlTemplate was specified");
        this.soqlTemplate = soqlTemplate;
        return this;
    }

    public SoqlBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    public SoqlBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SoqlBuilder depth(int depth) {
        this.depth = depth;
        return this;
    }

    public String build() {
        StringBuilder sb = replaceFieldWildcards(soqlTemplate);
        if (limit > 0)
            sb.append(" LIMIT ").append(limit);
        if (offset > 0)
            sb.append(" OFFSET ").append(offset);
        return sb.toString();
    }

    private StringBuilder replaceFieldWildcards(String soqlTemplate) {

        // Use a simple algorithm to make our job easy (remember this is simple JPA, not full JPA). Quoted literals
        // are a headache in Regex and we really don't want to bite off full SOQL parsing at this point. Simplify by
        // assuming that all of our wildcard substitutions will show up before any literals are encountered. Parts of
        // the SOQL that show up after the first literal should not need substitution. With this simplifying
        // assumption we can split the line up into two parts: the front part in which we perform substitutions
        // and the back part (which contains literals) and which we leave alone.
        Matcher splitMatcher = SPLIT_AT_LITERAL_PATTERN.matcher(soqlTemplate);
        splitMatcher.find();
        String partToScanForWildcards = splitMatcher.group(1);
        String partToLeaveAlone = splitMatcher.group(2);

        // Replace wildcards with concrete field lists.
        int lastEnd = 0;
        StringBuilder sb = new StringBuilder();
        Matcher wildcardMatcher = WILDCARD_PATTERN.matcher(partToScanForWildcards);
        while (wildcardMatcher.find()) {
            sb.append(partToScanForWildcards.substring(lastEnd, wildcardMatcher.start()));
            String prefix = wildcardMatcher.group(1);
            String entityName = wildcardMatcher.group(3);
            sb.append(getWildcardSubstitution(getEntityDescriptor(entityName), prefix, depth));
            lastEnd = wildcardMatcher.end();
        }
        sb.append(partToScanForWildcards.substring(lastEnd));
        sb.append(partToLeaveAlone);
        return sb;
    }

    private EntityDescriptor getEntityDescriptor(String entityName) {
        EntityDescriptor descriptor = getEntityDescriptor(rootDescriptor, entityName);
        if (descriptor != null)
            return descriptor;

        throw new IllegalArgumentException(
            String.format("Wildcard substitution for '%s' cannot be resolved", entityName));
    }

    private static EntityDescriptor getEntityDescriptor(EntityDescriptor candidateDescriptor, String entityName) {
        if (StringUtils.isEmpty(entityName) || candidateDescriptor.getName().equals(entityName))
            return candidateDescriptor;

        // Search through the related entities for a match.
        for (EntityDescriptor relatedDescriptor : candidateDescriptor.getRelatedEntities().values()) {
            EntityDescriptor descriptor = getEntityDescriptor(relatedDescriptor, entityName);
            if (descriptor != null)
                return descriptor;
        }
        return null; // Nothing found
    }

    private static String getWildcardSubstitution(EntityDescriptor descriptor, String prefix, int depth) {
        CacheKey cacheKey = new CacheKey(descriptor, prefix);
        String substitution = cachedWildcardSubstitutions.get(cacheKey);
        if (substitution != null)
            return substitution;

        substitution = StringUtils.join(getFields(descriptor, prefix, depth), ',');
        cachedWildcardSubstitutions.put(cacheKey, substitution);
        return substitution;
    }

    private static List<String> getFields(EntityDescriptor descriptor, String prefix, int depth) {
        List<String> fields = new ArrayList<String>();
        for (BeanPropertyDefinition property : descriptor.getBeanDescription().findProperties()) {
            String prefixedFieldName = prefix + property.getName();
            EntityDescriptor relatedDescriptor = descriptor.getRelatedEntities().get(property.getInternalName());
            if (relatedDescriptor != null) {
                if (depth > 0) {
                    if (isArrayOrCollection(property)) {
                        fields.add(getSubquery(relatedDescriptor, prefixedFieldName, depth - 1));
                    } else {
                        fields.addAll(getFields(relatedDescriptor, prefixedFieldName + ".", depth - 1));
                    }
                }
            } else {
                fields.add(prefixedFieldName);
            }
        }
        return fields;
    }

    private static boolean isArrayOrCollection(BeanPropertyDefinition property) {
        Type type = property.getAccessor().getGenericType();
        if (type instanceof Class && ((Class<?>) type).isArray()) {
            return true;
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class && Collection.class.isAssignableFrom((Class<?>) rawType))
                return true;
        }
        return false;
    }

    private static String getSubquery(EntityDescriptor descriptor, String fieldName, int depth) {
        return new SoqlBuilder(descriptor)
            .soqlTemplate("(SELECT * from " + fieldName + ")")
            .depth(depth)
            .build();
    }

    private static final class CacheKey {
        private final EntityDescriptor descriptor;
        private final String prefix;

        CacheKey(EntityDescriptor descriptor, String prefix) {
            this.descriptor = descriptor;
            this.prefix = prefix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (descriptor != null ? !descriptor.equals(cacheKey.descriptor) : cacheKey.descriptor != null)
                return false;
            if (prefix != null ? !prefix.equals(cacheKey.prefix) : cacheKey.prefix != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = descriptor != null ? descriptor.hashCode() : 0;
            result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
            return result;
        }
    }
}
