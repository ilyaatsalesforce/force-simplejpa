/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

/**
 * @author davidbuccola
 */
public interface SimpleEntityManager {
    void persist(Object entity);

    <T> T merge(T entity);

    void remove(Object entity);

    <T> T find(Class<T> entityClass, Object primaryKey);

    <T> SimpleTypedQuery<T> createQuery(String qualification, Class<T> resultClass);
}
