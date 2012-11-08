/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import java.io.InputStream;
import java.net.URI;

/**
 * A connector which knows how to issue requests to the Salesforce REST API.
 * <p/>
 * This is a simple internal abstraction that allows different REST libraries to be plugged in. This means libraries
 * like Apache HTTP or Sun's Jersey, or some other library.
 *
 * @author davidbuccola
 */
public interface RestConnector {
    InputStream doGet(URI uri);

    InputStream doCreate(String entityType, byte[] body);

    InputStream doQuery(String soql);
}
