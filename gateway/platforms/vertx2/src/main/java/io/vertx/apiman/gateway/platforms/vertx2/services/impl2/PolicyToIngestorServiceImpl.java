package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class PolicyToIngestorServiceImpl implements PolicyToIngestorService {

    private String uuid;

    public PolicyToIngestorServiceImpl() {
        this.uuid = "123-not-really-uuid";
    }

    @Override
    public void head(VertxServiceResponse serviceResponse, Handler<AsyncResult<Void>> readyHandler) {
        System.out.println("Head on PolicyToIngestorServiceImpl // " + serviceResponse);
        readyHandler.handle(Future.succeededFuture((Void) null));
    }

    @Override
    public void write(String chunk) {
        System.out.println("PolicyToIngestorServiceImpl Received chunk " + chunk + " // on UUID " + uuid);
    }

    @Override
    public void end() {
        System.out.println("OK, finished");
    }
}
