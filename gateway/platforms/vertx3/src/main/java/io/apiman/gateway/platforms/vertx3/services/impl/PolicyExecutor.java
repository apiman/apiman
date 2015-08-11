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

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.vertx3.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx3.io.VertxPolicyFailure;
import io.apiman.gateway.platforms.vertx3.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx3.io.VertxServiceResponse;
import io.apiman.gateway.platforms.vertx3.services.PolicyToIngestorService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;

/**
 * Execute policy
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class PolicyExecutor {
    private IngestorToPolicyImpl requestService;
    private PolicyToIngestorService replyProxy;
    private Logger log;
    private IEngine engine;

    public PolicyExecutor(IEngine engine, IngestorToPolicyImpl requestService,
            PolicyToIngestorService replyProxy, Logger log) {
        this.engine = engine;
        this.requestService = requestService;
        this.replyProxy = replyProxy;
        this.log = log;
    }

    public void execute() {
        requestService.headHandler((Handler<VertxServiceRequest>) serviceRequest -> {

            final IServiceRequestExecutor requestExecutor = engine.executor(serviceRequest, (IAsyncResultHandler<IEngineResult>) result -> {
                log.debug(String.format("Received result from apiman engine in PolicyVerticle. Request: %d Result Success?: %b",
                        serviceRequest.hashCode(), result.isSuccess()));

                if (result.isSuccess()) {
                    IEngineResult engineResult = result.getResult();

                    if (engineResult.isResponse()) {
                        doResponse(engineResult, replyProxy);
                        requestService.succeeded(); // no exception
                    } else {
                        log.debug(String.format("Failed with policy denial; setting end handler. Request: %d", serviceRequest.hashCode()));

                        requestService.endHandler((Handler<Void>) v -> {
                            replyEnd();
                            requestService.succeeded();
                        });

                        replyProxy.policyFailure(new VertxPolicyFailure(engineResult.getPolicyFailure()));
                        requestService.failHead();
                    }
                } else {
                    // Necessary to fail head to ensure #end is called. Could refactor this to call end ourselves, possibly.
                    log.debug(String.format("An exception occurred. Request: %d Error: %s ",
                            serviceRequest.hashCode(), result.getError().getMessage()));

                    requestService.endHandler((Handler<Void>) v -> {
                        requestService.fail(result.getError());
                        replyEnd();
                    });

                    requestService.failHead();
                }
            });

            requestExecutor.streamHandler((IAsyncHandler<ISignalWriteStream>) writeStream -> {
                requestService.bodyHandler((Handler<VertxApimanBuffer>) body -> {
                    writeStream.write(body);
                });

                requestService.endHandler((Handler<Void>) v -> {
                    writeStream.end();
                });

                requestService.ready();
            });

            log.debug("Calling RequestExecutor#execute on " + serviceRequest.hashCode());
            requestExecutor.execute();
        });
    }

    private void doResponse(IEngineResult engineResult, PolicyToIngestorService replyProxy) {
        VertxServiceResponse serviceResponse = new VertxServiceResponse(engineResult.getHead());

        replyProxy.head(serviceResponse, (Handler<AsyncResult<Void>>) result -> {
            if (result.failed())
                log.error("Head send from Proxy to Ingestor failed", result.cause());
        });

        engineResult.bodyHandler((IAsyncHandler<IApimanBuffer>) chunk -> {
            replyProxy.write(((Buffer) chunk.getNativeBuffer()).toString("UTF-8")); // TODO change when marshaller available
        });

        engineResult.endHandler((IAsyncHandler<Void>) v -> {
            replyEnd();
        });
    }

    private void replyEnd() {
        replyProxy.end((Handler<AsyncResult<Void>>) result -> {
            if (result.failed()) {
                log.error("Was unable to respond"); // TODO
            } else {
                log.debug("Called end on replyProxy");
            }
        });
    }
}
