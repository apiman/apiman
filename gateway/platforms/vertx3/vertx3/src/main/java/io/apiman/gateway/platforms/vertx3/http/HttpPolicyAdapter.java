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

import io.apiman.gateway.engine.IApiClientResponse;
import io.apiman.gateway.engine.IApiRequestExecutor;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IPolicyErrorWriter;
import io.apiman.gateway.engine.IPolicyFailureWriter;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

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

    public HttpPolicyAdapter(HttpServerRequest req,
                      IPolicyFailureWriter policyFailureWriter,
                      IPolicyErrorWriter policyErrorWriter,
                      IEngine engine,
                      boolean isTls) {
        this.vertxRequest = req;
        this.policyFailureWriter = policyFailureWriter;
        this.policyErrorWriter = policyErrorWriter;
        this.engine = engine;
        this.isTls = isTls;
        this.vertxResponse = req.response();
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
        if (engineResult.isResponse()) {
            ApiResponse response = engineResult.getApiResponse();
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

    private void handleError(ApiRequest apimanRequest, Throwable error, HttpServerResponse vertxResponse) {
        vertxResponse.setChunked(true);
        policyErrorWriter.write(apimanRequest, error, new IApiClientResponse() {

            @Override
            public void write(StringBuffer buffer) {
                vertxResponse.end(buffer.toString());
            }

            @Override
            public void write(StringBuilder builder) {
                vertxResponse.end(builder.toString());
            }

            @Override
            public void write(String body) {
                vertxResponse.end(body);
            }

            @Override
            public void setStatusCode(int code) {
                vertxResponse.setStatusCode(code);
            }

            @Override
            public void setHeader(String headerName, String headerValue) {
                vertxResponse.putHeader(headerName, headerValue);
            }
        });
    }

    private void handlePolicyFailure(ApiRequest apimanRequest, PolicyFailure policyFailure, HttpServerResponse vertxResponse) {
        vertxResponse.setChunked(true);
        policyFailureWriter.write(apimanRequest, policyFailure, new IApiClientResponse() {

            @Override
            public void write(StringBuffer buffer) {
                vertxResponse.end(buffer.toString());
            }

            @Override
            public void write(StringBuilder builder) {
                vertxResponse.end(builder.toString());
            }

            @Override
            public void write(String body) {
                vertxResponse.end(body);
            }

            @Override
            public void setStatusCode(int code) {
                vertxResponse.setStatusCode(code);
            }

            @Override
            public void setHeader(String headerName, String headerValue) {
                vertxResponse.putHeader(headerName, headerValue);
            }
        });
    }
}
