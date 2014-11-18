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

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.vertx.config.RouteMapper;
import io.apiman.gateway.vertx.http.HttpGatewayStreamerMultiplexer;

/**
 * The main HTTP gateway verticle which translates HTTP requests into the {@link ServiceRequest} and sends the
 * {@link ServiceResponse} back to the {@link HttpDispatcherVerticle}.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class HttpGatewayVerticle extends ApimanVerticleBase {
    
    private HttpGatewayStreamerMultiplexer streamMultiplexer; 
    private RouteMapper routeMap;

    @Override
    public void start() {
        super.start();
        
        streamMultiplexer = new HttpGatewayStreamerMultiplexer(vertx, container, verticleType());
        routeMap = amanConfig.getRouteMap();
        
        listen();
    }

    void listen() {
        vertx.createHttpServer().
            requestHandler(streamMultiplexer).
            listen(routeMap.getAddress(verticleType()), amanConfig.hostname());
    }

    @Override
    public String verticleType() {
        return "http-gateway";
    }
}
