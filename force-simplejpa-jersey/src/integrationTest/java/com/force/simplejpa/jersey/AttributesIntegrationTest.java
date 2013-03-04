/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.SimpleEntityManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author dbuccola
 */
public class AttributesIntegrationTest {
    private SimpleEntityManagerFactory emFactory = new SimpleEntityManagerFactory();
    private Set<Object> objects = new HashSet<Object>();

    @After
    public void deleteTestObjects() {
        SimpleEntityManager em = emFactory.newInstance();
        for (Object object : objects) {
            try {
                em.remove(object);
            } catch (Exception e) {
                System.err.println("Failed to clean up object: " + e.toString());
            }
        }
    }

    @Test
    public void testPersistAndFind() {
        SimpleEntityManager em = emFactory.newInstance();
        ContactWithAttributes contact = new ContactWithAttributes();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        Map<String,String> attributes = new HashMap<String,String>();
        attributes.put("sharing", "me:Edit, manager:Read");
        contact.setAttributes(attributes);
        em.persist(contact);
        objects.add(contact);

        ContactWithAttributes contact2 = em.find(ContactWithAttributes.class, contact.getId());
        Assert.assertEquals(contact.getId(), contact2.getId());
    }

}
