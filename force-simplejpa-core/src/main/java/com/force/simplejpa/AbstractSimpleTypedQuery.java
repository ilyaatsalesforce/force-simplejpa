/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

/**
 * An abstract implementation of {@link SimpleTypedQuery} that handles most of the standard stuff so that derived
 * classes just need to worry about implementing {@link com.force.simplejpa.SimpleTypedQuery#getResultList()}.
 *
 * @param <T> the class of returned values
 */
abstract class AbstractSimpleTypedQuery<T> implements SimpleTypedQuery<T> {
    private int maxResults;
    private int startPosition;

    @Override
    public SimpleTypedQuery<T> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    protected int getMaxResults() {
        return maxResults;
    }

    @Override
    public SimpleTypedQuery<T> setFirstResult(int startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    protected int getFirstResult() {
        return startPosition;
    }

    @Override
    public T getSingleResult() {
        List<T> results = getResultList();
        if (results.size() == 1)
            return results.get(0);
        else if (results.size() > 1)
            throw new NonUniqueResultException();
        else
            throw new NoResultException();
    }
}
