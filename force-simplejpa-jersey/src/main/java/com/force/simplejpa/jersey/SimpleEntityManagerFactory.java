/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import javax.ws.rs.core.HttpHeaders;

import com.force.simplejpa.AuthorizationConnector;
import com.force.simplejpa.RestSimpleEntityManager;
import com.force.simplejpa.SimpleEntityManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;

/**
 * A factory for instances of {@link com.force.simplejpa.SimpleEntityManager} that use a {@link JerseyRestConnector} for
 * communications.
 *
 * @author dbuccola
 */
public class SimpleEntityManagerFactory {
    private static final String DEFAULT_API_VERSION = "v26.0";

    private final String apiVersion;
    private final Client client;
    private final AuthorizationConnector authorizationConnector;

    /**
     * Constructs a new factory with a default {@link Client} and a default {@link AuthorizationConnector} that uses an
     * OAuth username-password flow with credential information retrieved from the environment.
     * <p/>
     * A default Salesforce API     * version is used (currently v26.0).
     * <p/>
     * This form is likely not very useful in production environments because of the limited authorization support but
     * can be useful for integration tests.
     *
     * @see PasswordAuthorizationConnector
     */
    public SimpleEntityManagerFactory() {
        this(ApacheHttpClient4.create());
    }

    /**
     * Constructs a new factory with a specific {@link Client} and a default {@link AuthorizationConnector} that uses an
     * OAuth username-password flow with credential information retrieved from the environment.
     * <p/>
     * A default Salesforce API version is used (currently v26.0).
     * <p/>
     * You'll typically use this form if you want to supply an {@link Client} that is pre-configured with specific
     * filters.
     * <p/>
     * This form is likely not very useful in production environments because of the limited authorization support but
     * can be useful for integration tests.
     *
     * @param client an initialized client instance
     *
     * @see PasswordAuthorizationConnector
     */
    public SimpleEntityManagerFactory(Client client) {
        this(client, new PasswordAuthorizationConnector());
    }

    /**
     * Constructs a new factory with a specific {@link Client} and a specific {@link AuthorizationConnector}.
     * <p/>
     * A default Salesforce API version is used (currently v26.0).
     *
     * @param client                 a client instance
     * @param authorizationConnector an authorization connector
     */
    public SimpleEntityManagerFactory(Client client, AuthorizationConnector authorizationConnector) {
        this(client, authorizationConnector, DEFAULT_API_VERSION);
    }

    /**
     * Constructs a new factory with a specific {@link Client} and a specific {@link AuthorizationConnector} and a
     * specific Salesforce API version.
     * <p/>
     * This likely the most common constructor to use in production environments because it gives full configuration
     * control to the surrounding application.
     *
     * @param client                 a client instance
     * @param authorizationConnector an authorization connector
     * @param apiVersion             the desired Salesforce API version
     */
    public SimpleEntityManagerFactory(final Client client, final AuthorizationConnector authorizationConnector, String apiVersion) {
        if (client == null) {
            throw new IllegalArgumentException("client is null");
        }
        if (authorizationConnector == null) {
            throw new IllegalArgumentException("authorizationConnector is null");
        }
        if (apiVersion == null) {
            throw new IllegalArgumentException("apiVersion is null");
        }

        this.client = client;
        this.authorizationConnector = authorizationConnector;
        this.apiVersion = apiVersion;

        client.addFilter(new ClientFilter() {
            @Override
            public ClientResponse handle(ClientRequest clientRequest) {
                clientRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, authorizationConnector.getAuthorization());
                return getNext().handle(clientRequest);
            }
        });
    }

    /**
     * Creates new instances of SimpleEntityManager bound to a JerseyRestConnector.
     *
     * @return a SimpleEntityManager
     */
    public SimpleEntityManager newInstance() {
        WebResource instanceResource = client.resource(authorizationConnector.getInstanceUrl());
        WebResource dataResource = instanceResource.path("services/data/" + apiVersion);
        return new RestSimpleEntityManager(new JerseyRestConnector(dataResource));
    }
}
