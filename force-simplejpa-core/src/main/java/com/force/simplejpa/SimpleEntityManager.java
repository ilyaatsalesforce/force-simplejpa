/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

/**
 * A simple JPA-like entity manager for interacting with Salesforce object persistence.
 */
public interface SimpleEntityManager {
    /**
     * Make an entity persistent. Also known as "create".
     *
     * @param entity the entity to persist
     */
    void persist(Object entity);

    /**
     * Merge changes into an existing persisted entity. Also known as "update"
     *
     * @param entity the entity containing new values to be persisted. This entity can be sparsely populated but must at
     *               least contain a value for the ID field.
     * @param <T>    a class annotated with JPA persistence annotations
     *
     * @return the input entity
     */
    <T> T merge(T entity);

    /**
     * Remove a persisted entity. Also known as "delete".
     *
     * @param entity the entity to remove. The only value that needs to be populated is the ID field.
     */
    void remove(Object entity);

    /**
     * Find a persisted entity by primary key. Also known as "get".
     *
     * @param entityClass the class of the entity
     * @param primaryKey  the primary key (Salesforce ID)
     * @param <T>         a class annotated with JPA persistence annotations
     *
     * @return the entity
     */
    <T> T find(Class<T> entityClass, Object primaryKey);

    /**
     * Creates an instance of {@link SimpleTypedQuery} for executing a query based on SOQL.
     *
     * @param qualification a template for the SOQL query. The template is processed before execution to arrive at the
     *                      final SOQL query. The processing involves replacing occurrences of wildcards (*) with actual
     *                      field names as described by the JPA annotations.
     * @param resultClass   the class of the result entities
     * @param <T>           a class annotated with JPA persistence annotations
     *
     * @return a {@link SimpleTypedQuery} which can be executed
     */
    <T> SimpleTypedQuery<T> createQuery(String qualification, Class<T> resultClass);
}
