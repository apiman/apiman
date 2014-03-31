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
package org.overlord.apiman.rt.test.echo;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.jackson.map.ObjectMapper;


/**
 * A very simple echo server used during testing as the back-end service
 * for all published managed services.
 *
 * @author eric.wittmann@redhat.com
 */
public class EchoServer {
    
    private Undertow server;
    private ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Constructor.
     */
    public EchoServer(int port) {
        server = Undertow.builder()
                .addHttpListener(port, "localhost") //$NON-NLS-1$
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        doEchoResponse(exchange);
                    }
                }).build();
    }

    /**
     * Responds with a comprehensive echo.  This means bundling up all the
     * information about the inbound request into a java bean and responding
     * with that data as a JSON response.
     * @param exchange
     */
    protected void doEchoResponse(HttpServerExchange exchange) {
        EchoResponse response = EchoResponse.from(exchange);
        
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, response);
        } catch (Exception e) {
            e.printStackTrace(new PrintWriter(writer));
            throw new RuntimeException(e);
        }
        
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json"); //$NON-NLS-1$
        exchange.getResponseSender().send(writer.getBuffer().toString());
    }

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
