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

import io.apiman.gateway.platforms.vertx3.io.VertxApiResponse;
import io.apiman.gateway.platforms.vertx3.io.VertxPolicyFailure;
import io.apiman.gateway.platforms.vertx3.services.impl.PolicyToIngestorServiceImpl;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Anything that goes from an ingestor (e.g. HTTP) to policy verticle
 *
 * HTTP <=> PolicyVerticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@ProxyGen
@VertxGen
public interface PolicyToIngestorService {

    static PolicyToIngestorService create(Vertx vertx) {
      return new PolicyToIngestorServiceImpl();
    }

    static PolicyToIngestorService createProxy(Vertx vertx, String address) {
      return ProxyHelper.createProxy(PolicyToIngestorService.class, vertx, address);
    }

    /**
     * Write a apiRequest (head)
     *
     * @param apiResponse the service request
     * @param readyHandler when ready to transmit body
     */
    void head(VertxApiResponse apiResponse, Handler<AsyncResult<Void>> readyHandler);

    /**
     * Write a body chunks
     *
     * @param chunk the body chunk
     */
    // TODO change this when https://github.com/vert-x3/vertx-service-proxy/issues/17
    void write(String chunk);

    /**
     * Finished all actions.
     * @param resultHandler the result handler
     */
    @ProxyClose
    void end(Handler<AsyncResult<Void>> resultHandler);

    /**
     * Indicate failure
     * @param policyFailure the policy failure
     */
    void policyFailure(VertxPolicyFailure policyFailure);
}