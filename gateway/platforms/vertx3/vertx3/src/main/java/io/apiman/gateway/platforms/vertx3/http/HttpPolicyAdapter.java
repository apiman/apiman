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

import io.apiman.common.util.MediaType;
import io.apiman.gateway.engine.IApiRequestExecutor;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.EngineErrorResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;

import java.util.Map;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpPolicyAdapter {
    private final HttpServerRequest vertxRequest;
    private final HttpServerResponse vertxResponse;
    private final IEngine engine;
    private final Logger log;
    private final boolean isTls;

    public HttpPolicyAdapter(HttpServerRequest req,
                      IEngine engine,
                      Logger log,
                      boolean isTls) {
        this.vertxRequest = req;
        this.engine = engine;
        this.log = log;
        this.isTls = isTls;
        this.vertxResponse = req.response();
    }

    public void execute() {
        // First, pause the request to avoid losing any data
        vertxRequest.pause();

        // Transform Vert.x request object into apiman's intermediate representation.
        ApiRequest request = HttpApiFactory.buildRequest(vertxRequest, isTls);

        // Exception
        vertxRequest.exceptionHandler(ex -> {
            handleError(vertxResponse, ex);
        }); // TODO: handle here or level above?

        // Set up executor to run conversation through apiman's policy engine.
        IApiRequestExecutor executor = engine.executor(request, result -> {
            if (result.isSuccess()) {
                handleEngineResult(result.getResult());
            } else {
                // An unexpected problem occurred somewhere.
                handleError(vertxResponse, result.getError());
            }
        });

        // Write data into executor. Called when ready.
        executor.streamHandler(writeStream -> {
            vertxRequest.bodyHandler(bufferChunk -> {
                // Wrap Vert.x buffer into apiman's buffer IR, then write into engine.
                writeStream.write(new VertxApimanBuffer(bufferChunk));
            });

            vertxRequest.endHandler(end -> writeStream.end());
            // Now safe to resume body transmission.
            vertxRequest.resume();
        });

        // Actually execute
        executor.execute();
    }

    private void handleEngineResult(IEngineResult engineResult) {
        // Everything worked
        if (engineResult.isResponse()) {
            ApiResponse response = engineResult.getApiResponse();
            HttpApiFactory.buildResponse(vertxResponse, response, vertxRequest.version());
            vertxResponse.setChunked(true);

            engineResult.bodyHandler(buffer -> {
                vertxResponse.write((Buffer) buffer.getNativeBuffer());
            });

            engineResult.endHandler(end -> vertxResponse.end());
        } else { // Policy failure (i.e. denial - it's not an exception).
            log.debug(String.format("Failed with policy failure (denial): %s", engineResult.getPolicyFailure()));
            handlePolicyFailure(vertxResponse, engineResult.getPolicyFailure());
        }
    }

    private static void handleError(HttpServerResponse response, Throwable error) {
        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        response.setStatusMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
        response.headers().add("X-Gateway-Error", String.valueOf(error.getMessage())); //$NON-NLS-1$
        response.headers().add(HttpHeaders.CONTENT_TYPE,  MediaType.APPLICATION_JSON);

        EngineErrorResponse errorResponse = new EngineErrorResponse();
        errorResponse.setResponseCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        errorResponse.setMessage(error.getMessage());
        errorResponse.setTrace(error);

        response.write(Json.encode(errorResponse));
        response.end();
    }

    private static void handlePolicyFailure(HttpServerResponse response, PolicyFailure failure) {
        response.headers().add("X-Policy-Failure-Type", String.valueOf(failure.getType())); //$NON-NLS-1$
        response.headers().add("X-Policy-Failure-Message", failure.getMessage()); //$NON-NLS-1$
        response.headers().add("X-Policy-Failure-Code", String.valueOf(failure.getFailureCode())); //$NON-NLS-1$
        response.headers().add(HttpHeaders.CONTENT_TYPE,  MediaType.APPLICATION_JSON);

        int code = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();

        switch (failure.getType()) {
        case Authentication:
            code = HttpResponseStatus.UNAUTHORIZED.code();
            break;
        case Authorization:
            code = HttpResponseStatus.FORBIDDEN.code();
            break;
        case NotFound:
            code = HttpResponseStatus.NOT_FOUND.code();
            break;
        case Other:
            code = failure.getResponseCode();
            break;
        }

        response.setStatusCode(code);
        response.setStatusMessage(failure.getMessage());

        for (Map.Entry<String, String> entry : failure.getHeaders()) {
            response.headers().add(entry.getKey(), entry.getValue());
        }

        response.end(Json.encode(failure));
    }
}
