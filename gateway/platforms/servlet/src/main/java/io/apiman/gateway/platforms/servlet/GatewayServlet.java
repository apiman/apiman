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

import io.apiman.common.util.ApimanPathUtils;
import io.apiman.common.util.ApimanPathUtils.ApiRequestPathInfo;
import io.apiman.gateway.engine.IApiClientResponse;
import io.apiman.gateway.engine.IApiRequestExecutor;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IPolicyErrorWriter;
import io.apiman.gateway.engine.IPolicyFailureWriter;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.beans.util.QueryMap;
import io.apiman.gateway.engine.io.ByteBuffer;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.servlet.i18n.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The API Management gateway servlet.  This servlet is responsible for converting inbound
 * http servlet requests into {@link ApiRequest}s so that they can be fed into the
 * API Management machinery.  It also is responsible for converting the resulting
 * {@link ApiResponse} into an HTTP Servlet Response that is suitable for returning
 * to the caller.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class GatewayServlet extends HttpServlet {

    private static final long serialVersionUID = 958726685958622333L;

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
        ApiRequest srequest;
        try {
            srequest = readRequest(req);
            srequest.setType(action);
        } catch (Exception e) {
            writeError(null, resp, e);
            return;
        }

        final CountDownLatch latch = new CountDownLatch(1);
        final ApiRequest finalRequest = srequest;

        // Now execute the request via the apiman engine
        IApiRequestExecutor executor = getEngine().executor(srequest, new IAsyncResultHandler<IEngineResult>() {
            @Override
            public void handle(IAsyncResult<IEngineResult> asyncResult) {
                if (asyncResult.isSuccess()) {
                    IEngineResult engineResult = asyncResult.getResult();
                    if (engineResult.isResponse()) {
                        try {
                            writeResponse(resp, engineResult.getApiResponse());
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
                                        // This will get caught by the API connector, which will abort the
                                        // connection to the back-end API.
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
                                        // This will get caught by the API connector, which will abort the
                                        // connection to the back-end API.
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
                        writeFailure(finalRequest, resp, engineResult.getPolicyFailure());
                        latch.countDown();
                    }
                } else {
                    writeError(finalRequest, resp, asyncResult.getError());
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
     * Gets the failure formatter to use when a policy failure is detected and
     * needs to be sent back to the calling client.
     */
    protected abstract IPolicyErrorWriter getErrorWriter();

    /**
     * Gets the failure formatter to use when a policy failure is detected and
     * needs to be sent back to the calling client.
     */
    protected abstract IPolicyFailureWriter getFailureWriter();

    /**
     * Reads a {@link ApiRequest} from information found in the inbound
     * portion of the http request.
     * @param request the undertow http server request
     * @return a valid {@link ApiRequest}
     * @throws IOException
     */
    protected ApiRequest readRequest(HttpServletRequest request) throws Exception {
        ApiRequestPathInfo pathInfo = parseApiRequestPath(request);
        if (pathInfo.orgId == null) {
            throw new Exception(Messages.i18n.format("GatewayServlet.InvalidApiEndpoint")); //$NON-NLS-1$
        }
        QueryMap queryParams = parseApiRequestQueryParams(request.getQueryString());

        String apiKey = getApiKey(request, queryParams);

        ApiRequest srequest = GatewayThreadContext.getApiRequest();
        srequest.setApiKey(apiKey);
        srequest.setApiOrgId(pathInfo.orgId);
        srequest.setApiId(pathInfo.apiId);
        srequest.setApiVersion(pathInfo.apiVersion);
        srequest.setUrl(request.getRequestURL().toString());
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
    protected String getApiKey(HttpServletRequest request, QueryMap queryParams) {
        String apiKey = request.getHeader("X-API-Key"); //$NON-NLS-1$
        if (apiKey == null || apiKey.trim().length() == 0) {
            apiKey = queryParams.get("apikey"); //$NON-NLS-1$
        }
        return apiKey;
    }

    /**
     * Reads the inbound request headers from the request and sets them on
     * the {@link ApiRequest}.
     * @param request
     * @param request
     */
    protected void readHeaders(ApiRequest srequest, HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hname = headerNames.nextElement();
            String hval = request.getHeader(hname);
            if (hname != null && hname.equalsIgnoreCase("accept") && hval != null && hval.startsWith("application/apiman.")) { //$NON-NLS-1$ //$NON-NLS-2$
                if (hval.contains("+json")) { //$NON-NLS-1$
                    hval = "application/json"; //$NON-NLS-1$
                } else if (hval.contains("+xml")) { //$NON-NLS-1$
                    hval = "text/xml"; //$NON-NLS-1$
                }
            }
            if (hname != null && hname.equalsIgnoreCase("X-API-Version")) { //$NON-NLS-1$
                continue;
            }
            srequest.getHeaders().add(hname, hval);
        }
    }

    /**
     * Writes the API response to the HTTP servlet response object.
     * @param response
     * @param sresponse
     */
    protected void writeResponse(HttpServletResponse response, ApiResponse sresponse) {
        response.setStatus(sresponse.getCode());
        HeaderMap headers = sresponse.getHeaders();
        for (Map.Entry<String, String> entry : headers) {
            String hname = entry.getKey();
            String hval = entry.getValue();
            response.setHeader(hname, hval);
        }
    }

    /**
     * Writes a policy failure to the http response.
     * @param request
     * @param resp
     * @param policyFailure
     */
    protected void writeFailure(final ApiRequest request, final HttpServletResponse resp, final PolicyFailure policyFailure) {
        getFailureWriter().write(request, policyFailure, new IApiClientResponse() {
            @Override
            public void write(StringBuffer buffer) {
                write(buffer.toString());
            }

            @Override
            public void write(StringBuilder builder) {
                write(builder.toString());
            }

            @Override
            public void write(String body) {
                try {
                    resp.getOutputStream().write(body.getBytes("UTF-8")); //$NON-NLS-1$
                    resp.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                };
            }

            /**
             * @see io.apiman.gateway.engine.IApiClientResponse#setStatusCode(int)
             */
            @Override
            public void setStatusCode(int code) {
                resp.setStatus(code);
            }

            @Override
            public void setHeader(String headerName, String headerValue) {
                resp.setHeader(headerName, headerValue);
            }
        });
    }

    /**
     * Writes an error to the servlet response object.
     * @param request
     * @param resp
     * @param error
     */
    protected void writeError(final ApiRequest request, final HttpServletResponse resp, final Throwable error) {
        getErrorWriter().write(request, error, new IApiClientResponse() {
            @Override
            public void write(StringBuffer buffer) {
                write(buffer.toString());
            }

            @Override
            public void write(StringBuilder builder) {
                write(builder.toString());
            }

            @Override
            public void write(String body) {
                try {
                    resp.getOutputStream().write(body.getBytes("UTF-8")); //$NON-NLS-1$
                    resp.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                };
            }

            /**
             * @see io.apiman.gateway.engine.IApiClientResponse#setStatusCode(int)
             */
            @Override
            public void setStatusCode(int code) {
                resp.setStatus(code);
            }

            @Override
            public void setHeader(String headerName, String headerValue) {
                resp.setHeader(headerName, headerValue);
            }
        });
    }

    /**
     * Parse a API request path from servlet path info.
     * @param pathInfo
     * @return the path info parsed into its component parts
     */
    protected static final ApiRequestPathInfo parseApiRequestPath(HttpServletRequest request) {
        return ApimanPathUtils.parseApiRequestPath(request.getHeader(ApimanPathUtils.X_API_VERSION_HEADER),
                request.getHeader(ApimanPathUtils.ACCEPT_HEADER),
                request.getPathInfo());
    }

    /**
     * Parses the query string into a map.
     * @param queryString
     */
    protected static final QueryMap parseApiRequestQueryParams(String queryString) {
        QueryMap rval = new QueryMap();
        
        if (queryString != null) {
            try {
                String[] pairSplit = queryString.split("&"); //$NON-NLS-1$
                for (String paramPair : pairSplit) {
                    int idx = paramPair.indexOf("="); //$NON-NLS-1$
                    String key, value;
                    if (idx != -1) {
                        key =  URLDecoder.decode(paramPair.substring(0, idx), "UTF-8"); //$NON-NLS-1$
                        value = URLDecoder.decode(paramPair.substring(idx + 1), "UTF-8"); //$NON-NLS-1$
                    } else {
                        key = URLDecoder.decode(paramPair, "UTF-8"); //$NON-NLS-1$
                        value = null;
                    }
                    rval.add(key,  value);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            
        }
        
        return rval;
    }

}
