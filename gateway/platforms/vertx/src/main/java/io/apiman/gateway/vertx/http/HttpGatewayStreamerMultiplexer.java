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

import io.apiman.gateway.vertx.config.VertxEngineConfig;
import io.apiman.gateway.vertx.worker.ServiceWorkerQueue;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

/**
 * Handles each {@link HttpServerRequest} arriving and dispatches to a {@link HttpGatewayStreamer} worker to deal
 * with. Once a conversation completes, the worker is made available for subsequent work..
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpGatewayStreamerMultiplexer implements Handler<HttpServerRequest> {

    private Logger logger;
    private ServiceWorkerQueue workerQueue;

    public HttpGatewayStreamerMultiplexer(Vertx vertx, Container container, String stripString) {
        logger = container.logger();
        this.workerQueue = new ServiceWorkerQueue(vertx, container,
                VertxEngineConfig.APIMAN_RT_EP_GATEWAY_REG_POLICY, stripString);
    }

    @Override
    public void handle(final HttpServerRequest request) {
        // Get a gateway streamer from the queue (or create one).
        workerQueue.poll(new Handler<HttpGatewayStreamer>() {

            @Override
            public void handle(final HttpGatewayStreamer gatewayStreamer) {

                gatewayStreamer.endHandler(new Handler<Void>() {

                    @Override
                    public void handle(Void flag) {
                        logger.debug("Reinserted worker back into the queue (it has finished!)"); //$NON-NLS-1$
                        workerQueue.add(gatewayStreamer);
                    }
                });
                logger.debug("Got a request, let's do the business!"); //$NON-NLS-1$
                gatewayStreamer.handle(request);
            }
        });
    }
}
