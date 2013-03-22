/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;

import java.util.HashMap;
import java.util.Map;

/**
 * Extra metadata about an entity above and beyond that normally managed by Jackson that is useful for persistence
 * operations. The information is collected at the same time as the core Jackson metadata (introspection time) and
 * references some of the standard Jackson classes.
 */
final class EntityDescriptor {

    private final String name;
    private final BeanPropertyDefinition idProperty;
    private final BeanPropertyDefinition attributesProperty;
    private final BasicBeanDescription beanDescription;
    private final Map<String, EntityDescriptor> relatedEntities;

    EntityDescriptor(String name, BasicBeanDescription beanDescription, BeanPropertyDefinition idProperty, BeanPropertyDefinition attributesProperty) {
        this.name = name;
        this.beanDescription = beanDescription;
        this.idProperty = idProperty;
        this.attributesProperty = attributesProperty;
        this.relatedEntities = new HashMap<String, EntityDescriptor>();
    }

    public String getName() {
        return name;
    }

    public BeanPropertyDefinition getIdProperty() {
        return idProperty;
    }

    public BeanPropertyDefinition getAttributesProperty() {
        return attributesProperty;
    }

    public boolean hasIdMember() {
        return getIdProperty() != null;
    }

    public boolean hasAttributesMember() {
        return getAttributesProperty() != null;
    }

    public BasicBeanDescription getBeanDescription() {
        return beanDescription;
    }

    public Map<String, EntityDescriptor> getRelatedEntities() {
        return relatedEntities;
    }
}
