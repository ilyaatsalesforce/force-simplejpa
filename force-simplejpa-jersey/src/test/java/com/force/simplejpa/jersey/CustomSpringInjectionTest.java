/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;
import com.force.simplejpa.SimpleEntityManager;
import com.sun.jersey.api.client.Client;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author dbuccola
 */
@ContextConfiguration(locations = {"classpath:com/force/simplejpa/jersey/customSpringInjectionContext.xml"})
public class CustomSpringInjectionTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private AuthorizationConnector authorizationConnector;

    @Autowired
    private Client client;

    @Autowired
    private SimpleEntityManager em;

    @Test
    public void testAutowiring() {
        assertNotNull(authorizationConnector);
        assertNotNull(client);
        assertNotNull(em);

        assertTrue(
            "Should be instance of CustomAuthorizationConnector",
            authorizationConnector instanceof CustomAuthorizationConnector);
    }
}
