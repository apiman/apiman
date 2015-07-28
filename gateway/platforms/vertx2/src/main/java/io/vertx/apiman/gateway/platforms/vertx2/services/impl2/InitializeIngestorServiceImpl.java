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
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.serviceproxy.ProxyHelper;

@SuppressWarnings("nls")
public class InitializeIngestorServiceImpl implements InitializeIngestorService {

    private Vertx vertx;
    private IEngine engine;
    private Logger log;

    public InitializeIngestorServiceImpl(Vertx vertx,
            VertxEngineConfig engineConfig,
            IEngine engine,
            Logger log) {
        this.vertx = vertx;
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

        PolicyExecutor executor = new PolicyExecutor(engine, service, replyProxy, log);
        executor.execute();

        // Open up a IngestorToPolicy service
        System.out.println("Called Future.succededFuture(service)");
        resultHandler.handle(Future.succeededFuture(service));
    }

//    public void setPolicyConnectionHandler(Handler<>) { could pass this out to the policy verticle
//
//    }
}
