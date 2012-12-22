/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

/**
 * Thrown to indicate a problem issuing the request for entity information. The request probably did not make it to the
 * server or failed so severely that the server couldn't even begin to process it.
 *
 * @author davidbuccola
 */
public class EntityRequestException extends RuntimeException {
    private static final long serialVersionUID = 6311549209416962878L;

    /**
     * Constructs a new instance with <code>null</code> as the detail message.
     */
    public EntityRequestException() {
        super();
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public EntityRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified cause and a detail message of <tt>(cause==null ? null :
     * cause.toString())</tt>. This constructor is useful for exceptions that are little more than wrappers for other
     * throwables.
     *
     * @param cause the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public EntityRequestException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new instance with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the cause. <tt>null</tt> is permitted, and indicates that the cause is nonexistent or unknown.
     */
    public EntityRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
