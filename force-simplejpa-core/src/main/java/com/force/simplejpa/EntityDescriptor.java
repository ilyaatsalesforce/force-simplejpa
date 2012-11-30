/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;

/**
 * Extra metadata about an entity that is used for certain advanced SimpleJPPA operations.
 * <p/>
 * This extra metadata is closely related to the metadata maintained by Jackson. The information is collected at the
 * same time as the core Jackson metadata (introspection time) and references some of the standard Jackson classes.
 *
 * @author davidbuccola
 */
class EntityDescriptor {
    private final String name;
    private final BeanPropertyDefinition idProperty;
    private final BasicBeanDescription beanDescription;
    private final Map<String, EntityDescriptor> relatedEntities;

    EntityDescriptor(String name, BasicBeanDescription beanDescription, BeanPropertyDefinition idProperty) {
        this.name = name;
        this.idProperty = idProperty;
        this.beanDescription = beanDescription;
        this.relatedEntities = new HashMap<String, EntityDescriptor>();
    }

    public String getName() {
        return name;
    }

    public BeanPropertyDefinition getIdProperty() {
        return idProperty;
    }

    public boolean hasIdMember() {
        return getIdProperty() != null;
    }

    public BasicBeanDescription getBeanDescription() {
        return beanDescription;
    }

    public Map<String, EntityDescriptor> getRelatedEntities() {
        return relatedEntities;
    }
}
