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
package io.apiman.gateway.platforms.vertx3.services.impl;

import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx3.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx3.services.IngestorToPolicyService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.UUID;

/**
 * Data sent from a Gateway and received at a Policy verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class IngestorToPolicyImpl implements IngestorToPolicyService {

    private String uuid = UUID.randomUUID().toString();
    private Handler<VertxServiceRequest> headHandler;
    private Handler<VertxApimanBuffer> bodyHandler;
    private Handler<Void> endHandler;
    private Handler<AsyncResult<Boolean>> readyHandler;
    private Handler<AsyncResult<Void>> resultHandler;

    public IngestorToPolicyImpl(Vertx vertx) {
        System.out.println("Creating IngestorToPolicyImpl");
    }

    @Override
    public void head(VertxServiceRequest serviceRequest,
            Handler<AsyncResult<Boolean>> readyHandler) {
        System.out.println("Received head: " + serviceRequest);

        this.readyHandler = readyHandler;
        headHandler.handle(serviceRequest);
    }

    @Override
    public void write(String chunk) {
        System.out.println("Received chunk " + chunk + " // on UUID " + uuid);

        if (bodyHandler != null)
            bodyHandler.handle(new VertxApimanBuffer(chunk)); //TODO this should be fixed when custom marshallers allowed
    }

    @Override
    public void end(Handler<AsyncResult<Void>> resultHandler) {
        System.out.println("OK, finished IngestorToPolicyImpl");

        this.resultHandler = resultHandler;
        if (endHandler != null)
            endHandler.handle((Void) null);
    }

    public void headHandler(Handler<VertxServiceRequest> handler) {
        this.headHandler = handler;
    }

    public void bodyHandler(Handler<VertxApimanBuffer> handler) {
        this.bodyHandler = handler;
    }

    public void endHandler(Handler<Void> handler) {
        this.endHandler = handler;
    }

    public void ready() {
        System.out.println("indicated ready (on head) in IngestorToPolicy");
        readyHandler.handle(Future.succeededFuture(true));
    }

    public void failHead() {
        System.out.println("Indicated FailedHead in IngestorToPolicy");
        readyHandler.handle(Future.succeededFuture(false));
    }

    public void fail(Throwable error) {
        System.out.println("Indicated Failure on result in INgestorToPolicy");
        resultHandler.handle(Future.failedFuture(error));
    }

    public void succeeded() {
        System.out.println("Indicated succeeded on result in IngestorToPolicy");
        resultHandler.handle(Future.succeededFuture());
    }
}
