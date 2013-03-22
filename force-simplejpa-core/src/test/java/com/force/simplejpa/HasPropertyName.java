/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Does the {@link BeanPropertyDefinition} have the specified name?
 */
class HasPropertyName<T extends BeanPropertyDefinition> extends TypeSafeMatcher<T> {
    private final String expectedName;

    HasPropertyName(String expectedName) {
        this.expectedName = expectedName;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return item.getName().equals(expectedName);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expectedName);
    }

    @Factory
    public static <T extends BeanPropertyDefinition> Matcher<T> hasPropertyName(String propertyName) {
        return new HasPropertyName<T>(propertyName);
    }
}
