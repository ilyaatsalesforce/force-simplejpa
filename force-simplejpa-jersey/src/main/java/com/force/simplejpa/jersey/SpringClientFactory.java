/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;
import com.sun.jersey.api.client.Client;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Spring factory for instances of {@link Client} configured appropriately for SimpleEntityManager use.
 * <p/>
 * This factory creates instances of {@link com.sun.jersey.client.apache4.ApacheHttpClient4} because the Apache client
 * is needed to support the HTTP "PATCH" request required by the Salesforce API. If you decide not to use this factory
 * and instead provide a client instance of your own then make sure that the client instance can support HTTP "PATCH".
 * You really should use this factory or something derived from it.
 * <p/>
 * By default, the returned instances use a {@link org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager} in order
 * to support multi-threaded use.
 *
 * @author dbuccola
 */
@Component("clientFactory")
public class SpringClientFactory implements FactoryBean<Client> {

    private final ClientFactory internalFactory = new ClientFactory();

    @Autowired
    private AuthorizationConnector authorizationConnector;

    @Override
    public Client getObject() throws Exception {
        return internalFactory.newInstance(authorizationConnector);
    }

    @Override
    public Class<?> getObjectType() {
        return Client.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
