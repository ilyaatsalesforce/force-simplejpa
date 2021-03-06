/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link SimpleEntityManager} that is based on the JSON representations of the Salesforce REST
 * API.
 */
public final class RestSimpleEntityManager implements SimpleEntityManager {
    private static final Logger log = LoggerFactory.getLogger(RestSimpleEntityManager.class);

    static final String SHARING_SPECIFICATION_HEADER_NAME = "Work-Sharing-Specification";
    static final String SHARING_SPECIFICATION_ATTRIBUTE_NAME = "sharingSpecification";

    // Just one mapping context is shared by all instances. It is thread-safe and configured the same every time. There
    // is no reason to go through the expense of creating multiple instances. This way we get to share the cache.
    private static final EntityMappingContext mappingContext = new EntityMappingContext();

    private RestConnector connector;

    /**
     * Constructs a new instance with the given {@link RestConnector}.
     *
     * @param connector a REST connector
     */
    public RestSimpleEntityManager(RestConnector connector) {
        this.connector = connector;
    }

    @Override
    public void persist(Object entity) {
        Validate.notNull(entity, "entity must not be null");

        EntityDescriptor descriptor = getRequiredEntityDescriptor(entity.getClass());
        if (descriptor.hasIdMember() && StringUtils.isNotEmpty(EntityUtils.getEntityId(descriptor, entity))) {
            throw new EntityRequestException("Id value should not exist for new object creation");
        }

        String json = convertToJsonForPersist(entity);

        optionallyLogRequest("Persist", descriptor.getName(), null, json);

        InputStream responseStream = connector.doCreate(descriptor.getName(), json, buildHeaders(descriptor, entity));
        JsonNode responseNode = parseJsonResponse(responseStream);
        if (!responseNode.has("success") || !responseNode.has("id")) {
            throw new EntityResponseException("JSON response is missing expected fields");
        }
        if (!responseNode.get("success").getBooleanValue()) {
            throw new EntityResponseException(getErrorsText(responseNode));
        }
        String id = responseNode.get("id").getTextValue();
        if (descriptor.hasIdMember()) {
            EntityUtils.setEntityId(descriptor, entity, id);
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("...Created %s %s", descriptor.getName(), id));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T merge(T entity) {
        Validate.notNull(entity, "entity must not be null");

        EntityDescriptor descriptor = getRequiredEntityDescriptor(entity.getClass());
        String id = getRequiredId(descriptor, entity);
        String json = convertToJsonForMerge(entity);

        optionallyLogRequest("Merge", descriptor.getName(), id, json);

        connector.doUpdate(descriptor.getName(), id, json, buildHeaders(descriptor, entity));

        if (log.isDebugEnabled()) {
            log.debug(String.format("...Updated %s %s", descriptor.getName(), id));
        }

        return entity;
    }

    @Override
    public void remove(Object entity) {
        Validate.notNull(entity, "entity must not be null");

        EntityDescriptor descriptor = getRequiredEntityDescriptor(entity.getClass());
        String id = getRequiredId(descriptor, entity);

        optionallyLogRequest("Remove", descriptor.getName(), id, null);

        connector.doDelete(descriptor.getName(), id, buildHeaders(descriptor, entity));

        if (log.isDebugEnabled()) {
            log.debug(String.format("...Deleted %s %s", descriptor.getName(), id));
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        Validate.notNull(entityClass, "entityClass must not be null");
        Validate.notNull(primaryKey, "primaryKey must not be null");

        EntityDescriptor descriptor = getRequiredEntityDescriptor(entityClass);

        optionallyLogRequest("Find", descriptor.getName(), primaryKey.toString(), null);

        return find(descriptor, entityClass, primaryKey);
    }

    private <T> T find(EntityDescriptor descriptor, Class<T> entityClass, Object primaryKey) {
        String soqlTemplate = String.format("SELECT * FROM %s WHERE Id = '%s'", descriptor.getName(), primaryKey);
        try {
            return createQuery(descriptor, soqlTemplate, entityClass).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public <T> SimpleTypedQuery<T> createQuery(final String soqlTemplate, final Class<T> entityClass) {
        Validate.notNull(soqlTemplate, "soqlTemplate must not be null");
        Validate.notNull(entityClass, "entityClass must not be null");

        final EntityDescriptor descriptor = getRequiredEntityDescriptor(entityClass);

        optionallyLogRequest("CreateQuery", descriptor.getName(), null, soqlTemplate);

        return createQuery(descriptor, soqlTemplate, entityClass);
    }

    private <T> SimpleTypedQuery<T> createQuery(final EntityDescriptor descriptor, final String soqlTemplate, final Class<T> entityClass) {
        return new RestSimpleTypedQuery<T>(descriptor, soqlTemplate, entityClass);
    }

    private EntityDescriptor getRequiredEntityDescriptor(Class<?> clazz) {
        EntityDescriptor descriptor = mappingContext.getEntityDescriptor(clazz);
        if (descriptor == null) {
            throw new IllegalArgumentException(
                String.format("%s can't be used as an entity, probably because it isn't annotated", clazz.getName()));
        }
        return descriptor;
    }

    private static String getRequiredId(EntityDescriptor descriptor, Object entity) {
        if (descriptor.hasIdMember()) {
            String id = EntityUtils.getEntityId(descriptor.getIdProperty(), entity);
            if (StringUtils.isEmpty(id)) {
                throw new EntityRequestException("Entity instance does not have an id value set");
            }
            return id;
        } else {
            throw new EntityRequestException("Entity class is not annotated with an Id member");
        }
    }

    private ObjectMapper getObjectMapper() {
        return mappingContext.getObjectMapper();
    }

    private String convertToJsonForPersist(Object entity) {
        try {
            return getObjectMapper().writerWithView(SerializationViews.Persist.class).writeValueAsString(entity);
        } catch (IOException e) {
            throw new EntityResponseException("Failed to encode entity as JSON", e);
        }
    }

    private String convertToJsonForMerge(Object entity) {
        try {
            return getObjectMapper().writerWithView(SerializationViews.Merge.class).writeValueAsString(entity);
        } catch (IOException e) {
            throw new EntityResponseException("Failed to encode entity as JSON", e);
        }
    }

    private JsonNode parseJsonResponse(InputStream inputStream) {
        try {
            return getObjectMapper().readTree(inputStream);
        } catch (IOException e) {
            throw new EntityResponseException("Failed to parse JSON response stream", e);
        }
    }

    /**
     * Build up any entity-specific headers that we want to attach to the outbound REST request to Salesforce.
     * <p/>
     * There is currently just one of these. It is a special Salesforce internal attribute that specifies sharing
     * conditions.
     *
     * @param descriptor descriptor of the entity
     * @param entity     the entity instance
     * @return entity-specific headers
     */
    private static Map<String, String> buildHeaders(EntityDescriptor descriptor, Object entity) {
        if (entity == null) {
            return null; // There are no headers defined yet that make sense without an entity
        }

        Map<String, String> headers = null;
        if (descriptor.hasAttributesMember()) {
            Map<String, String> attributes = EntityUtils.getAttributes(descriptor, entity);
            if (attributes != null) {
                String sharingSpecification = attributes.get(SHARING_SPECIFICATION_ATTRIBUTE_NAME);
                if (sharingSpecification != null) {
                    headers = new HashMap<String, String>();
                    headers.put(SHARING_SPECIFICATION_HEADER_NAME, sharingSpecification);
                }
            }
        }

        if (headers != null && log.isDebugEnabled()) {
            log.debug(String.format("...With Headers: %s", headers.toString()));
        }

        return headers;
    }

    private static void optionallyLogRequest(String operation, String entityTypeName, String id, String detail) {
        if (log.isDebugEnabled()) {
            if (id != null) {
                log.debug(String.format("%s %s %s: %s", operation, entityTypeName, id, StringUtils.trimToEmpty(detail)));
            } else {
                log.debug(String.format("%s %s: %s", operation, entityTypeName, StringUtils.trimToNull(detail)));
            }
        }
    }

    private static String getErrorsText(JsonNode node) {
        if (node.has("errors")) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode error : node.get("errors")) {
                if (sb.length() > 0)
                    sb.append("; ");
                sb.append(error.getTextValue());
            }
            if (sb.length() > 0)
                return sb.toString();
        }
        return "Salesforce persistence error with no message";
    }

    private final class RestSimpleTypedQuery<T> extends AbstractSimpleTypedQuery<T> {
        private EntityDescriptor descriptor;
        private Class<T> entityClass;
        private String soqlTemplate;

        private RestSimpleTypedQuery(EntityDescriptor descriptor, String soqlTemplate, Class<T> entityClass) {
            this.descriptor = descriptor;
            this.entityClass = entityClass;
            this.soqlTemplate = soqlTemplate;
        }

        @Override
        public List<T> getResultList() {
            return getResultList(entityClass);
        }

        @Override
        public <R> List<R> getResultList(Class<R> resultClass) {
            List<R> results = new ArrayList<R>();
            try {
                String soql = new SoqlBuilder(descriptor)
                    .soqlTemplate(soqlTemplate)
                    .offset(getFirstResult())
                    .limit(getMaxResults())
                    .build();

                if (log.isDebugEnabled())
                    log.debug(String.format("...Query: %s", soql));

                // Issue the query and parse the first batch of results.
                InputStream responseStream = connector.doQuery(soql, buildHeaders(descriptor, null));
                JsonNode rootNode = getObjectMapper().readTree(responseStream);
                for (JsonNode node : rootNode.get("records")) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("...Result Row: %s", node.toString()));
                    }
                    if (resultClass.equals(JsonNode.class)) {
                        results.add((resultClass.cast(node)));
                    } else {
                        results.add(getObjectMapper().readValue(node, resultClass));
                    }
                }

                // Request additional results if they exist
                while (rootNode.get("nextRecordsUrl") != null) {
                    URI nextRecordsUrl = URI.create(rootNode.get("nextRecordsUrl").getTextValue());
                    responseStream = connector.doGet(nextRecordsUrl, buildHeaders(descriptor, null));
                    rootNode = getObjectMapper().readTree(responseStream);
                    for (JsonNode node : rootNode.get("records")) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("...Result Row: %s", node.toString()));
                        }
                        if (resultClass.equals(JsonNode.class)) {
                            results.add((resultClass.cast(node)));
                        } else {
                            results.add(getObjectMapper().readValue(node, resultClass));
                        }
                    }
                }
            } catch (IOException e) {
                throw new EntityResponseException("Failed to parse the 'query' result", e);
            }
            return results;
        }
    }
}
