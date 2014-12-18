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
package io.apiman.gateway.vertx.verticles;

import io.apiman.gateway.engine.beans.ServiceEndpoint;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.vertx.api.AuthenticatingRouteMatcher;
import io.apiman.gateway.vertx.api.GenericError;
import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

/**
 * Verticle implementing apiman's gateway API.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpApiVerticle extends ApimanVerticleWithEngine {

    private RouteMatcher routeMatcher;
    private Set<String> apiSubscriberSet = new HashSet<>();

    @Override
    public void start() {
        super.start();

        routeMatcher = new AuthenticatingRouteMatcher(amanConfig, logger);
        initializeApi();
        listen();
    }

    public void listen() {
        vertx.createHttpServer()
                .requestHandler(routeMatcher)
                .listen(amanConfig.getRouteMap().getAddress(verticleType()), amanConfig.hostname(),
                        new Handler<AsyncResult<HttpServer>>() {

                            @Override
                            public void handle(AsyncResult<HttpServer> event) {
                                apiListener.listen(engine);
                            }
                        });
    }

    @Override
    public String verticleType() {
        return "api"; //$NON-NLS-1$
    }

    private void initializeApi() {
        // Collect verticles that are interested in updates.
        eb.registerHandler(VertxEngineConfig.APIMAN_API_SUBSCRIBE, new Handler<Message<String>>() {

            @Override
            public void handle(Message<String> message) {
                apiSubscriberSet.add(message.body());
                //logger.info("Adding api subscriber " + message.body());
            };
        });

        // Set up routematcher
        setupRoutes();
    }

    private void setupRoutes() {
        applicationApi();
        serviceApi();
        systemApi();

        // Default handler
        routeMatcher.allWithRegEx(".*", new Handler<HttpServerRequest>() { //$NON-NLS-1$

            @Override
            public void handle(HttpServerRequest request) {
                request.response().setStatusCode(HttpResponseStatus.NOT_FOUND.code());
                request.response().setStatusMessage(HttpResponseStatus.NOT_FOUND.reasonPhrase());
                request.response().end();
            }
        });
    }

    private void applicationApi() {
        // Register
        routeMatcher.put("/api/applications", new Handler<HttpServerRequest>() { //$NON-NLS-1$

            @Override
            public void handle(final HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer body) {
                        logger.debug("Got put request for applications register"); //$NON-NLS-1$

                        sendAll(VertxEngineConfig.APIMAN_API_APPLICATIONS_REGISTER, body.toString(),

                                new ResponseHandler(request.response(), new Handler<Void>() {

                                    @Override
                                    public void handle(Void event) {
                                        request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                                    }
                                })
                        );
                    }
                });

            }
        });

        // Unregister
        routeMatcher.delete("/api/applications/:organizationId/:applicationId/:version", //$NON-NLS-1$
                new Handler<HttpServerRequest>() {

                    @Override
                    public void handle(final HttpServerRequest request) {
                        JsonObject json = new JsonObject();
                        json.putString("organizationId", request.params().get("organizationId")); //$NON-NLS-1$ //$NON-NLS-2$
                        json.putString("applicationId", request.params().get("applicationId")); //$NON-NLS-1$ //$NON-NLS-2$
                        json.putString("version", request.params().get("version")); //$NON-NLS-1$ //$NON-NLS-2$

                        logger.debug(json);

                        sendAll(VertxEngineConfig.APIMAN_API_APPLICATIONS_DELETE, json,
                                new ResponseHandler(request.response(), new Handler<Void>() {

                                    @Override
                                    public void handle(Void event) {
                                        request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                                    }
                                }));
                    }
                });
    }

    private void serviceApi() {

        // getServiceEndpoint
        routeMatcher.get(":organizationId/:serviceId/:version/endpoint", new Handler<HttpServerRequest>() { //$NON-NLS-1$

            @Override
            public void handle(HttpServerRequest request) {
                ServiceEndpoint endpoint = new ServiceEndpoint();
                endpoint.setEndpoint("http://" + amanConfig.getEndpoint() + ":" //$NON-NLS-1$ //$NON-NLS-2$
                        + amanConfig.getRouteMap().getAddress(HttpDispatcherVerticle.VERTICLE_NAME));
                request.response().end(Json.encode(endpoint));
            }
        });

        // Publish
        routeMatcher.put("/api/services", new Handler<HttpServerRequest>() { //$NON-NLS-1$

            @Override
            public void handle(final HttpServerRequest request) {
                request.bodyHandler(new Handler<Buffer>() {

                    @Override
                    public void handle(Buffer body) {
                        sendAll(VertxEngineConfig.APIMAN_API_SERVICES_REGISTER, body.toString(),
                                new ResponseHandler(request.response(), new Handler<Void>() {

                                    @Override
                                    public void handle(Void event) {
                                        request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                                    }
                                }));
                    }
                });

            }
        });

        // Retire
        routeMatcher.delete("/api/services/:organizationId/:serviceId/:version", //$NON-NLS-1$
                new Handler<HttpServerRequest>() {

                    @Override
                    public void handle(final HttpServerRequest request) {
                        JsonObject json = new JsonObject();
                        json.putString("organizationId", request.params().get("organizationId")); //$NON-NLS-1$ //$NON-NLS-2$
                        json.putString("serviceId", request.params().get("serviceId")); //$NON-NLS-1$ //$NON-NLS-2$
                        json.putString("version", request.params().get("version")); //$NON-NLS-1$ //$NON-NLS-2$

                        logger.debug(json);

                        sendAll(VertxEngineConfig.APIMAN_API_SERVICES_DELETE, json,
                                new ResponseHandler(request.response(), new Handler<Void>() {

                                    @Override
                                    public void handle(Void event) {
                                        request.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                                    }
                                }));
                    }
                });
    }

    private void systemApi() {
        routeMatcher.get("/api/system", new Handler<HttpServerRequest>() { //$NON-NLS-1$

            @Override
            public void handle(HttpServerRequest request) {
                SystemStatus status = new SystemStatus();// TODO how to get/set global system status? Maybe
                                                         // the init verticle is in charge of this.
                status.setUp(true);
                status.setVersion(engine.getVersion());

                request.response().setChunked(true);
                request.response().write(Json.encode(status));
                request.response().end();
            }
        });
    }

    private void sendAll(String address, String message, final Handler<JsonObject> handler) {
        StatusHandler statusHandler = new StatusHandler(handler);

        for (String uuid : apiSubscriberSet) {
            eb.send(uuid + address, message, statusHandler);
        }
    }

    // TODO consolidate sendAll
    private void sendAll(String address, JsonObject json, final Handler<JsonObject> handler) {
        StatusHandler statusHandler = new StatusHandler(handler);

        for (String uuid : apiSubscriberSet) {
            eb.send(uuid + address, json, statusHandler);
        }
    }

    private class StatusHandler implements Handler<Message<JsonObject>> {
        private boolean collectiveStatus = true;
        private int respondants = 0;
        private Handler<JsonObject> responseHandler;
        private boolean idempotent_flag = false;

        public StatusHandler(Handler<JsonObject> responseHandler) {
            this.responseHandler = responseHandler;
        }

        @Override
        public void handle(Message<JsonObject> message) {
            JsonObject json = message.body();
            // Logical and together statuses to ensure all are true.
            collectiveStatus = collectiveStatus && json.getBoolean("status"); //$NON-NLS-1$
            // Call only iff: all responses are successful or on *first* failure.
            // Duplicate failures are ignored to preserve idempotence.

            if ((++respondants == apiSubscriberSet.size() && collectiveStatus)
                    || (!collectiveStatus && !idempotent_flag)) {
                idempotent_flag = true;
                responseHandler.handle(json);
            }
        }
    }

    private class ResponseHandler implements Handler<JsonObject> {
        private HttpServerResponse response;
        private Handler<Void> handler;

        public ResponseHandler(HttpServerResponse response, Handler<Void> handler) {
            this.response = response;
            this.handler = handler;
        }

        @Override
        public void handle(JsonObject status) {
            if (status.getBoolean("status")) { //$NON-NLS-1$
                handler.handle((Void) null);
            } else {
                GenericError error = Json.decodeValue(status.getObject("error").toString(), //$NON-NLS-1$
                        GenericError.class);

                response.setStatusCode(error.getResponseCode());
                response.setStatusMessage(error.getMessage());
            }

            response.end();
        }
    }
}
