/*
 * Copyright, 2012-2013, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;
import com.force.simplejpa.EntityRequestException;
import com.force.simplejpa.RestConnector;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link RestConnector} implementation that uses Sun's Jersey 1.x client to connect to Salesforce persistence using
 * the REST API.
 *
 * @author davidbuccola
 */
public final class JerseyRestConnector implements RestConnector {
    private static final Logger log = LoggerFactory.getLogger(JerseyRestConnector.class);
    private static final Cache<URI, String> versionedPathCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private final AuthorizationConnector authorizationConnector;
    private final Client client;
    private final String apiVersion;

    private final AtomicReference<WebResource> dataResourceHolder = new AtomicReference<WebResource>();

    /**
     * Constructs a new instance with the given instance URL and api version.
     *
     * @param authorizationConnector the authorization connector used to obtain the instance URL
     * @param client                 the fully configured Jersey client
     * @param apiVersion             an optional apiVersion. If specified as <code>null</code>, then the highest
     */
    public JerseyRestConnector(AuthorizationConnector authorizationConnector, Client client, String apiVersion) {
        this.client = client;
        this.apiVersion = apiVersion;
        this.authorizationConnector = authorizationConnector;
    }

    /**
     * Obtain the data resource object. Lazily create it if necessary. It is created lazily because the process can
     * involve server I/O to obtain the version number and that is something we don't want to do at construction time
     * when things might not be fully wired up yet.
     *
     * @return the data resource object
     */
    private WebResource getDataResource() {
        WebResource dataResource = dataResourceHolder.get();
        if (dataResource == null) {
            WebResource instanceResource = client.resource(authorizationConnector.getInstanceUrl());
            dataResource = instanceResource.path(getVersionedPath(instanceResource, apiVersion));
            dataResourceHolder.compareAndSet(null, dataResource);
        }
        return dataResource;
    }

    @Override
    public InputStream doCreate(String entityType, String jsonBody, Map<String, String> headers) {
        try {
            WebResource.Builder resource = buildResource(getDataResource().path("sobjects").path(entityType), headers);
            return resource.post(InputStream.class, jsonBody);
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Create failed: %s", extractMessage(e)), e);
        }
    }

    @Override
    public InputStream doGet(URI uri, Map<String, String> headers) {
        try {
            WebResource.Builder resource = buildResource(getDataResource().uri(uri), headers);
            return resource.get(InputStream.class);
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Get failed: %s", extractMessage(e)), e);
        }
    }

    @Override
    public InputStream doQuery(String soql, Map<String, String> headers) {
        try {
            WebResource.Builder resource = buildResource(getDataResource().path("query").queryParam("q", soql), headers);
            return resource.get(InputStream.class);
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Query failed: %s", extractMessage(e)), e);
        }
    }

    @Override
    public void doUpdate(String entityType, String id, String jsonBody, Map<String, String> headers) {
        try {
            WebResource.Builder resource = buildResource(getDataResource().path("sobjects").path(entityType).path(id), headers);
            ClientResponse response = resource.method("PATCH", ClientResponse.class, jsonBody);

            if (response.getStatus() >= 300) {
                throw new UniformInterfaceException(response, true);
            }
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Updated failed: %s", extractMessage(e)), e);
        }
    }

    @Override
    public void doDelete(String entityType, String id, Map<String, String> headers) {
        try {
            WebResource.Builder resource = buildResource(getDataResource().path("sobjects").path(entityType).path(id), headers);
            ClientResponse response = resource.delete(ClientResponse.class);

            if (response.getStatus() >= 300) {
                throw new UniformInterfaceException(response, true);
            }
        } catch (UniformInterfaceException e) {
            throw new EntityRequestException(String.format("Delete failed: %s", extractMessage(e)), e);
        }
    }

    private WebResource.Builder buildResource(WebResource resource, Map<String, String> headers) {
        WebResource.Builder builder = resource
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .type(MediaType.APPLICATION_JSON_TYPE);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder = builder.header(entry.getKey(), entry.getValue());
            }
        }
        return builder;
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

    private static String getVersionedPath(final WebResource instanceResource, String apiVersion) {
        if (apiVersion != null) {
            return "/services/data/" + apiVersion;
        } else {
            return getPathForHighestVersion(instanceResource);
        }
    }

    private static String getPathForHighestVersion(final WebResource instanceResource) {
        try {
            return versionedPathCache.get(instanceResource.getURI(), new Callable<String>() {
                @Override
                public String call() throws JSONException {
                    log.debug(String.format("Asking %s about Salesforce API versions", instanceResource.getURI()));
                    JSONArray versionChoices =
                        instanceResource.path("services/data")
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .get(JSONArray.class);

                    JSONObject highestVersion = (JSONObject) versionChoices.get(versionChoices.length() - 1);
                    return (String) highestVersion.get("url");
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve Salesforce API version information", e);
        }
    }
}
