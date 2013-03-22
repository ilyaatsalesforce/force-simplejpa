/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.codehaus.jackson.map.BeanPropertyDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Utilities for working with entity instances.
 */
public final class EntityUtils {
    private EntityUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Gets the attributes property of an entity instance.
     *
     * @param descriptor descriptor of the entity
     * @param instance   the entity instance from which to get the id
     * @return the attributes
     */
    public static Map<String, String> getAttributes(EntityDescriptor descriptor, Object instance) {
        if (descriptor.hasAttributesMember()) {
            return getAttributes(descriptor.getAttributesProperty(), instance);
        } else {
            throw new IllegalArgumentException("The entity does not have an attributes member");
        }
    }

    /**
     * Gets the attributes property of an entity instance.
     *
     * @param attributesProperty definition of the attributesProperty
     * @param instance           the entity instance from which to get the id
     * @return the attributes
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getAttributes(BeanPropertyDefinition attributesProperty, Object instance) {
        try {
            Object attributes;
            if (attributesProperty.hasGetter()) {
                attributes = attributesProperty.getGetter().getAnnotated().invoke(instance);
            } else if (attributesProperty.hasField()) {
                Field field = attributesProperty.getField().getAnnotated();
                field.setAccessible(true);
                attributes = field.get(instance);
            } else {
                throw new IllegalStateException("There is no way to get the entity attributes");
            }
            return (Map<String, String>) attributes;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the ID property of an entity instance.
     *
     * @param descriptor descriptor of the entity
     * @param instance   the entity instance from which to get the id
     * @return the id
     */
    public static String getEntityId(EntityDescriptor descriptor, Object instance) {
        if (descriptor.hasIdMember()) {
            return getEntityId(descriptor.getIdProperty(), instance);
        } else {
            throw new IllegalArgumentException("The entity does not have an id member");
        }
    }

    /**
     * Gets the ID property of an entity instance.
     *
     * @param idProperty definition of the idProperty
     * @param instance   the entity instance from which to get the id
     * @return the id
     */
    public static String getEntityId(BeanPropertyDefinition idProperty, Object instance) {
        try {
            Object id;
            if (idProperty.hasGetter()) {
                id = idProperty.getGetter().getAnnotated().invoke(instance);
            } else if (idProperty.hasField()) {
                Field field = idProperty.getField().getAnnotated();
                field.setAccessible(true);
                id = field.get(instance);
            } else {
                throw new IllegalStateException("There is no way to get the entity id");
            }
            return id == null ? null : id.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the ID property of an entity instance.
     *
     * @param descriptor descriptor of the entity
     * @param instance   the entity instance on which to set the id
     * @param value      the id
     */
    public static void setEntityId(EntityDescriptor descriptor, Object instance, String value) {
        if (descriptor.hasIdMember()) {
            setEntityId(descriptor.getIdProperty(), instance, value);
        } else {
            throw new IllegalArgumentException("The entity does not have an id member");
        }
    }

    /**
     * Sets the ID property of an entity instance.
     *
     * @param idProperty definition of the idProperty
     * @param instance   the entity instance on which to set the id
     * @param value      the id
     */
    public static void setEntityId(BeanPropertyDefinition idProperty, Object instance, String value) {
        if (idProperty.hasSetter()) {
            idProperty.getSetter().setValue(instance, value);
        } else if (idProperty.hasField()) {
            Field field = idProperty.getField().getAnnotated();
            field.setAccessible(true);
            idProperty.getField().setValue(instance, value);
        } else
            throw new IllegalArgumentException("There is no way to set the entity id");
    }
}
