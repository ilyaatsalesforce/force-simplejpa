/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import java.lang.reflect.InvocationTargetException;

import org.codehaus.jackson.map.BeanPropertyDefinition;

/**
 * Utilities for working with entity instances.
 *
 * @author dbuccola
 */
public final class EntityUtils {
    private EntityUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Gets the ID property of an entity instance.
     *
     * @param descriptor descriptor of the entity
     * @param instance   the entity instance from which to get the id
     *
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
     *
     * @return the id
     */
    public static String getEntityId(BeanPropertyDefinition idProperty, Object instance) {
        try {
            Object id;
            if (idProperty.hasGetter()) {
                id = idProperty.getGetter().getAnnotated().invoke(instance);
            } else if (idProperty.hasField()) {
                id = idProperty.getField().getAnnotated().get(instance);
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
        if (idProperty.hasSetter())
            idProperty.getSetter().setValue(instance, value);
        else if (idProperty.hasField())
            idProperty.getField().setValue(instance, value);
        else
            throw new IllegalArgumentException("There is no way to set the entity id");
    }
}
