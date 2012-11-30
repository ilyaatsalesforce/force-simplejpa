/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

/**
 * Thrown to indicate a problem obtaining entity information was returned in the response from the back-end server. The
 * request was successfully issued and the error is coming from the server.
 *
 * @author davidbuccola
 */
public class EntityResponseException extends RuntimeException {
    private static final long serialVersionUID = 1028782519413158664L;

    public EntityResponseException() {
        super();
    }

    public EntityResponseException(Throwable cause) {
        super(cause);
    }

    public EntityResponseException(String message) {
        super(message);
    }

    public EntityResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
