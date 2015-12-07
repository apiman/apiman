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
package io.apiman.gateway.platforms.vertx3.services;

import io.apiman.gateway.platforms.vertx3.io.VertxApiRequest;
import io.apiman.gateway.platforms.vertx3.services.impl.IngestorToPolicyImpl;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * From gateway to a policy verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@ProxyGen
@VertxGen
public interface IngestorToPolicyService {

    static IngestorToPolicyService create(Vertx vertx) {
      return new IngestorToPolicyImpl(vertx);
    }

    static IngestorToPolicyService createProxy(Vertx vertx, String address) {
      return ProxyHelper.createProxy(IngestorToPolicyService.class, vertx, address);
    }

    /**
     * Write a apiRequest (head)
     *
     * @param apiRequest the api request
     * @param readyHandler when ready to transmit body
     */
    void head(VertxApiRequest apiRequest,
            Handler<AsyncResult<Boolean>> readyHandler);

    /**
     * Write a body chunks
     *
     * @param chunk the body chunk
     */
    // TODO change this when https://github.com/vert-x3/vertx-service-proxy/issues/17
    void write(String chunk);

    /**
     * Finished transmitting body chunks
     * @param resultHandler the result handler
     */
    @ProxyClose
    void end(Handler<AsyncResult<Void>> resultHandler);
 }
