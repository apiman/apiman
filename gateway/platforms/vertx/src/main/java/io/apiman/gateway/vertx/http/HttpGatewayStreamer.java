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
package io.apiman.gateway.vertx.http;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.vertx.common.DoubleHandler;
import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.conversation.ServiceResponseListener;
import io.apiman.gateway.vertx.conversation.SignalRequestExecutor;
import io.apiman.gateway.vertx.io.ISimpleWriteStream;
import io.apiman.gateway.vertx.verticles.PolicyVerticle;
import io.apiman.gateway.vertx.worker.Registrant;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map.Entry;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * Handles an {@link HttpServerRequest}<=>{@link HttpServerResponse} conversation. This involves converting
 * the HTTP data into appropriate apiman equivalents which are pushed onto the event bus, where a
 * corresponding {@link PolicyVerticle} is awaiting data on the other end. Successful, unsuccessful and
 * erroneous responses are all handled and unmarshalled, as appropriate; for instance, all response types are
 * translated into their equivalent HTTP response codes.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpGatewayStreamer implements Registrant, Handler<HttpServerRequest> {

    private String policyVerticleAddress;
    private String stripString;
    private Logger logger;

    private SignalRequestExecutor<ServiceRequest> requestExecutor;
    private ServiceResponseListener responseListener;

    private HttpServerRequest request;
    private HttpServerResponse response;
    private Handler<Void> endHandler;

    public HttpGatewayStreamer(Vertx vertx, Container container, String policyVerticleAddress, String stripString) {
        this.policyVerticleAddress = policyVerticleAddress;
        this.logger = container.logger();
        this.stripString = stripString;

        // Handles request related stuff
        requestExecutor = new SignalRequestExecutor<>(vertx, container, policyVerticleAddress
                + VertxEngineConfig.APIMAN_RT_EP_SERVICE_REQUEST);

        // Handles response related stuff
        responseListener = new ServiceResponseListener(vertx, container, policyVerticleAddress
                + VertxEngineConfig.APIMAN_RT_EP_SERVICE_RESPONSE);
    }

    @Override
    public String getAddress() {
        return policyVerticleAddress;
    }

    @Override
    public void handle(HttpServerRequest r) {
        this.request = r;

        request.pause();

        handleResponse(request.response());

        ServiceRequest serviceRequest = HttpServiceFactory.build(request, stripString);

        // Received a request, handler called when #ready has been indicated.
        requestExecutor.execute(serviceRequest, new Handler<ISimpleWriteStream>() {

            @Override
            public void handle(final ISimpleWriteStream writeStream) {
                request.dataHandler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer buffer) {
                        writeStream.write(buffer);
                    }
                });

                request.endHandler(new Handler<Void>() {

                    @Override
                    public void handle(Void flag) {
                        writeStream.end();
                    }
                });

                logger.debug("Resuming GatewayStreamer receive");
                // As we've now set the handlers, it's safe to resume.
                request.resume();
            }
        });
    }

    private void handleResponse(HttpServerResponse r) {
        this.response = r;

        response.setChunked(true);

        responseListener.serviceHandler(new Handler<ServiceResponse>() {

            @Override
            public void handle(ServiceResponse amanResponse) {
                logger.debug("Received a response on: " + policyVerticleAddress + " code: "
                        + amanResponse.getCode() + " with message: " + amanResponse.getMessage());

                HttpServiceFactory.buildResponse(response, amanResponse);
            }
        });

        // Set up the response listening sections.
        responseListener.bodyHandler(new Handler<Buffer>() {

            @Override
            public void handle(Buffer buffer) {
                logger.debug("Received chunk in GatewayStreamer " + buffer.toString());
                response.write(buffer);
            }
        });

        responseListener.endHandler(new Handler<Void>() {

            @Override
            public void handle(Void flag) {
                end(response);
            }
        });

        // TODO move to ServiceFactory
        responseListener.errorHandler(new Handler<Throwable>() {

            @Override
            public void handle(Throwable error) {
                response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                response.setStatusMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
                response.headers().add("X-Exception", String.valueOf(error.getMessage())); //$NON-NLS-1$
                response.write(ExceptionUtils.getStackTrace(error));
                end(response);
            }
        });

        responseListener.policyFailureHandler(new DoubleHandler<PolicyFailure, String>() {

            @Override
            public void handle(PolicyFailure failure, String rawResponse) {
              response.headers().add("X-Policy-Failure-Type", String.valueOf(failure.getType())); //$NON-NLS-1$
              response.headers().add("X-Policy-Failure-Message", failure.getMessage()); //$NON-NLS-1$
              response.headers().add("X-Policy-Failure-Code", String.valueOf(failure.getFailureCode())); //$NON-NLS-1$
              response.headers().add(HttpHeaders.CONTENT_TYPE, "application/json"); //$NON-NLS-1$

              HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;

              if (failure.getType() == PolicyFailureType.Authentication) {
                  status = HttpResponseStatus.UNAUTHORIZED;
              } else if (failure.getType() == PolicyFailureType.Authorization) {
                  status = HttpResponseStatus.FORBIDDEN;
              }

              response.setStatusCode(status.code());
              response.setStatusMessage(failure.getMessage());

              for (Entry<String, String> entry : failure.getHeaders().entrySet()) {
                  response.headers().add(entry.getKey(), entry.getValue());
              }

              response.write(rawResponse);
              end(response);
            }
        });

        responseListener.listen();
    }

    @Override
    public void endHandler(Handler<Void> endHandler) {
        this.endHandler = endHandler;
    }

    private void end(HttpServerResponse response) {
        response.end();
        requestExecutor.reset();
        responseListener.reset();

        if(endHandler != null)
            endHandler.handle((Void) null);
    }
}
