package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.UUID;

/**
 * This is what gets called *after* the proxy. (PolicyVerticle)
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class IngestorToPolicyImpl implements IngestorToPolicyService {

    private String uuid = UUID.randomUUID().toString();
    private Vertx vertx;
    private String channelId;
    private PolicyToIngestorService service2;

    public IngestorToPolicyImpl(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void head(VertxServiceRequest serviceRequest,
            Handler<AsyncResult<Void>> readyHandler) {
        System.out.println("Head received on IngestorToPolicyImpl // " + serviceRequest);
        readyHandler.handle(Future.succeededFuture());
    }

    @Override
    public void write(String chunk) {
        System.out.println("Received chunk " + chunk + " // on UUID " + uuid);
    }

    @Override
    public void end() {
        System.out.println("OK, finished");
    }
}
