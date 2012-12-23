/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import org.junit.Assert;
import org.junit.Test;

import com.force.simplejpa.SimpleEntityManager;

/**
 * @author dbuccola
 */
public class SimpleEntityManagerIntegrationTest {
    private SimpleEntityManagerFactory emFactory = new SimpleEntityManagerFactory();

    @Test
    public void testPersistAndFind() {
        SimpleEntityManager em = emFactory.newInstance();
        Contact contact = new Contact();
        contact.setFirstName("Miguel");
        contact.setLastName("Indurain");
        em.persist(contact);

        Contact contact2 = em.find(Contact.class, contact.getId());
        Assert.assertEquals(contact.getId(), contact2.getId());
    }

    @Test
    public void testMerge() {
        SimpleEntityManager em = emFactory.newInstance();
        Contact contact = new Contact();
        contact.setFirstName("Miguel");
        contact.setLastName("Indurain");
        em.persist(contact);

        contact.setEmail("miguel.indurain@tdf.org");
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
        contact.setFirstName("Miguel");
        contact.setLastName("Indurain");
        em.persist(contact);

        Contact contact2 = em.find(Contact.class, contact.getId());
        Assert.assertEquals(contact.getId(), contact2.getId());

        em.remove(contact);

        Contact contact3 = em.find(Contact.class, contact.getId());
        Assert.assertNull(contact3);
    }
}
