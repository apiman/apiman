package io.apiman.gateway.platforms.vertx2.services.impl;

import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
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
    private Handler<VertxServiceRequest> headHandler;
    private Handler<VertxApimanBuffer> bodyHandler;
    private Handler<Void> endHandler;
    private Handler<AsyncResult<Boolean>> readyHandler;
    private Handler<AsyncResult<Void>> resultHandler;

    public IngestorToPolicyImpl(Vertx vertx) {
        System.out.println("Creating IngestorToPolicyImpl");
    }

    @Override
    public void head(VertxServiceRequest serviceRequest,
            Handler<AsyncResult<Boolean>> readyHandler) {
        System.out.println("Received head:");

        this.readyHandler = readyHandler;
        headHandler.handle(serviceRequest);
    }

    @Override
    public void write(String chunk) {
        System.out.println("Received chunk " + chunk + " // on UUID " + uuid);

        if (bodyHandler != null)
            bodyHandler.handle(new VertxApimanBuffer(chunk)); //TODO this should be fixed when custom marshallers allowed
    }

    @Override
    public void end(Handler<AsyncResult<Void>> resultHandler) {
        System.out.println("OK, finished IngestorToPolicyImpl");

        this.resultHandler = resultHandler;

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
        System.out.println("indicated ready (on head) in IngestorToPolicy");
        readyHandler.handle(Future.succeededFuture(true));
    }

    public void failHead() {
        System.out.println("Indicated FailedHead in IngestorToPolicy");
        readyHandler.handle(Future.succeededFuture(false));
    }

    public void fail(Throwable error) {
        System.out.println("Indicated Failure on result in INgestorToPolicy");
        resultHandler.handle(Future.failedFuture(error));
    }

    public void succeeded() {
        System.out.println("Indicated succeeded on result in IngestorToPolicy");
        resultHandler.handle(Future.succeededFuture());
    }
}
