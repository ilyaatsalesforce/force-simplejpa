/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.SimpleEntityManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author davidbuccola
 */
public class SimpleEntityManagerIntegrationTest {
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
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        em.persist(contact);
        objects.add(contact);

        Contact contact2 = em.find(Contact.class, contact.getId());
        Assert.assertEquals(contact.getId(), contact2.getId());
    }

    @Test
    public void testMerge() {
        SimpleEntityManager em = emFactory.newInstance();
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        em.persist(contact);
        objects.add(contact);

        contact.setEmail("john.smith@acme.com");
        em.merge(contact);

        Contact contact2 = new Contact();
        contact2.setId(contact.getId());
        contact2.setPhone("925-555-1212");
        em.merge(contact2);

        Contact contact3 = em.find(Contact.class, contact.getId());
        Assert.assertEquals(contact.getId(), contact3.getId());
        Assert.assertEquals(contact.getFirstName(), contact3.getFirstName());
        Assert.assertEquals(contact.getLastName(), contact3.getLastName());
        Assert.assertEquals(contact.getEmail(), contact3.getEmail());
        Assert.assertEquals(contact2.getPhone(), contact3.getPhone());
    }

    @Test
    public void testRemove() {
        SimpleEntityManager em = emFactory.newInstance();
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Smith");
        em.persist(contact);
        objects.add(contact);

        Contact contact2 = em.find(Contact.class, contact.getId());
        Assert.assertEquals(contact.getId(), contact2.getId());

        em.remove(contact);
        objects.remove(contact);

        Contact contact3 = em.find(Contact.class, contact.getId());
        Assert.assertNull(contact3);
    }
}
