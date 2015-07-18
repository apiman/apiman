package io.vertx.apiman.gateway.platforms.vertx2.services;

import io.vertx.apiman.gateway.platforms.vertx2.services.impl2.InitializeIngestorServiceImpl;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

@ProxyGen
public interface InitializeIngestorService {

    void createIngestor(String uuid, Handler<AsyncResult<IngestorToPolicyService>> resultHandler);

    static InitializeIngestorService create(Vertx vertx) {
        return new InitializeIngestorServiceImpl(vertx);
    }

    static InitializeIngestorService createProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(InitializeIngestorService.class, vertx, address);
    }
}
