package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
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
    private Handler<VertxServiceResponse> headHandler;
    private Handler<VertxApimanBuffer> bodyHandler;
    private Handler<Void> endHandler;

    public PolicyToIngestorServiceImpl() {
        this.uuid = "123-not-really-uuid";
        System.out.println("Creating PolicyToIngestorServiceImpl");
    }

    @Override
    public void head(VertxServiceResponse serviceResponse, Handler<AsyncResult<Void>> readyHandler) {
        System.out.println("Head on PolicyToIngestorServiceImpl // " + serviceResponse);
        headHandler.handle(serviceResponse);
        // Fire the ready handler
        readyHandler.handle(Future.succeededFuture((Void) null));
    }

    @Override
    public void write(String chunk) {
        System.out.println("PolicyToIngestorServiceImpl Received chunk " + chunk + " // on UUID " + uuid);
        bodyHandler.handle(new VertxApimanBuffer(chunk, "utf-8")); // TODO fix when upstream ready
    }

    @Override
    public void end() {
        System.out.println("OK, finished");
        endHandler.handle((Void) null);
    }

    public void headHandler(Handler<VertxServiceResponse> handler) {
        this.headHandler = handler;
    }

    public void bodyHandler(Handler<VertxApimanBuffer> handler) {
        this.bodyHandler = handler;
    }

    public void endHandler(Handler<Void> handler) {
        this.endHandler = handler;
    }
}
