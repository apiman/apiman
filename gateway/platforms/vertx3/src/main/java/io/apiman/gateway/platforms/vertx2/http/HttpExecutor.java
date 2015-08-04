/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.platforms.vertx2.http;

import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx2.io.VertxPolicyFailure;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceResponse;
import io.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.apiman.gateway.platforms.vertx2.services.impl.PolicyToIngestorServiceImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Execute request and response, with requests and responses piped over the bus to/from a policy verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class HttpExecutor implements Handler<HttpServerRequest> {

    private HttpServerRequest request;
    private HttpServerResponse response;
    private Vertx vertx;
    private String httpSessionUuid;
    private Logger log;
    private boolean transportSecure;

    public HttpExecutor(Vertx vertx,
            Logger log,
            boolean transportSecure) {
        this.transportSecure = transportSecure;
        this.vertx = vertx;
        this.httpSessionUuid = UUID.randomUUID().toString(); // Unique ID for this request/response sequence.
        this.log = log;
    }

    @Override
    public void handle(HttpServerRequest request) {
        this.request = request;
        this.response = request.response();
        request.exceptionHandler(this::setError);
        response.exceptionHandler(this::setError);

        execute();
    }

    private void execute() {
        // Use chunked mode to avoid needing to know full payload size in advance
        response.setChunked(true);

        // Pause the request, as we want to give explicit permission to transmit body chunks
        request.pause();

        // Create proxy in order to initialise a unique channel with a PolicyVerticle
        InitializeIngestorService initService = InitializeIngestorService.createProxy(vertx,
                        VertxEngineConfig.GATEWAY_ENDPOINT_POLICY_INGESTION);

        // Setup response leg first
        setupResponse();

        // The outer proxy returns an inner proxy, which is our request 'connection'
        initService.createIngestor(httpSessionUuid, this::setupRequest);
    }

    private void setupRequest(AsyncResult<IngestorToPolicyService> result) {
        log.debug("Setting up the request with " + httpSessionUuid);
        IngestorToPolicyService send = result.result();

        if (result.succeeded()) {
            VertxServiceRequest serviceRequest = HttpServiceFactory.buildRequest(request, transportSecure);

            send.head(serviceRequest, (Handler<AsyncResult<Boolean>>) ready -> {
                if (ready.succeeded()) {
                    // Signalled that we can send the body.
                    if (ready.result()) {
                        request.handler((Handler<Buffer>) buffer -> { // TODO fixme when pluggable marshallers available...
                            send.write(buffer.toString("UTF-8"));
                        });

                        request.endHandler((Handler<Void>) end -> {
                            // Finish *send* to Policy
                            send.end(this::errorCatchingReplyHandler);
                        });

                        System.out.println("Resuming body in HttpExecutor");
                        request.resume();
                    } else { // It didn't work; probably a policy failure. Just call #end immediately.
                        System.out.println("There was a policy failure");
                        System.out.println("End called....");
                        send.end(this::errorCatchingReplyHandler);
                    }
                } else {
                    System.out.println("There was a failure - likely an exception. Should see it come through end()");
                    setError(ready.cause());
                }
            });
        } else {
            setError(result.cause());
        }
    }

    private void errorCatchingReplyHandler(AsyncResult<Void> result) {
        if (result.failed())
            setError(result.cause());
    }

    private void setupResponse() {
        PolicyToIngestorServiceImpl receive = new PolicyToIngestorServiceImpl();

        ProxyHelper.registerService(PolicyToIngestorService.class, vertx, receive,
                httpSessionUuid + VertxEngineConfig.GATEWAY_ENDPOINT_RESPONSE);

        receive.headHandler((Handler<VertxServiceResponse>) apimanResponse -> {
            HttpServiceFactory.buildResponse(response, apimanResponse);
        });

        receive.bodyHandler((Handler<VertxApimanBuffer>) buff -> {
            response.write((Buffer) buff.getNativeBuffer());
        });

        receive.endHandler((Handler<Void>) vertxEngineResult -> {
            System.out.println("Response has been written");
            if (!response.ended())
                response.end();
        });

        receive.policyFailureHandler((Handler<VertxPolicyFailure>) failure -> {
            setPolicyFailure(failure);
        });
    }

    private void setError(Throwable error) {
        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        response.setStatusMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
        response.headers().add("X-Exception", String.valueOf(error.getMessage())); //$NON-NLS-1$
        response.end(ExceptionUtils.getStackTrace(error));
    }

    private void setPolicyFailure(VertxPolicyFailure failure) {
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

        for (Entry<String, String> entry : failure.getHeaders().entrySet()) {
            response.headers().add(entry.getKey(), entry.getValue());
        }

        response.write(failure.getRaw());
    }
}
