/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import javax.ws.rs.core.HttpHeaders;

/**
 * A simple (non-Spring) factory for instances of {@link Client} configured appropriately for SimpleEntityManager use.
 * <p/>
 * This factory creates instances of {@link ApacheHttpClient4} because the Apache client is needed to support the HTTP
 * "PATCH" request required by the Salesforce API. If you decide not to use this factory and instead provide a client
 * instance of your own then make sure that the client instance can support HTTP "PATCH". You really should use this
 * factory or something derived from it.
 * <p/>
 * By default, the returned instances use a {@link ThreadSafeClientConnManager} in order to support multi-threaded use.
 *
 * @author dbuccola
 */
public final class ClientFactory {
    /**
     * Creates a new instance of {@link Client} configured appropriately for SimpleEntityManager use.
     *
     * @param authorizationConnector an authorization connector
     * @return a Client
     */
    public Client newInstance(AuthorizationConnector authorizationConnector) {
        return newInstance(authorizationConnector, new DefaultApacheHttpClient4Config());
    }

    /**
     * Creates a new instance of {@link Client} configured appropriately for SimpleEntityManager use.
     *
     * @param authorizationConnector an authorization connector
     * @param clientConfig           configuration information for the client
     * @return a Client
     */
    public Client newInstance(final AuthorizationConnector authorizationConnector, ClientConfig clientConfig) {

        // If the caller hasn't explicitly chosen something else, select a thread-safe Apache connection manager.
        if (!clientConfig.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER)) {
            clientConfig.getProperties().put(
                ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER, new ThreadSafeClientConnManager());
        }

        Client client = ApacheHttpClient4.create(clientConfig);
        client.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest clientRequest) {
                clientRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, authorizationConnector.getAuthorization());
                return getNext().handle(clientRequest);
            }
        });
        return client;
    }
}
