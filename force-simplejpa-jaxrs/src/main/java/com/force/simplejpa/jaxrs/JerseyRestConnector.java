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

import com.force.simplejpa.EntityRequestException;
import com.force.simplejpa.RestConnector;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * A {@link RestConnector} implementation that uses Sun's Jersey 1.x client to connect to Salesforce persistence using
 * the REST API.
 *
 * @author davidbuccola
 */
public final class JerseyRestConnector implements RestConnector {
    private WebResource dataResource;

    /**
     * Constructs a new instance with the given {@link WebResource}.
     *
     * @param dataResource a {@link WebResource} which refers to a particular version of the Salesforce "data" resource.
     *                     As an example, the {@link WebResource} should correspond to a URL something like this:
     *                     <tt>https://na1.salesforce.com/services/data/v26.0</tt> (the instance prefix or the API
     *                     version may be different in your case).
     */
    public JerseyRestConnector(WebResource dataResource) {
        this.dataResource = dataResource;
    }

    @Override
    public InputStream doCreate(String entityType, String jsonBody) {
        try {
            return dataResource.path("sobjects").path(entityType)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(InputStream.class, jsonBody);
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Create failed: %s", extractMessage(e)), e);
        }
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

    @Override
    public void doUpdate(String entityType, String id, String jsonBody) {
        try {
            ClientResponse response = dataResource.path("sobjects").path(entityType).path(id)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .method("PATCH", ClientResponse.class, jsonBody);

            if (response.getStatus() >= 300) {
                throw new UniformInterfaceException(response, true);
            }
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Updated failed: %s", extractMessage(e)), e);
        }
    }

    @Override
    public void doDelete(String entityType, String id) {
        try {
            ClientResponse response = dataResource.path("sobjects").path(entityType).path(id)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .delete(ClientResponse.class);

            if (response.getStatus() >= 300) {
                throw new UniformInterfaceException(response, true);
            }
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Delete failed: %s", extractMessage(e)), e);
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
