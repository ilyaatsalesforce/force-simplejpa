/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * A connector which knows how to issue requests to the Salesforce "data" REST API.
 * <p/>
 * This is a simple internal abstraction that allows different REST libraries to be plugged in. This means libraries
 * like Sun's Jersey or Apache HTTP.
 * <p/>
 * The methods on this interface correspond to the kinds of thing you want to do with the Salesforce REST api, not the
 * kinds of generic things you want to do with REST. In other words, this interface is not intended to be some general
 * purpose facade on top of HTTP libraries. It is, instead, very specific to the Salesforce REST task at hand.
 */
public interface RestConnector {
    /**
     * Creates a new Salesforce object.
     *
     * @param entityType the Salesforce object type
     * @param jsonBody   the JSON encoded body for the creation request. See Salesforce REST documentation for more
     *                   details on the format.
     * @param headers    optional HTTP headers to add to the request.
     * @return input stream for the response body returned by Salesforce.
     */
    InputStream doCreate(String entityType, String jsonBody, Map<String, String> headers);

    /**
     * Issues a GET request to an arbitrary Salesforce REST URI, usually for the purpose of picking up subsequent
     * batches of a paged query result.
     *
     * @param uri     the URI
     * @param headers optional HTTP headers to add to the request.
     * @return input stream for the response body returned by Salesforce.
     */
    InputStream doGet(URI uri, Map<String, String> headers);

    /**
     * Issues a Salesforce SOQL query.
     *
     * @param soql    the SOQL for the query
     * @param headers optional HTTP headers to add to the request.
     * @return input stream for the response body returned by Salesforce.
     */
    InputStream doQuery(String soql, Map<String, String> headers);

    /**
     * Updates an existing Salesforce object.
     *
     * @param entityType the Salesforce object type
     * @param id         the Salesforce ID of the object
     * @param jsonBody   the JSON encoded body for the update request. See Salesforce REST documentation for more
     *                   details on the format.
     * @param headers    optional HTTP headers to add to the request.
     */
    void doUpdate(String entityType, String id, String jsonBody, Map<String, String> headers);

    /**
     * Deletes an existing Salesforce object.
     *
     * @param entityType the Salesforce object type
     * @param id         the Salesforce ID of the object
     * @param headers    optional HTTP headers to add to the request.
     */
    void doDelete(String entityType, String id, Map<String, String> headers);
}
