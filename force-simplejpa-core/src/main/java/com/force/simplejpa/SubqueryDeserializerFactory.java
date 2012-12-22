/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializerFactory;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.deser.BeanDeserializerFactory;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;

/**
 * A special {@link DeserializerFactory} that knows how to create collection and array deserialiers which can deal with
 * the Salesforce REST representation for SOQL subqueries.
 * <p/>
 * This factory is mostly a standard {@link DeserializerFactory} with the exception that collection and array
 * deserializers are decorated with {@link SubqueryDeserializer} to help deal with the extra information returned with
 * SOQL subquery results.
 *
 * @author davidbuccola
 * @see SubqueryDeserializer
 */
class SubqueryDeserializerFactory extends BeanDeserializerFactory {
    public SubqueryDeserializerFactory() {
        this(null);
    }

    public SubqueryDeserializerFactory(Config config) {
        super(config);
    }

    @Override
    public DeserializerFactory withConfig(Config config) {
        return (getConfig() == config) ? this : new SubqueryDeserializerFactory(config);
    }

    @Override
    public JsonDeserializer<?> createCollectionDeserializer(DeserializationConfig config, DeserializerProvider p, CollectionType type, BeanProperty property) throws JsonMappingException {
        return new SubqueryDeserializer(super.createCollectionDeserializer(config, p, type, property));
    }

    @Override
    public JsonDeserializer<?> createArrayDeserializer(DeserializationConfig config, DeserializerProvider p, ArrayType type, BeanProperty property) throws JsonMappingException {
        return new SubqueryDeserializer(super.createArrayDeserializer(config, p, type, property));
    }

    @Override
    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationConfig config, DeserializerProvider p, CollectionLikeType type, BeanProperty property) throws JsonMappingException {
        return new SubqueryDeserializer(super.createCollectionLikeDeserializer(config, p, type, property));
    }
}
