/*
 * Copyright, 2013, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;

import java.net.URI;

/**
 * @author dbuccola
 */
public class CustomAuthorizationConnector implements AuthorizationConnector {
    @Override
    public String getAuthorization() {
        return "OAuth 1999";
    }

    @Override
    public URI getInstanceUrl() {
        return URI.create("http://localhost:1999");
    }
}
