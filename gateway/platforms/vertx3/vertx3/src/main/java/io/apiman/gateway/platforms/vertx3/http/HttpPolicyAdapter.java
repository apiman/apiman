/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.gateway.platforms.vertx3.http;

import io.apiman.gateway.engine.*;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashSet;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpPolicyAdapter {
    private final HttpServerRequest vertxRequest;
    private final HttpServerResponse vertxResponse;
    private final IPolicyFailureWriter policyFailureWriter;
    private final IPolicyErrorWriter policyErrorWriter;
    private final IEngine engine;
    private final Logger log = LoggerFactory.getLogger(HttpPolicyAdapter.class);
    private final boolean isTls;
    private HashSet<String> allowedCorsOrigins;

    public HttpPolicyAdapter(HttpServerRequest req,
                      IPolicyFailureWriter policyFailureWriter,
                      IPolicyErrorWriter policyErrorWriter,
                      IEngine engine,
                      boolean isTls,
                      HashSet<String> allowedCorsOrigins) {
        this.vertxRequest = req;
        this.policyFailureWriter = policyFailureWriter;
        this.policyErrorWriter = policyErrorWriter;
        this.engine = engine;
        this.isTls = isTls;
        this.vertxResponse = req.response();
        this.allowedCorsOrigins = allowedCorsOrigins;
    }

    public void execute() {
        try {
            _execute();
        } catch (Exception ex) {
            handleError(new ApiRequest(), ex, vertxResponse);
        }
    }

    public void _execute() {
        log.trace("Received request {0}", vertxRequest.absoluteURI()); //$NON-NLS-1$

        // First, pause the request to avoid losing any data
        vertxRequest.pause();

        // Transform Vert.x request object into apiman's intermediate representation.
        ApiRequest request = HttpApiFactory.buildRequest(vertxRequest, isTls);

        // Exception
        vertxRequest.exceptionHandler(ex -> {
            handleError(request, ex, vertxResponse);
        }); // TODO: Should probably log error also.

        // Set up executor to run conversation through apiman's policy engine.
        IApiRequestExecutor executor = engine.executor(request, result -> {
            if (result.isSuccess()) {
                handleEngineResult(request, result.getResult());
            } else {
                // An unexpected problem occurred somewhere.
                handleError(request, result.getError(), vertxResponse);
            }
        });

        // Write data into executor. Called when ready.
        executor.streamHandler(writeStream -> {
            vertxRequest.handler(bufferChunk -> {
                // Apply back-pressure.
                if (writeStream.isFull()) {
                    vertxRequest.pause();
                }
                // Wrap Vert.x buffer into apiman's buffer IR, then write into engine.
                writeStream.write(new VertxApimanBuffer(bufferChunk));
            });

            // Called when back-pressure has reduced sufficiently to resume.
            writeStream.drainHandler(onDrain -> vertxRequest.resume());

            vertxRequest.endHandler(end -> writeStream.end());

            // Now safe to resume body transmission.
            vertxRequest.resume();
        });

        // Actually execute
        executor.execute();
    }

    private void handleEngineResult(ApiRequest request, IEngineResult engineResult) {
        // Everything worked
        if (engineResult.isResponse() && responseIsNotClosed()) {
            ApiResponse response = engineResult.getApiResponse();

            setCorsHeadersForSwaggerUi(request, response);

            HttpApiFactory.buildResponse(vertxResponse, response, vertxRequest.version());

            if (!response.getHeaders().containsKey("Content-Length")) { //$NON-NLS-1$
                vertxResponse.setChunked(true);
            }

            engineResult.bodyHandler(buffer -> {
                vertxResponse.write((Buffer) buffer.getNativeBuffer());
            });

            engineResult.endHandler(end -> vertxResponse.end());
        } else { // Policy failure (i.e. denial - it's not an exception).
            log.debug(String.format("Failed with policy failure (denial): %s", engineResult.getPolicyFailure())); //$NON-NLS-1$
            handlePolicyFailure(request, engineResult.getPolicyFailure(), vertxResponse);
        }
    }

    /**
     * Set correct CORS headers in the response for SwaggerUis
     * @param request the ApiRequest
     * @param response the ApiResponse
     * @llink  @see io.apiman.common.servlet.ApimanCorsFilter
     */
    private void setCorsHeadersForSwaggerUi(ApiRequest request, ApiResponse response) {
        if (hasOriginHeader(request) && originIsAllowed(request)) {
            // check if response already contains CORS Headers - we don't overwrite existing CORS Headers from API or CORS Policy
            if (hasNoCorsAccessControlAllowOrigin(response)) {
                // Check if the request from the UI is a preflight request
                if (isPreflightRequest(request)) {
                    response.getHeaders().clear();
                    // we allow all requested methods and headers
                    response.getHeaders().add("Access-Control-Allow-Origin", request.getHeaders().get("Origin"));
                    response.getHeaders().add("Access-Control-Allow-Credentials", "true");
                    response.getHeaders().add("Access-Control-Max-Age", "1800");
                    response.getHeaders().add("Access-Control-Allow-Methods", request.getHeaders().get("Access-Control-Request-Method"));
                    response.getHeaders().add("Access-Control-Allow-Headers", request.getHeaders().get("Access-Control-Request-Headers"));
                    response.setCode(200);
                    response.setMessage("OK");
                } else {
                    response.getHeaders().add("Access-Control-Allow-Origin", request.getHeaders().get("Origin"));
                    response.getHeaders().add("Access-Control-Allow-Credentials", "true");
                }
            }
        }
    }

    /**
     * Check if the origin of the request is in the list of allows cors origins.
     *
     * @param request the ApiRequest
     * @return true if the origin is allowed, else false
     */
    private boolean originIsAllowed(ApiRequest request) {
        String origin = request.getHeaders().get("Origin").trim();
        return allowedCorsOrigins.contains(origin);
    }

    /**
     * Returns true if the Access-Control-Allow-Origin is not present.
     *
     * @param response the ApiResponse
     * @return true if has no Access-Control-Allow-Origin header, else false
     */
    private boolean hasNoCorsAccessControlAllowOrigin(ApiResponse response) {
        return !response.getHeaders().containsKey("Access-Control-Allow-Origin");
    }

    /**
     * Determines whether the request is a CORS preflight request.
     *
     * @param request the ApiRequest
     * @return true if preflight, else false
     * @link io.apiman.common.servlet.ApimanCorsFilter#isPreflightRequest
     */
    private boolean isPreflightRequest(ApiRequest request) {
        return request.getType().equals("OPTIONS") && hasOriginHeader(request);
    }

    /**
     * Returns true if the Origin request header is present.
     *
     * @param request the ApiRequest
     * @return true if has origin header, else false
     * @link io.apiman.common.servlet.ApimanCorsFilter#hasOriginHeader
     */
    private boolean hasOriginHeader(ApiRequest request){
        String origin = request.getHeaders().get("Origin");
        return origin != null && origin.trim().length() > 0;
    }

    private void handleError(ApiRequest apimanRequest, Throwable error, HttpServerResponse vertxResponse) {
        if (responseIsNotClosed()) {
            vertxResponse.setChunked(true);
            policyErrorWriter.write(apimanRequest, error, new DefaultApiClientResponse());
        } else {
            // probably log this
        }
    }

    private void handlePolicyFailure(ApiRequest apimanRequest, PolicyFailure policyFailure, HttpServerResponse vertxResponse) {
        if (responseIsNotClosed()) {
            vertxResponse.setChunked(true);
            policyFailureWriter.write(apimanRequest, policyFailure, new DefaultApiClientResponse());
        } else {
            // probably log this
        }
    }

    private boolean responseIsNotClosed() {
        return !vertxResponse.closed() && !vertxResponse.ended();
    }


    private class DefaultApiClientResponse implements IApiClientResponse {

        @Override
        public void setStatusCode(int code) {
            vertxResponse.setStatusCode(code);
        }

        @Override
        public void setHeader(String headerName, String headerValue) {
            vertxResponse.putHeader(headerName, headerValue);
        }

        @Override
        public void write(String body) {
            vertxRequest.resume();
            vertxResponse.end(body);
        }

        @Override
        public void write(StringBuilder builder) {
            vertxRequest.resume();
            vertxResponse.end(builder.toString());
        }

        @Override
        public void write(StringBuffer buffer) {
            vertxRequest.resume();
            vertxResponse.end(buffer.toString());
        }
    }
}
