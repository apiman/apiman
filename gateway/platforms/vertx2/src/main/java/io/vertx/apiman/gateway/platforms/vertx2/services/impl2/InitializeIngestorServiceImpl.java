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
package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.ISignalWriteStream;
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.serviceproxy.ProxyHelper;

public class InitializeIngestorServiceImpl implements InitializeIngestorService {

    private Vertx vertx;
    private VertxEngineConfig engineConfig;
    private IEngine engine;
    private Logger log;

    public InitializeIngestorServiceImpl(Vertx vertx,
            VertxEngineConfig engineConfig,
            IEngine engine,
            Logger log) {
        this.vertx = vertx;
        this.engineConfig = engineConfig;
        this.engine = engine;
        this.log = log;
    }

    @Override
    public void createIngestor(String uuid, Handler<AsyncResult<IngestorToPolicyService>> resultHandler) {
        System.out.println("Creating ingestor who will listen on " + uuid);

        IngestorToPolicyImpl service = new IngestorToPolicyImpl(vertx);

        ProxyHelper.registerService(IngestorToPolicyService.class,
                vertx, service, uuid);

        PolicyToIngestorService replyProxy = PolicyToIngestorService.createProxy(vertx, uuid + ".response");

        execute(service, replyProxy, resultHandler);

        // Open up a IngestorToPolicy service
        resultHandler.handle(Future.succeededFuture(service));
        //service.ready();
    }

    // Do as class?
    private void execute(IngestorToPolicyImpl requestService, PolicyToIngestorService replyProxy, Handler<AsyncResult<IngestorToPolicyService>> resultHandler) {

        System.out.println("Setting head handler");
        requestService.headHandler((Handler<VertxServiceRequest>) serviceRequest -> {

            System.out.println("Head has arrived....");

            final IServiceRequestExecutor requestExecutor = engine.executor(serviceRequest,
                    (IAsyncResultHandler<IEngineResult>) result -> {

                log.debug("Received result from apiman engine in PolicyVerticle!"); //$NON-NLS-1$

                if (result.isSuccess()) {
                    IEngineResult engineResult = result.getResult();

                    if (engineResult.isResponse()) {
                        doResponse(engineResult, replyProxy);
                    } else {
                        //responseExecutor.failure(engineResult.getPolicyFailure());
                        //resultHandler.handle(Future.failedFuture(engineResult.get));
                        System.out.println("Failed with policy denial");
                    }

                } else {
                    //responseExecutor.error(result.getError());
                    System.out.println("Failed with exception");
                    System.out.println(result.getError().getMessage());
                    replyProxy.end();
                }
            });

            requestExecutor.streamHandler((IAsyncHandler<ISignalWriteStream>) writeStream -> {
                requestService.bodyHandler((Handler<VertxApimanBuffer>) body -> {
                    writeStream.write(body);
                });

                requestService.endHandler((Handler<Void>) v -> {
                    writeStream.end();
                });
            });

            requestExecutor.execute();
        });
    }

    // Do as separate class?
    private void doResponse(IEngineResult engineResult, PolicyToIngestorService replyProxy) {

        VertxServiceResponse serviceResponse = new VertxServiceResponse(engineResult.getHead());

        replyProxy.head(serviceResponse, (Handler<AsyncResult<Void>>) ready -> {

        });

        engineResult.bodyHandler((IAsyncHandler<IApimanBuffer>) chunk -> {
            replyProxy.write(((Buffer) chunk.getNativeBuffer()).toString("utf-8")); // TODO change
        });

        engineResult.endHandler((IAsyncHandler<Void>) v -> {
            replyProxy.end();
        });
    }
}
