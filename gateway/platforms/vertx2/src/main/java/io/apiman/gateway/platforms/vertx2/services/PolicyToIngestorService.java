package io.apiman.gateway.platforms.vertx2.services;

import io.apiman.gateway.platforms.vertx2.io.VertxPolicyFailure;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceResponse;
import io.apiman.gateway.platforms.vertx2.services.impl.PolicyToIngestorServiceImpl;
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
public interface PolicyToIngestorService {

    static PolicyToIngestorService create(Vertx vertx) {
      return new PolicyToIngestorServiceImpl();
    }

    static PolicyToIngestorService createProxy(Vertx vertx, String address) {
      return ProxyHelper.createProxy(PolicyToIngestorService.class, vertx, address);
    }

    /**
     * Write a serviceRequest (head)
     *
     * @param serviceResponse the service request
     * @param readyHandler when ready to transmit body
     */
    void head(VertxServiceResponse serviceResponse, Handler<AsyncResult<Void>> readyHandler);

    /**
     * Write a body chunks
     *
     * @param chunk the body chunk
     */
    // TODO change this when https://github.com/vert-x3/vertx-service-proxy/issues/17
    void write(String chunk);

    /**
     * Finished all actions.
     * @param resultHandler the result handler
     */
    @ProxyClose
    void end(Handler<AsyncResult<Void>> resultHandler);

    /**
     * Indicate failure
     * @param policyFailure the policy failure
     */
    void policyFailure(VertxPolicyFailure policyFailure);
}