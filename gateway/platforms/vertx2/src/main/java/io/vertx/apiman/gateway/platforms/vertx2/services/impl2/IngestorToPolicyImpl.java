package io.vertx.apiman.gateway.platforms.vertx2.services.impl2;

import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
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
@SuppressWarnings("nls")
public class IngestorToPolicyImpl implements IngestorToPolicyService {

    private String uuid = UUID.randomUUID().toString();
    //private Vertx vertx;

    private Handler<VertxServiceRequest> headHandler;
    private Handler<VertxApimanBuffer> bodyHandler;
    private Handler<Void> endHandler;
    private Handler<AsyncResult<Void>> readyHandler;

    public IngestorToPolicyImpl(Vertx vertx) {
        //this.vertx = vertx;
        System.out.println("Creating IngestorToPolicyImpl");
    }

    @Override
    public void head(VertxServiceRequest serviceRequest,
            Handler<AsyncResult<Void>> readyHandler) {
        System.out.println("Received head");
        this.readyHandler = readyHandler;
        headHandler.handle(serviceRequest);
    }

    @Override
    public void write(String chunk) {
        System.out.println("Received chunk " + chunk + " // on UUID " + uuid);
        bodyHandler.handle(new VertxApimanBuffer(chunk)); //TODO this should be fixed when custom marshallers allowed
    }

    @Override
    public void end() {
        System.out.println("OK, finished");
        endHandler.handle((Void) null);
    }

    public void headHandler(Handler<VertxServiceRequest> handler) {
        this.headHandler = handler;
    }

    public void bodyHandler(Handler<VertxApimanBuffer> handler) {
        this.bodyHandler = handler;
    }

    public void endHandler(Handler<Void> handler) {
        this.endHandler = handler;
    }

    public void ready() {
        readyHandler.handle(Future.succeededFuture());
    }

    public void fail(Throwable throwable) {
        readyHandler.handle(Future.failedFuture(throwable));
    }
}
