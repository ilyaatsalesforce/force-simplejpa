/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.type.JavaType;

import javax.persistence.Id;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for mapping annotated persistent entities to and from the JSON representations of the Salesforce generic REST
 * API.
 * <p/>
 * This context includes the basic Jackson {@link ObjectMapper} configured appropriately for serialization and
 * deserialization as well as extra metadata in the form of {@link EntityDescriptor} for use in making other advanced
 * choices related to entity persistence.
 */
public final class EntityMappingContext {
    private final ObjectMapper objectMapper;
    private final Map<Class<?>, EntityDescriptor> descriptors = new ConcurrentHashMap<Class<?>, EntityDescriptor>();
    private final Map<Class<?>, EntityDescriptor> incompleteDescriptors = new HashMap<Class<?>, EntityDescriptor>();

    /**
     * Constructs a new instance.
     */
    public EntityMappingContext() {
        objectMapper = new ObjectMapper();
        objectMapper.setDeserializerProvider(new StdDeserializerProvider(new SubqueryDeserializerFactory()));
        objectMapper.setSerializationConfig(
            objectMapper.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .withPropertyNamingStrategy(new EntityPropertyNamingStrategy(true))
                .withAnnotationIntrospector(new SimpleJpaAnnotationIntrospector(this)));
        objectMapper.setDeserializationConfig(
            objectMapper.getDeserializationConfig()
                .withPropertyNamingStrategy(new EntityPropertyNamingStrategy(false))
                .withAnnotationIntrospector(new SimpleJpaAnnotationIntrospector(this)));
        objectMapper.setVisibilityChecker(
            objectMapper.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
	    objectMapper.disable(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Gets the {@link ObjectMapper} appropriate for serializing and deserializing the persistent entities for use with
     * the generic Salesforce REST API.
     *
     * @return an object mapper
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Gets the {@link EntityDescriptor} for the specified class.
     * <p/>
     * If the class has already been seen then an existing cached descriptor is returned. Otherwise the class is
     * introspected to build a new descriptor and the new descriptor cached for future use.
     *
     * @param clazz the class for which an entity descriptor is desired
     * @return the entity descriptor
     */
    public EntityDescriptor getEntityDescriptor(Class<?> clazz) {
        EntityDescriptor descriptor = descriptors.get(clazz);
        if (descriptor != null)
            return descriptor;

        if (clazz.isPrimitive() || isIntrinsicJavaPackage(clazz.getPackage()) || isJodaTimePackage(clazz.getPackage()))
            return null; // Primitive types can't be entities and therefore have no descriptors.

        if (clazz.isEnum())
            return null; // Enums can't be entities

        return createEntityDescriptor(clazz);
    }

    private EntityDescriptor createEntityDescriptor(Class<?> clazz) {
        synchronized (incompleteDescriptors) { // Just one thread can create at a time. Creation doesn't happen often.

            // If we already have something under construction then return it. (Comes into play for recursive calls
            // when resolving related entities).
            if (incompleteDescriptors.containsKey(clazz))
                return incompleteDescriptors.get(clazz);

            boolean recursiveCall = incompleteDescriptors.size() > 0;
            try {
                JavaType type = objectMapper.getTypeFactory().constructType(clazz);
                BasicBeanDescription beanDescription = objectMapper.getDeserializationConfig().introspect(type);
                EntityDescriptor entityDescriptor =
                    new EntityDescriptor(
                        getEntityName(beanDescription), beanDescription,
                        getIdProperty(beanDescription), getAttributesProperty(beanDescription));
                incompleteDescriptors.put(clazz, entityDescriptor);

                // Resolve related entity descriptions recursively
                for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                    EntityDescriptor relatedEntityDescriptor = getEntityDescriptor(getPropertyClass(property));
                    if (relatedEntityDescriptor != null)
                        entityDescriptor.getRelatedEntities().put(property.getInternalName(), relatedEntityDescriptor);
                }

                if (!recursiveCall)
                    for (Class<?> key : incompleteDescriptors.keySet())
                        descriptors.put(key, incompleteDescriptors.get(key));

                return entityDescriptor;

            } finally {
                if (!recursiveCall)
                    incompleteDescriptors.clear();
            }
        }
    }

    private String getEntityName(BasicBeanDescription beanDescription) {
        AnnotationIntrospector introspector = objectMapper.getDeserializationConfig().getAnnotationIntrospector();
        String name = introspector.findTypeName(beanDescription.getClassInfo());
        if (!StringUtils.isEmpty(name))
            return name;

        return beanDescription.getClassInfo().getRawType().getSimpleName();
    }

    private static BeanPropertyDefinition getIdProperty(BasicBeanDescription beanDescription) {
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            // Try setter first. Setters takes precedence over fields.
            if (property.hasSetter()) {
                if (property.getSetter().hasAnnotation(Id.class)) {
                    return property;
                }
            }
            if (property.hasField()) {
                if (property.getField().hasAnnotation(Id.class)) {
                    return property;
                }
            }
        }
        return null;
    }

    private static BeanPropertyDefinition getAttributesProperty(BasicBeanDescription beanDescription) {
        for (BeanPropertyDefinition property : beanDescription.findProperties()) {
            if (property.getName().equals("attributes")) {
                if (Map.class.isAssignableFrom(getPropertyClass(property))) {
                    return property;
                }
            }
        }
        return null;
    }

    private static boolean isIntrinsicJavaPackage(Package aPackage) {
        return (aPackage != null) && (aPackage.getName().startsWith("java."));
    }

    private static boolean isJodaTimePackage(Package aPackage) {
        return (aPackage != null) && (aPackage.getName().startsWith("org.joda.time"));
    }

    /**
     * Gets the class of the related entity. If the class of the property is an array or {@link Collection} then it is
     * the class of the contained elements that is relevant. Otherwise the property class is the one we want.
     *
     * @param property the property definition
     * @return the class of the property or, if the property is an array or collection, the class of the elements
     */
    private static Class<?> getPropertyClass(BeanPropertyDefinition property) {
        if (property.hasSetter()) {
            return getPropertyClass(property.getSetter().getParameterType(0));
        } else if (property.hasField()) {
            return getPropertyClass(property.getField().getGenericType());
        } else
            throw new IllegalArgumentException("I don't know how to deal with that kind of property definition");
    }

    /**
     * Gets the class of the related entity. If the class of the property is an array or {@link Collection} then it is
     * the class of the contained elements that is relevant. Otherwise the property class is the one we want.
     *
     * @param propertyType the type of the property
     * @return the class of the property or, if the property is an array or collection, the class of the elements
     */
    private static Class<?> getPropertyClass(Type propertyType) {
        if (propertyType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) propertyType;
            if (Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                return getPropertyClass(parameterizedType.getActualTypeArguments()[0]); // Return the element class
            } else {
                return getPropertyClass(parameterizedType.getRawType());
            }

        } else if (propertyType instanceof Class) {
            Class<?> propertyClass = (Class<?>) propertyType;
            if (propertyClass.isArray()) {
                return propertyClass.getComponentType(); // Return the element class
            } else {
                return propertyClass;
            }
        } else {
            throw new IllegalArgumentException("I don't know how to deal with that kind of property definition");
        }
    }
}
