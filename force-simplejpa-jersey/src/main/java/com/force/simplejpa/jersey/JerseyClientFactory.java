/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;

/**
 * A factory for instances of {@link Client} configured appropriately for SimpleEntityManager use.
 * <p/>
 * This factory creates instances of {@link ApacheHttpClient4} because the Apache client is needed to support the HTTP
 * "PATCH" request required by the Salesforce API. If you decide not to use this factory and instead provide a client
 * instance of your own then make sure that the client instance can support HTTP "PATCH".
 * <p/>
 * By default, the returned instances use a {@link ThreadSafeClientConnManager} in order to support multi-threaded use.
 *
 * @author dbuccola
 */
public class JerseyClientFactory {
    /**
     * Creates a new instance of {@link Client} configured appropriately for SimpleEntityManager use.
     *
     * @return a Client
     */
    public Client newInstance() {
        return newInstance(new DefaultApacheHttpClient4Config());
    }

    /**
     * Creates a new instance of {@link Client} configured appropriately for SimpleEntityManager use.
     *
     * @param clientConfig configuration information for the client
     *
     * @return a Client
     */
    public Client newInstance(ClientConfig clientConfig) {

        // If the caller hasn't explicitly chosen something else, select a thread-safe Apache connection manager.
        if (!clientConfig.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER)) {
            clientConfig.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, new ThreadSafeClientConnManager());
        }
        return ApacheHttpClient4.create(clientConfig);
    }
}
