/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import java.util.List;

/**
 * A simple JPA-like interface for controlling the execution of SOQL queries.
 *
 * @param <T> type of object returned by the query
 */
public interface SimpleTypedQuery<T> {
    /**
     * Execute a SOQL query and return the list of objects satisfying the query.
     *
     * @return the list of objects satisfying the query
     */
    List<T> getResultList();

    /**
     * Execute a SOQL query and return the list of objects satisfying the query.
     *
     * @param resultClass the class of the returned object
     * @param <R>         the class of the returned object
     * @return the list of objects satisfying the query
     */
    <R> List<R> getResultList(Class<R> resultClass);

    /**
     * Execute a SOQL query and return the single object satisfying the query. If more than one object satisfies the
     * query then an exception is thrown.
     *
     * @return the object satisfying the query
     */
    T getSingleResult();

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param maxResult the maximum number of results to retrieve
     * @return the same query instance
     */
    SimpleTypedQuery<T> setMaxResults(int maxResult);

    /**
     * Sets the position of the first result to retrieve.
     *
     * @param startPosition the position of the first result to retrieve
     * @return the same query instance
     */
    SimpleTypedQuery<T> setFirstResult(int startPosition);
}
