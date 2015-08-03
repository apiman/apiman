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

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx2.io.VertxPolicyFailure;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceResponse;
import io.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;

/**
 * Execute policy
 *
 * @author Marc Savy <msavy@redhat.com>
 */
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
            System.out.println("Head has arrived....");

            final IServiceRequestExecutor requestExecutor = engine.executor(serviceRequest, (IAsyncResultHandler<IEngineResult>) result -> {
                log.debug("Received result from apiman engine in PolicyVerticle!"); //$NON-NLS-1$

                if (result.isSuccess()) {
                    IEngineResult engineResult = result.getResult();

                    if (engineResult.isResponse()) {
                        doResponse(engineResult, replyProxy);
                        requestService.succeeded(); // no exception
                    } else {
                        System.out.println("Failed with policy denial - setting end handler");
                        requestService.endHandler((Handler<Void>) v -> {
                            System.out.println("requestService.endHandler called");
                            replyEnd();
                            requestService.succeeded();
                        });
                        replyProxy.policyFailure(new VertxPolicyFailure(engineResult.getPolicyFailure()));
                        requestService.failHead();
                        requestService.succeeded();
                    }
                } else {
                    // Necessary to fail head to ensure #end is called. Could refactor this to call end ourselves, possibly.
                    requestService.failHead();
                    requestService.endHandler((Handler<Void>) v -> {
                        requestService.fail(result.getError());
                        replyEnd();
                    });
                }
            });

            requestExecutor.streamHandler((IAsyncHandler<ISignalWriteStream>) writeStream -> {
                System.out.println("In streamhandler");

                requestService.bodyHandler((Handler<VertxApimanBuffer>) body -> {
                    writeStream.write(body);
                });

                requestService.endHandler((Handler<Void>) v -> {
                    System.out.println("requestService.endHandler handle");
                    writeStream.end();
                });

                requestService.ready();
            });

            requestExecutor.execute();
        });
    }

    private void doResponse(IEngineResult engineResult, PolicyToIngestorService replyProxy) {
        VertxServiceResponse serviceResponse = new VertxServiceResponse(engineResult.getHead());

        replyProxy.head(serviceResponse, (Handler<AsyncResult<Void>>) ready -> {
            System.out.println("Acking head response");
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
                log.error("Was unable to respond "); // TODO
            } else {
                System.out.println("Called end on replyProxy");
            }
        });
    }
}
