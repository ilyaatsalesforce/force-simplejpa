/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa;

import org.apache.commons.io.IOUtils;
import org.junit.Before;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;

/**
 * An abstract base class that includes a small amount of supporting infrastructure to help with unit tests related to a
 * {@link SimpleEntityManager}.
 * <p/>
 * This class initializes a {@link SimpleEntityManager} with a mock {@link RestConnector} that can be used to interact
 * with the network inputs and outputs of the {@link SimpleEntityManager}.
 * <p/>
 * Also included here are some utility routines for reading mock network requests and responses from resources.
 *
 * @author dbuccola
 */
public abstract class AbstractSimpleEntityManagerTest {
    private String resourcePrefix = this.getClass().getPackage().getName().replace('.', '/');

    protected SimpleEntityManager em;

    protected RestConnector mockConnector;

    @Before
    public void initializeMockEntityManager() {
        mockConnector = mock(RestConnector.class);
        em = new RestSimpleEntityManager(mockConnector);
    }

    /**
     * Returns the contents of the specified resource as a String.
     *
     * @param relativeResourceName the name of the resource relative to the package of the current class.
     * @return the contents of the specified resource as a String.
     * @throws IOException if the resource can not be found or could not be converted to a string.
     */
    protected String getResourceString(String relativeResourceName) throws IOException {
        return IOUtils.toString(getResourceStream(relativeResourceName), "UTF-8");
    }

    /**
     * Returns the contents of the specified resource as an InputStream.
     *
     * @param relativeResourceName the name of the resource relative to the package of the current class.
     * @return the contents of the specified resource as an InputStream
     * @throws FileNotFoundException if the resource can not be found
     */
    protected InputStream getResourceStream(String relativeResourceName) throws FileNotFoundException {
        String resourceName = resourcePrefix + '/' + relativeResourceName;
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            throw new FileNotFoundException(String.format("Testing resource not found: %s", resourceName));
        }
        return inputStream;
    }
}
