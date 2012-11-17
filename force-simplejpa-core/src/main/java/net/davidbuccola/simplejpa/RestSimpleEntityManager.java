/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link SimpleEntityManager} that is based on the JSON representations of the Salesforce REST
 * API.
 *
 * @author davidbuccola
 */
public final class RestSimpleEntityManager implements SimpleEntityManager {
    private static final Logger log = LoggerFactory.getLogger(RestSimpleEntityManager.class);

    // Configure the object mapper and descriptor provider. Note that the object mapper and descriptor provider are
    // intertwined and this makes initialization order important. We first create an empty object mapper and a
    // corresponding descriptor provider. After these two are wired together we can go ahead and configure the object
    // mapper details.
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final EntityDescriptorProvider descriptorProvider = new EntityDescriptorProvider(objectMapper);

    static {
        objectMapper.setDeserializerProvider(new StdDeserializerProvider(new SubqueryDeserializerFactory()));
        objectMapper.withModule(new MapAsStringModule());
        objectMapper.setSerializationConfig(
            objectMapper.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .withPropertyNamingStrategy(new EntityPropertyNamingStrategy(true))
                .withAnnotationIntrospector(new SimpleJpaAnnotationIntrospector(descriptorProvider)));
        objectMapper.setDeserializationConfig(
            objectMapper.getDeserializationConfig()
                .withPropertyNamingStrategy(new EntityPropertyNamingStrategy(false))
                .withAnnotationIntrospector(new SimpleJpaAnnotationIntrospector(descriptorProvider)));
        objectMapper.setVisibilityChecker(
            objectMapper.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    private RestConnector connector;

    public RestSimpleEntityManager(RestConnector connector) {
        this.connector = connector;
    }

    @Override
    public void persist(Object entity) {
        if (entity == null)
            throw new NullPointerException("entity");

        EntityDescriptor descriptor = descriptorProvider.get(entity.getClass());
        if (descriptor == null)
            throw new IllegalArgumentException("entity can't be used as an entity");

        try {
            if (descriptor.hasIdMember())
                setEntityId(descriptor.getIdProperty(), entity, null);

            byte[] jsonBytes = objectMapper.writeValueAsBytes(entity);

            if (log.isDebugEnabled())
                log.debug(String.format("Create: %s: %s", descriptor.getName(), new String(jsonBytes, "UTF-8")));

            InputStream responseStream = connector.doCreate(descriptor.getName(), jsonBytes);
            JsonNode responseNode = objectMapper.readTree(responseStream);
            if (!responseNode.has("success") || !responseNode.has("id"))
                throw new EntityResponseException("JSON database response is missing expected fields");

            if (!responseNode.get("success").getBooleanValue())
                throw new EntityResponseException(getErrorsText(responseNode));

            if (descriptor.hasIdMember()) {
                String id = responseNode.get("id").getTextValue();
                setEntityId(descriptor.getIdProperty(), entity, id);
            }
        } catch (IOException e) {
            throw new EntityResponseException("Failed to parse JSON database response", e);
        }
    }

    @Override
    public <T> T merge(T entity) {
        if (entity == null)
            throw new NullPointerException("entity");

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void remove(Object entity) {
        if (entity == null)
            throw new NullPointerException("entity");

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        if (entityClass == null)
            throw new NullPointerException("entityClass");

        if (primaryKey == null)
            throw new NullPointerException("primaryKey");

        EntityDescriptor descriptor = descriptorProvider.get(entityClass);
        if (descriptor == null)
            throw new IllegalArgumentException("entityClass can't be used as an entity");

        String soqlTemplate = String.format("SELECT * FROM %s WHERE Id = '%s'", descriptor.getName(), primaryKey);
        try {
            return createQuery(soqlTemplate, entityClass).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public final <T> SimpleTypedQuery<T> createQuery(final String soqlTemplate, final Class<T> entityClass) {

        if (soqlTemplate == null)
            throw new NullPointerException("soqlTemplate");

        if (entityClass == null)
            throw new NullPointerException("entityClass");

        final EntityDescriptor descriptor = descriptorProvider.get(entityClass);
        if (descriptor == null)
            throw new IllegalArgumentException("entityClass can't be used as an entity");

        return new AbstractSimpleTypedQuery<T>() {
            @Override
            public List<T> getResultList() {
                List<T> results = new ArrayList<T>();
                try {
                    String soql = new SoqlBuilder(descriptor)
                        .soqlTemplate(soqlTemplate)
                        .offset(getFirstResult())
                        .limit(getMaxResults())
                        .build();

                    if (log.isDebugEnabled())
                        log.debug(String.format("Query: %s", soql));

                    // Issue the query and parse the first batch of results.
                    InputStream responseStream = connector.doQuery(soql);
                    JsonNode rootNode = objectMapper.readTree(responseStream);
                    for (JsonNode node : rootNode.get("records")) {
                        results.add(objectMapper.readValue(node, entityClass));
                    }

                    // Request additional results if they exist
                    while (rootNode.get("nextRecordsUrl") != null) {
                        responseStream = connector.doGet(URI.create(rootNode.get("nextRecordsUrl").getTextValue()));
                        rootNode = objectMapper.readTree(responseStream);
                        for (JsonNode node : rootNode.get("records")) {
                            results.add(objectMapper.readValue(node, entityClass));
                        }
                    }
                } catch (IOException e) {
                    throw new EntityResponseException("Failed to parse the 'query' result", e);
                }
                return results;
            }
        };
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
        return "database error with no error message";
    }

    /**
     * Sets the ID property of an entity instance.
     *
     * @param idProperty definition of the idProperty
     * @param instance   the entity instance on which to set the id
     * @param value      the id
     */
    private static void setEntityId(BeanPropertyDefinition idProperty, Object instance, String value) {
        if (idProperty.hasSetter())
            idProperty.getSetter().setValue(instance, value);
        else if (idProperty.hasField())
            idProperty.getField().setValue(instance, value);
        else
            throw new IllegalStateException("There is no way to set the entity id");
    }
}
