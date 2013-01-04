/*
 * Copyright, 2012, SALESFORCE.com 
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import com.force.simplejpa.AuthorizationConnector;
import com.force.simplejpa.RestSimpleEntityManager;
import com.force.simplejpa.SimpleEntityManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for creating a Jersey-based SimpleEntityManager.
 * <p/>
 * These utilities are shared by a couple different SimpleEntityManagerFactory implementations.
 *
 * @author davidbuccola
 */
public final class SimpleEntityManagerFactoryUtils {
    private static final Logger log = LoggerFactory.getLogger(SimpleEntityManagerFactoryUtils.class);

    private static final Cache<URI, String> versionedPathCache =
        CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private SimpleEntityManagerFactoryUtils() {
        throw new UnsupportedOperationException("Can not be instantiated");
    }

    /**
     * Creates a new instance of SimpleEntityManager bound to a JerseyRestConnector.
     *
     * @param client                 a fully configured Jersey client
     * @param authorizationConnector an authorization connector
     * @param apiVersion             an optional apiVersion. If specified as <code>null</code>, then the highest
     *                               available API version supported by the server will be used.
     * @return a SimpleEntityManager
     */
    public static SimpleEntityManager newInstance(Client client, AuthorizationConnector authorizationConnector, String apiVersion) {
        WebResource instanceResource = client.resource(authorizationConnector.getInstanceUrl());
        WebResource dataResource = getDataResource(instanceResource, apiVersion);
        return new RestSimpleEntityManager(new JerseyRestConnector(dataResource));
    }

    private static WebResource getDataResource(WebResource instanceResource, String apiVersion) {
        String versionedPath;
        if (apiVersion != null) {
            versionedPath = "/services/data/" + apiVersion;
        } else {
            versionedPath = getPathForHighestVersion(instanceResource);
        }
        return instanceResource.path(versionedPath);
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
