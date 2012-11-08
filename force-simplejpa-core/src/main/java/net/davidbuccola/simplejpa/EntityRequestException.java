/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

/**
 * Thrown to indicate a problem issuing the request for entity information. The request probably did not make it to the
 * server or failed so severely that the server couldn't even begin to process it.
 *
 * @author davidbuccola
 */
public class EntityRequestException extends RuntimeException {
    private static final long serialVersionUID = 6311549209416962878L;

    public EntityRequestException() {
        super();
    }

    public EntityRequestException(Throwable cause) {
        super(cause);
    }

    public EntityRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
