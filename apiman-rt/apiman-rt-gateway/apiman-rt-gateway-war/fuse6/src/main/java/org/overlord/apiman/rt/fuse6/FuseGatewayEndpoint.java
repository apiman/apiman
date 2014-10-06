/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.apiman.rt.fuse6;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.codehaus.jackson.map.ObjectMapper;
import org.overlord.apiman.rt.api.rest.impl.AbstractResourceImpl;
import org.overlord.apiman.rt.engine.EngineResult;
import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.PolicyFailureType;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * A simple JAX-RS endpoint that is invoked for all inbound requests. Obviously
 * this supports REST style endpoints only, so we'll have to figure out
 * something else in Fuse for other styles.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("/gateway")
public class FuseGatewayEndpoint extends AbstractResourceImpl {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * All GET REST calls to the gateway come in through here.
     * @param path
     */
    @Path("/{path:.*}")
    @GET
    @POST
    @PUT
    @DELETE
    public Response doGet(@Context HttpServletRequest request, @PathParam("path") String path) {
        Response response;
        try {
            ServiceRequest srequest = readRequest(request);
            srequest.setType(request.getMethod());

            Future<IAsyncResult<EngineResult>> futureResult = getEngine().execute(srequest);
            IAsyncResult<EngineResult> asyncResult = futureResult.get();
            if (asyncResult.isError()) {
                throw new Exception(asyncResult.getError());
            } else {
                EngineResult result = asyncResult.getResult();
                if (result.isResponse()) {
                    response = createResponse(result.getServiceResponse());
                } else {
                    response = createResponse(result.getPolicyFailure());
                }
            }
        } catch (Throwable e) {
            response = createResponse(e);
        } finally {
            FuseGatewayThreadContext.reset();
        }
        return response;
    }

    /**
     * Reads a {@link ServiceRequest} from information found in the inbound
     * portion of the http request.
     * @param request the undertow http server request
     * @return a valid {@link ServiceRequest}
     * @throws IOException 
     */
    protected ServiceRequest readRequest(HttpServletRequest request) throws Exception {
        String apiKey = getApiKey(request);
        if (apiKey == null) {
            throw new Exception("API key not found."); //$NON-NLS-1$
        }

        ServiceRequest srequest = FuseGatewayThreadContext.getServiceRequest();
        srequest.setApiKey(apiKey);
        srequest.setDestination(getDestination(request));
        readHeaders(srequest, request);
        srequest.setBody(request.getInputStream());
        srequest.setRawRequest(request);
        srequest.setRemoteAddr(request.getRemoteAddr());
        return srequest;
    }

    /**
     * Gets the API Key from the request.  The API key can be passed either via
     * a custom http request header called X-API-Key or else by a query parameter
     * in the URL called apikey.
     * @param request the inbound request
     * @return the api key or null if not found
     */
    protected String getApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key"); //$NON-NLS-1$
        if (apiKey == null || apiKey.trim().length() == 0) {
            apiKey = getApiKeyFromQuery(request);
        }
        return apiKey;
    }

    /**
     * Gets the API key from the request's query string.
     * @param request the inbound request
     * @return the api key or null if not found
     */
    protected String getApiKeyFromQuery(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null) {
            return null;
        }
        int idx = queryString.indexOf("apikey="); //$NON-NLS-1$
        if (idx >= 0) {
            int endIdx = queryString.indexOf('&', idx);
            if (endIdx == -1) {
                endIdx = queryString.length();
            }
            return queryString.substring(idx + 7, endIdx);
        } else {
            return null;
        }
    }

    /**
     * Returns the path to the resource.
     * @param request
     */
    protected String getDestination(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path;
    }

    /**
     * Reads the inbound request headers from the request and sets them on
     * the {@link ServiceRequest}.
     * @param request
     * @param request
     */
    protected void readHeaders(ServiceRequest srequest, HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hname = headerNames.nextElement();
            String hval = request.getHeader(hname);
            srequest.getHeaders().put(hname, hval);
        }
    }

    /**
     * Creates a jax-rs response from the {@link ServiceResponse}.
     * @param sresponse
     */
    protected Response createResponse(ServiceResponse sresponse) {
        ResponseBuilder builder = Response.status(sresponse.getCode());
        Map<String, String> headers = sresponse.getHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String hname = entry.getKey();
            String hval = entry.getValue();
            builder.header(hname, hval);
        }
        if (sresponse.getBody() != null) {
            builder.entity(sresponse.getBody());
        }
        return builder.build();
    }
    
    /**
     * Creates a jax-rs response from the {@link PolicyFailure}.
     * @param policyFailure
     */
    private Response createResponse(PolicyFailure policyFailure) {
        int errorCode = 500;
        if (policyFailure.getType() == PolicyFailureType.Authentication) {
            errorCode = 401;
        } else if (policyFailure.getType() == PolicyFailureType.Authorization) {
            errorCode = 403;
        }
        ResponseBuilder builder = Response.status(errorCode).type("application/json"); //$NON-NLS-1$

        builder.header("X-Policy-Failure-Type", String.valueOf(policyFailure.getType())); //$NON-NLS-1$
        builder.header("X-Policy-Failure-Message", policyFailure.getMessage()); //$NON-NLS-1$
        builder.header("X-Policy-Failure-Code", String.valueOf(policyFailure.getFailureCode())); //$NON-NLS-1$
        for (Entry<String, String> entry : policyFailure.getHeaders().entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }
        try {
            builder.entity(mapper.writer().writeValueAsString(policyFailure));
            return builder.build();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    /**
     * Creates a jax-rs response from an error.
     * @param resp
     * @param error
     */
    protected Response createResponse(Throwable error) {
        StringWriter entity = new StringWriter();
        PrintWriter writer = new PrintWriter(entity);
        error.printStackTrace(writer);
        return Response.serverError().type("text/plain").entity(entity.getBuffer().toString()).header("X-Exception", error.getMessage()).build(); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
