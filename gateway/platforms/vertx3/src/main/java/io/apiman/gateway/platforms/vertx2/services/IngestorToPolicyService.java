package io.apiman.gateway.platforms.vertx2.services;

import io.apiman.gateway.platforms.vertx2.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx2.services.impl.IngestorToPolicyImpl;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Anything that goes from an ingestor (e.g. HTTP) to policy verticle
 *
 * HTTP <=> PolicyVerticle
 *
 * @author Marc Savy <msavy@redhat.com>
 */

@ProxyGen
@VertxGen
public interface IngestorToPolicyService {

    static IngestorToPolicyService create(Vertx vertx) {
      return new IngestorToPolicyImpl(vertx);
    }

    static IngestorToPolicyService createProxy(Vertx vertx, String address) {
      return ProxyHelper.createProxy(IngestorToPolicyService.class, vertx, address);
    }

    /**
     * Write a serviceRequest (head)
     *
     * @param serviceRequest the service request
     * @param readyHandler when ready to transmit body
     */
    void head(VertxServiceRequest serviceRequest,
            Handler<AsyncResult<Boolean>> readyHandler);

    /**
     * Write a body chunks
     *
     * @param chunk the body chunk
     */
    // TODO change this when https://github.com/vert-x3/vertx-service-proxy/issues/17
    void write(String chunk);

    /**
     * Finished transmitting body chunks
     * @param resultHandler the result handler
     */
    @ProxyClose
    void end(Handler<AsyncResult<Void>> resultHandler);
 }
