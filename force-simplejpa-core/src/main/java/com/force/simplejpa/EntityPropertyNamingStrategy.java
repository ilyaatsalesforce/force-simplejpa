/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMember;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;

import static com.force.simplejpa.IntrospectionUtils.getEntityNamespace;
import static com.force.simplejpa.IntrospectionUtils.isMissingNamespace;
import static com.force.simplejpa.IntrospectionUtils.isNotStandardProperty;
import static com.force.simplejpa.IntrospectionUtils.isPropertyOfCustomEntity;
import static com.force.simplejpa.IntrospectionUtils.isRelationshipField;

/**
 * A property naming strategy which helps with subtleties of naming objects in Salesforce. The strategy tries to help
 * with the ugliness of namespaces and the "__c" suffix. It also aids with read/write differences for references to
 * related objects.
 * <p/>
 * See the descriptions below for more details on the transformations.
 *
 * @author davidbuccola
 */
class EntityPropertyNamingStrategy extends PropertyNamingStrategy {
    private boolean forSerialization;

    EntityPropertyNamingStrategy(boolean forSerialization) {
        this.forSerialization = forSerialization;
    }

    @Override
    public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
        return translate(field, defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(method, defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        return translate(method, defaultName);
    }

    @Override
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam,
                                              String defaultName) {
        return translate(ctorParam, defaultName);
    }

    /**
     * Translate the property name if necessary.
     *
     * @param member       The annotated member for the property
     * @param propertyName The default property name
     *
     * @return the translated property name
     *
     * @see #translateCustomPropertyName(org.codehaus.jackson.map.introspect.AnnotatedMember, String)
     * @see #translateStandardPropertyName(org.codehaus.jackson.map.introspect.AnnotatedMember, String)
     */
    protected String translate(AnnotatedMember member, String propertyName) {
        if (isPropertyOfCustomEntity(member) && isNotStandardProperty(propertyName)) {
            return translateCustomPropertyName(member, propertyName);
        } else {
            return translateStandardPropertyName(member, propertyName);
        }
    }

    /**
     * Translate a property name for a custom field by patterning if after the name of the enclosing custom entity. Make
     * sure to adjust for the special handling of relationship fields. For serialization of relationships we use the
     * "xxx__c" field name while for deserialization we use the "xxx__r" relationship name.
     *
     * @param member       The annotated member for the property
     * @param propertyName The default property name
     *
     * @return the translated property name
     */
    protected String translateCustomPropertyName(AnnotatedMember member, String propertyName) {
        String propertyNameSansSuffix = propertyName;
        if (propertyName.endsWith("__c") || propertyName.endsWith("__r"))
            propertyNameSansSuffix = propertyName.substring(0, propertyName.length() - 3);

        String namespace = getEntityNamespace(member);
        if (namespace != null && isMissingNamespace(propertyNameSansSuffix))
            propertyNameSansSuffix = namespace + "__" + propertyNameSansSuffix;

        if (forSerialization) {
            return propertyNameSansSuffix + "__c";
        } else {
            return propertyNameSansSuffix + (isRelationshipField(member) ? "__r" : "__c");
        }
    }

    /**
     * Translate a property name for a standard field.
     * <p/>
     * For simple fields there is nothing to do. For relationship fields we adjust based on whether or not we are
     * serializing. For serialization of relationships we use the field name (xxxId) while for deserialization we use
     * the relationship name (which should be the given raw property name).
     *
     * @param member       The annotated member for the property
     * @param propertyName The default property name
     *
     * @return the translated property name
     */
    protected String translateStandardPropertyName(AnnotatedMember member, String propertyName) {
        if (isRelationshipField(member)) {
            String propertyNameSansId = propertyName;
            if (propertyName.endsWith("Id"))
                propertyNameSansId = propertyName.substring(0, propertyName.length() - 2);

            return forSerialization ? (propertyNameSansId + "Id") : propertyNameSansId;
        }
        return propertyName;
    }
}
