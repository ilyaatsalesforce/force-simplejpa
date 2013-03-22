/*
 * Copyright, 2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.junit.Test;

import java.io.Serializable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EntityResponseExceptionTest {
    private static final String MESSAGE = "Exception Message";
    private static final String CAUSE_MESSAGE = "Cause Message";

    @Test
    public void testDerivation() {
        assertThat(new EntityResponseException(), isA(RuntimeException.class));
        assertThat(new EntityResponseException(), isA(Serializable.class));
    }

    @Test
    public void testDefaultConstructor() {
        EntityResponseException exception = new EntityResponseException();
        assertThat(exception.getCause(), is(nullValue()));
        assertThat(exception.getMessage(), is(nullValue()));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName())));
    }

    @Test
    public void testCauseConstructor() {
        RuntimeException cause = new RuntimeException(CAUSE_MESSAGE);

        EntityResponseException exception = new EntityResponseException(cause);
        assertThat(exception.getCause(), is(sameInstance((Throwable) cause)));
        assertThat(exception.getMessage(), is(equalTo(cause.toString())));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName() + ": " + cause.toString())));
    }

    @Test
    public void testMessageConstructor() {
        EntityResponseException exception = new EntityResponseException(MESSAGE);
        assertThat(exception.getCause(), is(nullValue()));
        assertThat(exception.getMessage(), is(equalTo(MESSAGE)));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName() + ": " + MESSAGE)));
    }

    @Test
    public void testMessageAndCauseConstructor() {
        RuntimeException cause = new RuntimeException(CAUSE_MESSAGE);

        EntityResponseException exception = new EntityResponseException(MESSAGE, cause);
        assertThat(exception.getCause(), is(sameInstance((Throwable) cause)));
        assertThat(exception.getMessage(), is(equalTo(MESSAGE)));
        assertThat(exception.toString(), is(equalTo(exception.getClass().getName() + ": " + MESSAGE)));
    }
}
