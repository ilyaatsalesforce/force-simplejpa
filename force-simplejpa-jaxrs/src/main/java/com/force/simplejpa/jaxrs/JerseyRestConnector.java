/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jaxrs;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import com.force.simplejpa.EntityRequestException;
import com.force.simplejpa.RestConnector;

/**
 * A {@link RestConnector} implementation that uses Jersey to connect to Salesforce persistence using the REST API.
 *
 * @author davidbuccola
 */
public final class JerseyRestConnector implements RestConnector {
    private WebResource dataResource;

    public JerseyRestConnector(WebResource dataResource) {
        this.dataResource = dataResource;
    }

    @Override
    public InputStream doGet(URI uri) {
        try {
            return dataResource.uri(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(InputStream.class);
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Get failed: %s", extractMessage(e)), e);
        }
    }

    @Override
    public InputStream doCreate(String entityType, byte[] body) {
        try {
            return dataResource.path("sobjects")
                .path(entityType)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(InputStream.class, body);
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Create failed: %s", extractMessage(e)), e);
        }
    }

    @Override
    public InputStream doQuery(String soql) {
        try {
            return dataResource.path("query")
                .queryParam("q", soql)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(InputStream.class);
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Query failed: %s", extractMessage(e)), e);
        }
    }

    private String extractMessage(UniformInterfaceException e) {
        try {
            JSONArray jsonResponse = e.getResponse().getEntity(JSONArray.class);
            JSONObject errorObject = (JSONObject) jsonResponse.get(0);
            return errorObject.getString("message");
        } catch (Exception e1) {
            // Failed to extract Force error message. There probably was none. Just return exception message.
            return e.getMessage();
        }
    }
}
