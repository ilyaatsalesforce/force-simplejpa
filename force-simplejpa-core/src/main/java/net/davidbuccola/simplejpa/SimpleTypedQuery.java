/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import java.util.List;

/**
 * @author davidbuccola
 */
public interface SimpleTypedQuery<T> {
    List<T> getResultList();

    T getSingleResult();

    SimpleTypedQuery<T> setMaxResults(int maxResult);

    int getMaxResults();

    SimpleTypedQuery<T> setFirstResult(int startPosition);

    int getFirstResult();
}
