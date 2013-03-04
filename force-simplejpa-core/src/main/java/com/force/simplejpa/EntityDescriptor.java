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
 * Extra metadata about an entity that is used for certain advanced SimpleJPPA operations.
 * <p/>
 * This extra metadata is closely related to the metadata maintained by Jackson. The information is collected at the
 * same time as the core Jackson metadata (introspection time) and references some of the standard Jackson classes.
 *
 * @author dbuccola
 */
class EntityDescriptor {
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
