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

import io.apiman.gateway.vertx.config.RouteMapper;

import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Dispatch incoming HTTP requests to the appropriate verticle. For instance, gateway requests to one place,
 * and api requests another.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpDispatcherVerticle extends ApimanVerticleBase {
    public static String VERTICLE_NAME = "http-dispatcher"; //$NON-NLS-1$

    private RouteMapper routeMapper;

    @Override
    public void start() {
        super.start();

        routeMapper = amanConfig.getRouteMap();
        logger.debug("Proxied routes " + routeMapper.getRoutes()); //$NON-NLS-1$

        createDispatcher(routeMapper);
    }

    protected void createDispatcher(final RouteMapper routeMapper) {
        final HttpServer httpServer = vertx.createHttpServer();

        httpServer.requestHandler(new Handler<HttpServerRequest>() {

            @Override
            // Proxy initial request (iRequest -> pRequest)
            public void handle(final HttpServerRequest iRequest) {

                Integer port = routeMapper.getAddress(iRequest.path());

                if (port == null) {
                    handleNotFound(iRequest);
                } else {
                    handleRequest(iRequest, port);
                }
            }
        });

        httpServer.listen(amanConfig.getRouteMap().getAddress(verticleType()), amanConfig.hostname());
    }

    private void handleRequest(final HttpServerRequest iRequest, int port) {
        final HttpClient httpClient = vertx.createHttpClient().setPort(port);

        final HttpClientRequest pRequest = httpClient.request(iRequest.method(), iRequest.uri(),
                new Handler<HttpClientResponse>() {

            @Override
            // Proxy response
            public void handle(HttpClientResponse pResponse) {
                iRequest.response().setStatusCode(pResponse.statusCode());
                iRequest.response().headers().set(pResponse.headers());
                iRequest.response().setChunked(true);

                // Pack the response
                pResponse.dataHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer contents) {
                        iRequest.response().write(contents);
                    }
                });

                pResponse.endHandler(new VoidHandler() {
                    @Override
                    public void handle() {
                        iRequest.response().end();
                    }
                });
            }
        });

        pRequest.headers().set(iRequest.headers());
        pRequest.setChunked(true);

        iRequest.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer data) {
                pRequest.write(data);
            }
        });

        iRequest.endHandler(new VoidHandler() {
            @Override
            public void handle() {
                pRequest.end();
            }
        });
    }

    private void handleNotFound(final HttpServerRequest iRequest) {
        iRequest.response().setStatusCode(404);
        iRequest.response().end();
    }

    @Override
    public String verticleType() {
        return VERTICLE_NAME;
    }
}
