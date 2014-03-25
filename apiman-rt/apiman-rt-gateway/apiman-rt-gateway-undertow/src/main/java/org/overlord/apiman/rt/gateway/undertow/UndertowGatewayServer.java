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
package org.overlord.apiman.rt.gateway.undertow;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;



/**
 * A very simple echo server used during testing as the back-end service
 * for all published managed services.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class UndertowGatewayServer {

    private Undertow server;

    /**
     * Constructor.
     */
    public UndertowGatewayServer(int port) {
        server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        doGateway(exchange);
                    }
                }).build();
    }

    /**
     * @param exchange
     */
    protected abstract void doGateway(HttpServerExchange exchange);

    /**
     * Starts the server.
     */
    public void start() {
        server.start();
    }

    /**
     * Stops the server.
     */
    public void stop() {
        server.stop();
    }
    
}
