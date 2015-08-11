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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.UUID;

/**
 * Data sent from a Gateway and received at a Policy verticle
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class IngestorToPolicyImpl implements IngestorToPolicyService {

    private Handler<VertxServiceRequest> headHandler;
    private Handler<VertxApimanBuffer> bodyHandler;
    private Handler<Void> endHandler;
    private Handler<AsyncResult<Boolean>> readyHandler;
    private Handler<AsyncResult<Void>> resultHandler;
    private Logger log = LoggerFactory.getLogger(IngestorToPolicyImpl.class);
    private String uuid = UUID.randomUUID().toString();

    public IngestorToPolicyImpl(Vertx vertx) {
        log.debug("Creating IngestorToPolicyImpl " + uuid);
    }

    @Override
    public void head(VertxServiceRequest serviceRequest,
            Handler<AsyncResult<Boolean>> readyHandler) {
        log.debug(String.format("%s received ServiceRequest %s", uuid, serviceRequest));
        this.readyHandler = readyHandler;
        headHandler.handle(serviceRequest);
    }

    @Override
    public void write(String chunk) {
        log.debug(String.format("%s received chunk of size %s", uuid, chunk.length()));
        if (bodyHandler != null)
            bodyHandler.handle(new VertxApimanBuffer(chunk)); //TODO this should be fixed when custom marshallers allowed
    }

    @Override
    public void end(Handler<AsyncResult<Void>> resultHandler) {
        log.debug(uuid + " ended");
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
        log.debug(String.format("%s indicated #ready", uuid));
        readyHandler.handle(Future.succeededFuture(true));
    }

    public void failHead() {
        log.debug(String.format("%s indicated #failHead", uuid));
        readyHandler.handle(Future.succeededFuture(false));
    }

    public void fail(Throwable error) {
        log.debug(String.format("%s indicated #fail", uuid));
        resultHandler.handle(Future.failedFuture(error));
    }

    public void succeeded() {
        log.debug(String.format("%s indicated #succeeded",uuid));
        resultHandler.handle(Future.succeededFuture());
    }
}
