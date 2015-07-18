package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

public class InitializeIngestorServiceImpl implements InitializeIngestorService {

    private Vertx vertx;

    public InitializeIngestorServiceImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void createIngestor(String uuid, Handler<AsyncResult<IngestorToPolicyService>> resultHandler) {
        System.out.println("Creating ingestor who will listen on " + uuid);

        IngestorToPolicyImpl service = new IngestorToPolicyImpl(vertx);

        ProxyHelper.registerService(IngestorToPolicyService.class,
                vertx, service, uuid + ".request");

        PolicyToIngestorService proxy = PolicyToIngestorService.createProxy(vertx, uuid + ".response");

        proxy.write("test-response");

        resultHandler.handle(Future.succeededFuture(service));
    }
}
