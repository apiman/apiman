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
package io.apiman.gateway.platforms.vertx2.services.impl;

import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx2.io.VertxPolicyFailure;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceResponse;
import io.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.UUID;

/**
 * Data sent from a Policy verticle and received at a gateway.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class PolicyToIngestorServiceImpl implements PolicyToIngestorService {

    private String uuid = UUID.randomUUID().toString();
    private Handler<VertxServiceResponse> headHandler;
    private Handler<VertxApimanBuffer> bodyHandler;
    private Handler<Void> endHandler;
    private Handler<VertxPolicyFailure> policyFailureHandler;

    public PolicyToIngestorServiceImpl() {
        System.out.println("Creating PolicyToIngestorServiceImpl");
    }

    @Override
    public void head(VertxServiceResponse serviceResponse, Handler<AsyncResult<Void>> readyHandler) {
        System.out.println("Head on PolicyToIngestorServiceImpl // " + serviceResponse);
        headHandler.handle(serviceResponse);
        // Fire the ready handler

        System.out.println("Successful ack in readyHandler in PolicyToIngestor");
        readyHandler.handle(Future.succeededFuture((Void) null));
    }

    @Override
    public void write(String chunk) {
        System.out.println("PolicyToIngestorServiceImpl Received chunk " + chunk + " // on UUID " + uuid);
        bodyHandler.handle(new VertxApimanBuffer(chunk, "UTF-8")); // TODO fix when upstream ready
    }

    @Override
    public void end(Handler<AsyncResult<Void>> resultHandler) {
        System.out.println("Finished PolicyToIngestor");
        endHandler.handle((Void) null);

        System.out.println("Acking resultHandler in PolicyToIngestor");
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void policyFailure(VertxPolicyFailure policyFailure) {
        policyFailureHandler.handle(policyFailure);
    }

    public void policyFailureHandler(Handler<VertxPolicyFailure> policyFailureHandler) {
        this.policyFailureHandler = policyFailureHandler;
    }

    public void headHandler(Handler<VertxServiceResponse> handler) {
        this.headHandler = handler;
    }

    public void bodyHandler(Handler<VertxApimanBuffer> handler) {
        this.bodyHandler = handler;
    }

    public void endHandler(Handler<Void> handler) {
        this.endHandler = handler;
    }
}
