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
package org.overlord.apiman.rt.war.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.Future;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.overlord.apiman.rt.engine.EngineResult;
import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.PolicyFailureType;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.war.WarGateway;
import org.overlord.apiman.rt.war.WarGatewayThreadContext;
import org.overlord.apiman.rt.war.i18n.Messages;

/**
 * The API Management gateway servlet.  This servlet is responsible for converting inbound
 * http servlet requests into {@link ServiceRequest}s so that they can be fed into the 
 * API Management machinery.  It also is responsible for converting the resulting 
 * {@link ServiceResponse} into an HTTP Servlet Response that is suitable for returning
 * to the caller.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarGatewayServlet extends HttpServlet {

    private static final long serialVersionUID = 958726685958622333L;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Constructor.
     */
    public WarGatewayServlet() {
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "GET"); //$NON-NLS-1$
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "POST"); //$NON-NLS-1$
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "PUT"); //$NON-NLS-1$
    }
    
    /**
     * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "DELETE"); //$NON-NLS-1$
    }

    /**
     * Generic handler for all types of http actions/verbs.
     * @param req
     * @param resp
     * @param action 
     */
    protected void doAction(HttpServletRequest req, HttpServletResponse resp, String action) {
        try {
            ServiceRequest srequest = readRequest(req);
            srequest.setType(action);

            Future<IAsyncResult<EngineResult>> futureResult = WarGateway.engine.execute(srequest);
            IAsyncResult<EngineResult> asyncResult = futureResult.get();
            if (asyncResult.isError()) {
                throw new Exception(asyncResult.getError());
            } else {
                EngineResult result = asyncResult.getResult();
                if (result.isResponse()) {
                    writeResponse(resp, result.getServiceResponse());
                } else {
                    writeFailure(resp, result.getPolicyFailure());
                }
            }
        } catch (Throwable e) {
            writeError(resp, e);
        } finally {
            WarGatewayThreadContext.reset();
        }
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
            throw new Exception(Messages.i18n.format("WarGatewayServlet.MissingAPIKey")); //$NON-NLS-1$
        }

        ServiceRequest srequest = WarGatewayThreadContext.getServiceRequest();
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
     * Writes the service response to the HTTP servlet response object.
     * @param response
     * @param sresponse
     */
    protected void writeResponse(HttpServletResponse response, ServiceResponse sresponse) {
        response.setStatus(sresponse.getCode());
        Map<String, String> headers = sresponse.getHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String hname = entry.getKey();
            String hval = entry.getValue();
            response.setHeader(hname, hval);
        }
        if (sresponse.getBody() != null) {
            InputStream body = null;
            OutputStream out = null;
            try {
                body = sresponse.getBody();
                out = response.getOutputStream();
                IOUtils.copy(body, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(body);
                IOUtils.closeQuietly(out);
            }
        }
        try {
            response.flushBuffer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Writes a policy failure to the http response.
     * @param resp
     * @param policyFailure
     */
    private void writeFailure(HttpServletResponse resp, PolicyFailure policyFailure) {
        resp.setContentType("application/json"); //$NON-NLS-1$
        resp.setHeader("X-Policy-Failure-Type", String.valueOf(policyFailure.getType())); //$NON-NLS-1$
        resp.setHeader("X-Policy-Failure-Message", policyFailure.getMessage()); //$NON-NLS-1$
        resp.setHeader("X-Policy-Failure-Code", String.valueOf(policyFailure.getFailureCode())); //$NON-NLS-1$
        int errorCode = 500;
        if (policyFailure.getType() == PolicyFailureType.Authentication) {
            errorCode = 401;
        } else if (policyFailure.getType() == PolicyFailureType.Authorization) {
            errorCode = 403;
        }
        resp.setStatus(errorCode);
        try {
            mapper.writer().writeValue(resp.getOutputStream(), policyFailure);
            IOUtils.closeQuietly(resp.getOutputStream());
        } catch (Exception e) {
            writeError(resp, e);
        } finally {
        }
    }

    /**
     * Writes an error to the servlet response object.
     * @param resp
     * @param error
     */
    protected void writeError(HttpServletResponse resp, Throwable error) {
        try {
            resp.setHeader("X-Exception", error.getMessage()); //$NON-NLS-1$
            resp.sendError(500, error.getMessage());
        } catch (IOException e1) {
            throw new RuntimeException(error);
        }
    }

}
