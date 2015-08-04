package io.apiman.gateway.platforms.vertx2.services;

import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.services.impl.InitializeIngestorServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.serviceproxy.ProxyHelper;

@ProxyGen
public interface InitializeIngestorService {

    void createIngestor(String uuid, Handler<AsyncResult<IngestorToPolicyService>> resultHandler);

    static InitializeIngestorService create(Vertx vertx,
            VertxEngineConfig engineConfig,
            IEngine engine,
            Logger log) {
        return new InitializeIngestorServiceImpl(vertx, engineConfig, engine, log);
    }

    static InitializeIngestorService createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(InitializeIngestorService.class, vertx, address);
    }
}
