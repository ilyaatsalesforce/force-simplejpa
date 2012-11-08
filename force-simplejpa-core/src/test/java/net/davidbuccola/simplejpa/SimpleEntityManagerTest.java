/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa;

import java.io.InputStream;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

/**
 * @author davidbuccola
 */
public class SimpleEntityManagerTest {
    private String resourcePrefix = this.getClass().getPackage().getName().replace('.', '/');

    EasyMockSupport mockSupport = new EasyMockSupport();

    @Test
    public void testParsingSubqueryResult() {
        RestConnector mockConnector = mockSupport.createMock(RestConnector.class);
        expect(mockConnector.doQuery(anyObject(String.class))).andReturn(getResource("subqueryResult1.json"));

        mockSupport.replayAll();
        SimpleEntityManager em = new RestSimpleEntityManager(mockConnector);
        Bean2 result = em.createQuery("select * from Bean2", Bean2.class ).getSingleResult();
        mockSupport.verifyAll();
    }

    private InputStream getResource(String relativeName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePrefix + '/' + relativeName);
    }
}
