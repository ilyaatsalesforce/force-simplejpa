/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;
import com.force.simplejpa.RestSimpleEntityManager;
import com.force.simplejpa.SimpleEntityManager;
import com.sun.jersey.api.client.Client;
import org.apache.commons.lang.Validate;

/**
 * A simple (non-Spring) factory for instances of {@link com.force.simplejpa.SimpleEntityManager} that use a
 * {@link JerseyRestConnector} for communications.
 *
 * @author dbuccola
 */
public class SimpleEntityManagerFactory {
    private final ClientFactory clientFactory = new ClientFactory();
    private AuthorizationConnector defaultAuthorizationConnector = null; // Lazily populated.

    /**
     * Creates a new instance of {@link SimpleEntityManager} with a default {@link Client} and a default
     * {@link AuthorizationConnector} that uses an OAuth username-password flow with credential information retrieved
     * from the environment. The most current Salesforce API version is used by default.
     * <p/>
     * This form is likely not very useful in production environments because of the limited authorization support but
     * can be useful for integration tests.
     *
     * @return a SimpleEntityManager
     * @see PasswordAuthorizationConnector
     */
    public SimpleEntityManager newInstance() {
        return newInstance(getDefaultAuthorizationConnector());
    }

    /**
     * Creates a new instance of {@link SimpleEntityManager} with a default {@link Client} and a specific
     * {@link AuthorizationConnector}. The most current Salesforce API version is used by default.
     * <p/>
     * This is probably the most common constructor to use in production environments because it provides sufficient
     * control over authorization support but defaults everything else for simplicity.
     *
     * @param authorizationConnector an authorization connector
     * @return a SimpleEntityManager
     */
    public SimpleEntityManager newInstance(AuthorizationConnector authorizationConnector) {
        return newInstance(authorizationConnector, clientFactory.newInstance(authorizationConnector), null);
    }

    /**
     * Creates a new instance of {@link SimpleEntityManager} with a specific {@link Client} and a specific
     * {@link AuthorizationConnector} and a specific Salesforce API version.
     * <p/>
     * You'll typically use this form if you want to supply a {@link Client} that is pre-configured with specific
     * filters or if you need full control over the api version.
     *
     * @param client                 a client instance
     * @param authorizationConnector an authorization connector
     * @param apiVersion             the desired Salesforce API version
     * @return a SimpleEntityManager
     */
    public SimpleEntityManager newInstance(AuthorizationConnector authorizationConnector, Client client, String apiVersion) {
        Validate.notNull(authorizationConnector, "authorizationConnector must not be null");
        Validate.notNull(client, "client must not be null");

        return new RestSimpleEntityManager(new JerseyRestConnector(authorizationConnector, client, apiVersion));
    }

    private AuthorizationConnector getDefaultAuthorizationConnector() {
        if (defaultAuthorizationConnector == null) {
            defaultAuthorizationConnector = new PasswordAuthorizationConnector();
        }
        return defaultAuthorizationConnector;
    }
}
