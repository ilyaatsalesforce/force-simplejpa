/*
 * Copyright, 2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package com.force.simplejpa.jersey;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.force.simplejpa.AuthorizationConnector;

/**
 * An implementation of {@link AuthorizationConnector} which looks for the authorization information and instance
 * information in the headers of the currently active inbound Spring HTTP request.
 * <p/>
 * The authorization header of the currently active inbound request is propagated to the outbound REST request. The
 * instance url information is expected in a header of name "Force-Instance-Url".
 *
 * @author davidbuccola
 */
public class SpringRequestAuthorizationConnector implements AuthorizationConnector {
    @Override
    public final String getAuthorization() {
        return getAuthorization(getCurrentRequest());
    }

    @Override
    public final URI getInstanceUrl() {
        return getInstanceUrl(getCurrentRequest());
    }

    /**
     * Obtain the current {@link javax.servlet.http.HttpServletRequest} from the Spring container.
     *
     * @return the current servlet request
     */
    protected final HttpServletRequest getCurrentRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException(
                "No Spring RequestAttributes are available. There's probably no active Spring web request");
        }
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            throw new IllegalStateException("Spring RequestAttributes are not of type ServletRequestAttributes");
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * Extracts authentication information from the current inbound request.
     *
     * @param request the current request
     *
     * @return a value for the outbound "Authorization" header
     */
    protected String getAuthorization(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader;
        } else {
            throw new RuntimeException("Missing Authorization header");
        }
    }

    /**
     * Extracts the instance URL from the current inbound request.
     *
     * @param request the current request
     *
     * @return the instance URL
     */
    protected URI getInstanceUrl(HttpServletRequest request) {
        String instanceUrlHeader = request.getHeader("Force-Instance-Url");
        if (instanceUrlHeader != null) {
            try {
                return new URI(instanceUrlHeader);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid Force-Instance-Url header", e);
            }
        } else {
            throw new RuntimeException("Missing Force-Instance-Url header");
        }
    }
}
