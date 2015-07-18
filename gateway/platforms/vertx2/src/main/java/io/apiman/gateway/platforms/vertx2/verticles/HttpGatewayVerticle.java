package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.http.HttpServiceFactory;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.apiman.gateway.platforms.vertx2.services.impl2.PolicyToIngestorServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.UUID;

@SuppressWarnings("nls")
public class HttpGatewayVerticle extends AbstractVerticle {

    @Override
    public void start() {

        vertx.createHttpServer().requestHandler(req -> {
            // Unique ID for this request
            String httpSessionUuid = UUID.randomUUID().toString();

            // Create proxy in order to initialise a unique channel with a PolicyVerticle
            InitializeIngestorService initService = InitializeIngestorService.createProxy(vertx,
                            VertxEngineConfig.GATEWAY_ENDPOINT_POLICY_INGESTION);

            // Set up our response channel first
            PolicyToIngestorService receive = new PolicyToIngestorServiceImpl();

            ProxyHelper.registerService(PolicyToIngestorService.class, vertx, receive,
                    httpSessionUuid + ".response");

            // Send stuff

            // The outer proxy returns an inner proxy, which is our 'connection' session.
            initService.createIngestor(httpSessionUuid +  ".request", (Handler<AsyncResult<IngestorToPolicyService>>) event -> {
                    IngestorToPolicyService send = event.result();

                    VertxServiceRequest serviceRequest = HttpServiceFactory.buildRequest(req,
                            "/foo",
                            false);

                    send.head(serviceRequest, (Handler<AsyncResult<Void>>) ready -> {
                        // Signalled that we can send the body.
                        if (ready.succeeded()) {
                            send.write(String.format("Hello1 from SendVerticle %s!", httpSessionUuid));
                        } else {
                            // TODO error on head
                        } // Close now, irrespective of failure.
                        send.end();
                    });
                });

            req.response().end();
        }).listen(8080);
    }
}