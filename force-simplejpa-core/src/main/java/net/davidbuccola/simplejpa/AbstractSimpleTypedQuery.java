/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import java.util.List;

/**
 * @author davidbuccola
 */
abstract class AbstractSimpleTypedQuery<T> implements SimpleTypedQuery<T> {
    private int maxResults;
    private int startPosition;

    @Override
    public SimpleTypedQuery<T> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public SimpleTypedQuery<T> setFirstResult(int startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    @Override
    public int getFirstResult() {
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
