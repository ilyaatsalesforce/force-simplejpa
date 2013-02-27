/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.SimpleEntityManager;
import com.sun.jersey.api.client.Client;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author dbuccola
 */
@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext1.xml"})
public class SpringInjectionTest {
    @Autowired
    private Client client;

    @Autowired
    private SimpleEntityManager em;

    @Test
    public void testClientFactory() {
        // Just a placeholder for now. If we get this far then things are looking up. Need to add real tests.
    }
}
