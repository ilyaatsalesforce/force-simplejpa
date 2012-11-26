/*
 * Copyright, 1999-2012, SALESFORCE.com
 * All Rights Reserved
 * Company Confidential
 */
package net.davidbuccola.simplejpa.jaxrs;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import net.davidbuccola.simplejpa.EntityRequestException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * @author davidbuccola
 */
@Named
@Provider
public class EntityRequestExceptionMapper implements ExceptionMapper<EntityRequestException> {
    @Override
    public Response toResponse(EntityRequestException exception) {
        if (exception.getCause() instanceof UniformInterfaceException) {
            ClientResponse clientResponse = ((UniformInterfaceException) exception.getCause()).getResponse();
            switch (clientResponse.getClientResponseStatus()) {
            case OK:
            case CREATED:
            case ACCEPTED:
            case NON_AUTHORITIVE_INFORMATION:
            case NO_CONTENT:
            case RESET_CONTENT:
            case PARTIAL_CONTENT:
                throw exception; // These shouldn't show up as exceptions, something must be wrong. Throw.

            case MOVED_PERMANENTLY:
            case FOUND:
            case SEE_OTHER:
            case NOT_MODIFIED:
            case USE_PROXY:
            case TEMPORARY_REDIRECT:
                throw exception; // These shouldn't show up as exceptions, something must be wrong. Throw.

            case BAD_REQUEST:
            case FORBIDDEN:
            case METHOD_NOT_ALLOWED:
            case NOT_ACCEPTABLE:
            case LENGTH_REQUIRED:
            case PRECONDITION_FAILED:
            case REQUEST_URI_TOO_LONG:
            case UNSUPPORTED_MEDIA_TYPE:
            case REQUESTED_RANGE_NOT_SATIFIABLE:
            case EXPECTATION_FAILED:
            case INTERNAL_SERVER_ERROR:
            case NOT_IMPLEMENTED:
            case HTTP_VERSION_NOT_SUPPORTED:
                throw exception; // These are likely our problem. Throw.

            case UNAUTHORIZED:
            case PAYMENT_REQUIRED:
            case NOT_FOUND:
            case PROXY_AUTHENTICATION_REQUIRED:
            case REQUEST_TIMEOUT:
            case CONFLICT:
            case GONE:
            case REQUEST_ENTITY_TOO_LARGE:
            case BAD_GATEWAY:
            case SERVICE_UNAVAILABLE:
            case GATEWAY_TIMEOUT:
                // Map to an equivalent response to our client.
                return Response
                        .status(clientResponse.getStatus())
                        .entity(clientResponse.getEntityInputStream())
                        .build();

            default:
                throw exception;
            }
        } else {
            // Not an exception we care about. Pass it on down the line.
            throw exception;
        }
    }
}