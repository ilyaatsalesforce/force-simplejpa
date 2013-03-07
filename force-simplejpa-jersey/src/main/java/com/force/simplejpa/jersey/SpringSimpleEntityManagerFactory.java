/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;
import com.force.simplejpa.SimpleEntityManager;
import com.sun.jersey.api.client.Client;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Spring factory for instances of {@link com.force.simplejpa.SimpleEntityManager} that use a
 * {@link JerseyRestConnector} for communications.
 *
 * @author dbuccola
 */
@Component("simpleEntityManagerFactory")
public class SpringSimpleEntityManagerFactory implements FactoryBean<SimpleEntityManager> {

    private final SimpleEntityManagerFactory internalFactory = new SimpleEntityManagerFactory();

    @Autowired
    private Client client;

    @Autowired
    private AuthorizationConnector authorizationConnector;

    private String apiVersion = null;

    /**
     * Sets the Salesforce API version used by the generated {@link SimpleEntityManager} instances.
     *
     * @param apiVersion a Salesforce API version (for example: "v28.0")
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public SimpleEntityManager getObject() {
        return internalFactory.newInstance(authorizationConnector, client, apiVersion);
    }

    @Override
    public Class<?> getObjectType() {
        return SimpleEntityManager.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
