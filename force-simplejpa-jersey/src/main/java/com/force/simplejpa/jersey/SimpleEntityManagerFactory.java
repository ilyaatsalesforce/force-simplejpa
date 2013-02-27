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
 * A factory for instances of {@link com.force.simplejpa.SimpleEntityManager} that use a {@link JerseyRestConnector} for
 * communications.
 *
 * @author dbuccola
 */
public class SimpleEntityManagerFactory {
    private final String apiVersion;
    private final Client client;
    private final AuthorizationConnector authorizationConnector;

    /**
     * Constructs a new factory with a default {@link Client} and a default {@link AuthorizationConnector} that uses an
     * OAuth username-password flow with credential information retrieved from the environment. The most current
     * Salesforce API version is used by default.
     * <p/>
     * This form is likely not very useful in production environments because of the limited authorization support but
     * can be useful for integration tests.
     *
     * @see PasswordAuthorizationConnector
     */
    public SimpleEntityManagerFactory() {
        this(new PasswordAuthorizationConnector());
    }

    /**
     * Constructs a new factory with a default {@link Client} and a specific {@link AuthorizationConnector}. The most
     * current Salesforce API version is used by default.
     * <p/>
     * This is probably the most common constructor to use in production environments because it provides sufficient
     * control over authorization support but defaults everything else for simplicity.
     *
     * @param authorizationConnector an authorization connector
     */
    public SimpleEntityManagerFactory(AuthorizationConnector authorizationConnector) {
        this(authorizationConnector, new JerseyClientFactory().newInstance(authorizationConnector), null);
    }

    /**
     * Constructs a new factory with a specific {@link Client} and a specific {@link AuthorizationConnector} and a
     * specific Salesforce API version.
     * <p/>
     * You'll typically use this form if you want to supply a {@link Client} that is pre-configured with specific
     * filters or if you need full control over the api version.
     *
     * @param client                 a client instance
     * @param authorizationConnector an authorization connector
     * @param apiVersion             the desired Salesforce API version
     */
    public SimpleEntityManagerFactory(AuthorizationConnector authorizationConnector, final Client client, String apiVersion) {
        Validate.notNull(authorizationConnector, "authorizationConnector must not be null");
        Validate.notNull(client, "client must not be null");

        this.authorizationConnector = authorizationConnector;
        this.client = client;
        this.apiVersion = apiVersion;
    }

    /**
     * Creates new instances of SimpleEntityManager bound to a JerseyRestConnector.
     *
     * @return a SimpleEntityManager
     */
    public SimpleEntityManager newInstance() {
        return new RestSimpleEntityManager(new JerseyRestConnector(client, authorizationConnector.getInstanceUrl(), apiVersion));
    }
}
