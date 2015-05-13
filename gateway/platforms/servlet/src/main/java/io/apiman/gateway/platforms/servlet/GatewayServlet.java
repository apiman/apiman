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
package io.apiman.gateway.platforms.servlet;

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.servlet.i18n.Messages;
import io.apiman.gateway.platforms.servlet.io.ByteBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * The API Management gateway servlet.  This servlet is responsible for converting inbound
 * http servlet requests into {@link ServiceRequest}s so that they can be fed into the
 * API Management machinery.  It also is responsible for converting the resulting
 * {@link ServiceResponse} into an HTTP Servlet Response that is suitable for returning
 * to the caller.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class GatewayServlet extends HttpServlet {

    private static final long serialVersionUID = 958726685958622333L;
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Constructor.
     */
    public GatewayServlet() {
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


    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doOptions(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "OPTIONS"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "HEAD"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doTrace(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        doAction(req, resp, "TRACE"); //$NON-NLS-1$
    }

    /**
     * Generic handler for all types of http actions/verbs.
     * @param req
     * @param resp
     * @param action
     */
    protected void doAction(final HttpServletRequest req, final HttpServletResponse resp, String action) {
        // Read the request.
        ServiceRequest srequest = null;
        try {
            srequest = readRequest(req);
            srequest.setType(action);
        } catch (Exception e) {
            writeError(resp, e);
            return;
        }

        final CountDownLatch latch = new CountDownLatch(1);

        // Now execute the request via the apiman engine
        IServiceRequestExecutor executor = getEngine().executor(srequest, new IAsyncResultHandler<IEngineResult>() {
            @Override
            public void handle(IAsyncResult<IEngineResult> asyncResult) {
                if (asyncResult.isSuccess()) {
                    IEngineResult engineResult = asyncResult.getResult();
                    if (engineResult.isResponse()) {
                        try {
                            writeResponse(resp, engineResult.getServiceResponse());
                            final ServletOutputStream outputStream = resp.getOutputStream();
                            engineResult.bodyHandler(new IAsyncHandler<IApimanBuffer>() {
                                @Override
                                public void handle(IApimanBuffer chunk) {
                                    try {
                                        if (chunk instanceof ByteBuffer) {
                                            byte [] buffer = (byte []) chunk.getNativeBuffer();
                                            outputStream.write(buffer, 0, chunk.length());
                                        } else {
                                            outputStream.write(chunk.getBytes());
                                        }
                                    } catch (IOException e) {
                                        // This will get caught by the service connector, which will abort the
                                        // connection to the back-end service.
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                            engineResult.endHandler(new IAsyncHandler<Void>() {
                                @Override
                                public void handle(Void result) {
                                    try {
                                        resp.flushBuffer();
                                    } catch (IOException e) {
                                        // This will get caught by the service connector, which will abort the
                                        // connection to the back-end service.
                                        throw new RuntimeException(e);
                                    } finally {
                                        latch.countDown();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            // this would mean we couldn't get the output stream from the response, so we
                            // need to abort the engine result (which will let the back-end connection
                            // close down).
                            engineResult.abort();
                            latch.countDown();
                            throw new RuntimeException(e);
                        }
                    } else {
                        writeFailure(resp, engineResult.getPolicyFailure());
                        latch.countDown();
                    }
                } else {
                    writeError(resp, asyncResult.getError());
                    latch.countDown();
                }
            }
        });
        executor.streamHandler(new IAsyncHandler<ISignalWriteStream>() {
            @Override
            public void handle(ISignalWriteStream connectorStream) {
                try {
                    final InputStream is = req.getInputStream();
                    ByteBuffer buffer = new ByteBuffer(2048);
                    int numBytes = buffer.readFrom(is);
                    while (numBytes != -1) {
                        connectorStream.write(buffer);
                        numBytes = buffer.readFrom(is);
                    }
                    connectorStream.end();
                } catch (IOException e) {
                    connectorStream.abort();
                    throw new RuntimeException(e);
                }
            }
        });
        executor.execute();
        try { latch.await(); } catch (InterruptedException e) { }
    }

    /**
     * Gets the engine - subclasses must implement this.
     * @return gets the engine
     */
    protected abstract IEngine getEngine();

    /**
     * Reads a {@link ServiceRequest} from information found in the inbound
     * portion of the http request.
     * @param request the undertow http server request
     * @return a valid {@link ServiceRequest}
     * @throws IOException
     */
    protected ServiceRequest readRequest(HttpServletRequest request) throws Exception {
        ServiceRequestPathInfo pathInfo = parseServiceRequestPath(request.getPathInfo());
        if (pathInfo.orgId == null) {
            throw new Exception(Messages.i18n.format("GatewayServlet.InvalidServiceEndpoint")); //$NON-NLS-1$
        }
        Map<String, String> queryParams = parseServiceRequestQueryParams(request.getQueryString());

        String apiKey = getApiKey(request, queryParams);

        ServiceRequest srequest = GatewayThreadContext.getServiceRequest();
        srequest.setApiKey(apiKey);
        srequest.setServiceOrgId(pathInfo.orgId);
        srequest.setServiceId(pathInfo.serviceId);
        srequest.setServiceVersion(pathInfo.serviceVersion);
        srequest.setDestination(pathInfo.resource);
        srequest.setQueryParams(queryParams);
        readHeaders(srequest, request);
        srequest.setRawRequest(request);
        srequest.setRemoteAddr(request.getRemoteAddr());
        srequest.setTransportSecure(request.isSecure());
        return srequest;
    }

    /**
     * Gets the API Key from the request.  The API key can be passed either via
     * a custom http request header called X-API-Key or else by a query parameter
     * in the URL called apikey.
     * @param request the inbound request
     * @param queryParams the inbound request query params
     * @return the api key or null if not found
     */
    protected String getApiKey(HttpServletRequest request, Map<String, String> queryParams) {
        String apiKey = request.getHeader("X-API-Key"); //$NON-NLS-1$
        if (apiKey == null || apiKey.trim().length() == 0) {
            apiKey = queryParams.get("apikey"); //$NON-NLS-1$
        }
        return apiKey;
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
        for (Entry<String, String> entry : policyFailure.getHeaders().entrySet()) {
            resp.setHeader(entry.getKey(), entry.getValue());
        }
        int errorCode = 500;
        if (policyFailure.getType() == PolicyFailureType.Authentication) {
            errorCode = 401;
        } else if (policyFailure.getType() == PolicyFailureType.Authorization) {
            errorCode = 403;
        } else if (policyFailure.getType() == PolicyFailureType.NotFound) {
            errorCode = 404;
        } if (policyFailure.getType() == PolicyFailureType.Other) {
            if (policyFailure.getResponseCode() >= 300) {
                errorCode = policyFailure.getResponseCode();
            }
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
            resp.setStatus(500);
            OutputStream outputStream = null;
            try {
                outputStream = resp.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);
                error.printStackTrace(writer);
                writer.flush();
                outputStream.flush();
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
        } catch (IOException e1) {
            throw new RuntimeException(error);
        }
    }

    /**
     * Parse a service request path from servlet path info.
     * @param pathInfo
     * @return the path info parsed into its component parts
     */
    protected static final ServiceRequestPathInfo parseServiceRequestPath(String pathInfo) {
        ServiceRequestPathInfo info = new ServiceRequestPathInfo();
        if (pathInfo != null) {
            String[] split = pathInfo.split("/"); //$NON-NLS-1$
            if (split.length >= 4) {
                info.orgId = split[1];
                info.serviceId = split[2];
                info.serviceVersion = split[3];
                if (split.length > 4) {
                    StringBuilder resource = new StringBuilder();
                    for (int idx = 4; idx < split.length; idx++) {
                        resource.append('/');
                        resource.append(split[idx]);
                    }
                    if (pathInfo.endsWith("/")) { //$NON-NLS-1$
                        resource.append('/');
                    }
                    info.resource = resource.toString();
                }
            }
        }
        return info;
    }

    /**
     * Parses the query string into a map.
     * @param queryString
     */
    protected static final Map<String, String> parseServiceRequestQueryParams(String queryString) {
        Map<String, String> rval = new LinkedHashMap<>();
        if (queryString != null) {
            try {
                queryString = URLDecoder.decode(queryString, "UTF-8"); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            String[] pairSplit = queryString.split("&"); //$NON-NLS-1$
            for (String paramPair : pairSplit) {
                int idx = paramPair.indexOf("="); //$NON-NLS-1$
                if (idx != -1) {
                    String key = paramPair.substring(0, idx);
                    String val = paramPair.substring(idx + 1);
                    rval.put(key, val);
                } else {
                    rval.put(paramPair, null);
                }
            }
        }

        return rval;
    }

    /**
     * Parsed service request path information.
     */
    protected static class ServiceRequestPathInfo {
        public String orgId;
        public String serviceId;
        public String serviceVersion;
        public String resource;
    }

}
