package io.apiman.gateway.platforms.vertx3.http;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;

public class HttpSpawner implements Handler<HttpServerRequest> {

    private Vertx vertx;
    private Logger log;
    private boolean transportSecure;

    public HttpSpawner(Vertx vertx,
            Logger log,
            boolean transportSecure) {
        this.vertx = vertx;
        this.log = log;
        this.transportSecure = transportSecure;

    }

    @Override
    public void handle(HttpServerRequest event) {
        new HttpExecutor(vertx, log, transportSecure).handle(event);
    }

}
