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

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;

import java.util.Collection;

import org.overlord.apiman.rt.engine.EngineConfig;
import org.overlord.apiman.rt.engine.EngineFactory;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * An implementation of the API Management Gateway in Undertow.
 *
 * @author eric.wittmann@redhat.com
 */
public class UndertowGateway {
    
    private IEngine engine;
    private UndertowGatewayServer server;
    
    /**
     * Constructor.
     */
    public UndertowGateway() {
    }

    /**
     * Starts the gateway.
     */
    public void start() {
        engine = EngineFactory.createEngine();
        server = new UndertowGatewayServer(EngineConfig.getServerPort()) {
            @Override
            protected void doGateway(final HttpServerExchange exchange) {
                ServiceRequest request = readRequest(exchange);
                try {
                    // TODO recent engine interface changes broke this - fix!
//                    ServiceResponse response = engine.execute(request);
//                    writeResponse(exchange, response);
                } catch (Exception e) {
                    writeError(exchange, e);
                }
            }
        };
        server.start();
    }

    /**
     * Stopes the gateway.
     */
    public void stop() {
        server.stop();
        engine = null;
        server = null;
    }

    /**
     * Reads a {@link ServiceRequest} from information found in the inbound
     * portion of the http exchange.
     * @param exchange the undertow http server exchange
     * @return a valid {@link ServiceRequest}
     */
    protected ServiceRequest readRequest(HttpServerExchange exchange) {
        // TODO get the service request from a pool (re-use these objects)
        ServiceRequest request = new ServiceRequest();
        request.setOrganization(getOrganization(exchange));
        request.setService(getService(exchange));
        request.setVersion(getVersion(exchange));
        request.setApiKey(getApiKey(exchange));
        request.setType(exchange.getRequestMethod().toString());
        request.setDestination(getDestination(exchange));
        readHeaders(request, exchange);
        request.setRawRequest(exchange);
        return request;
    }

    /**
     * @param exchange
     * @return
     */
    protected String getOrganization(HttpServerExchange exchange) {
        String path = exchange.getRequestPath();
        return path.split("/")[1]; //$NON-NLS-1$
    }

    /**
     * @param exchange
     * @return
     */
    protected String getService(HttpServerExchange exchange) {
        String path = exchange.getRequestPath();
        return path.split("/")[2]; //$NON-NLS-1$
    }

    /**
     * @param exchange
     * @return
     */
    protected String getVersion(HttpServerExchange exchange) {
        String path = exchange.getRequestPath();
        return path.split("/")[3]; //$NON-NLS-1$
    }

    /**
     * @param exchange
     * @return
     */
    protected String getApiKey(HttpServerExchange exchange) {
        return exchange.getRequestHeaders().getFirst("X-API-Key"); //$NON-NLS-1$
    }

    /**
     * @param exchange
     * @return
     */
    protected String getDestination(HttpServerExchange exchange) {
        // Format:  /org/svc/version/dest/in/a/tion
        String path = exchange.getRequestPath();
        int idx = -1;
        for (int i=0; i<4; i++) {
            idx = path.indexOf('/', idx+1);
        }
        return path.substring(idx);
    }

    /**
     * Reads the inbound request headers from the exchange and sets them on
     * the {@link ServiceRequest}.
     * @param request
     * @param exchange
     */
    protected void readHeaders(ServiceRequest request, HttpServerExchange exchange) {
        HeaderMap headers = exchange.getRequestHeaders();
        Collection<HttpString> names = headers.getHeaderNames();
        for (HttpString headerName : names) {
            String headerValue = headers.getFirst(headerName);
            request.getHeaders().put(headerName.toString(), headerValue);
        }
    }

    /**
     * @param exchange
     * @param response
     */
    protected void writeResponse(HttpServerExchange exchange, ServiceResponse response) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param exchange
     * @param e
     */
    protected void writeError(HttpServerExchange exchange, Exception e) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Main entry point for the Undertow Gateway.
     * @param args
     */
    public static final void main(String [] args) {
        UndertowGateway gateway = new UndertowGateway();
        gateway.start();
        synchronized (gateway) {
            for (;;) {
                try {
                    gateway.wait();
                    return;
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
