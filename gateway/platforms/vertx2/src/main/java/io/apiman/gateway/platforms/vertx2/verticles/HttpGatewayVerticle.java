package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.http.HttpServiceFactory;
import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceResponse;
import io.vertx.apiman.gateway.platforms.vertx2.services.impl2.PolicyToIngestorServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("nls")
public class HttpGatewayVerticle extends ApimanVerticleBase {

    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding");
        SUPPRESSED_HEADERS.add("Content-Length");
        SUPPRESSED_HEADERS.add("X-API-Key");
    }

    @Override
    public void start() {
        super.start();

        vertx.createHttpServer().requestHandler(req -> {
            // Pause the request, as we want to give explicit permission to transmit body chunks
            req.pause();

            // Unique ID for this request
            String httpSessionUuid = UUID.randomUUID().toString();

            // Create proxy in order to initialise a unique channel with a PolicyVerticle
            InitializeIngestorService initService = InitializeIngestorService.createProxy(vertx,
                            VertxEngineConfig.GATEWAY_ENDPOINT_POLICY_INGESTION);

            // Setup response leg first
            setupResponse(req.response(), httpSessionUuid);

            // The outer proxy returns an inner proxy, which is our request 'connection'
            initService.createIngestor(httpSessionUuid, (Handler<AsyncResult<IngestorToPolicyService>>) result -> {
                setupRequest(req, result, httpSessionUuid);
            });
        }).listen(8080);
    }

    // Setup request leg
    private void setupRequest(HttpServerRequest request, AsyncResult<IngestorToPolicyService> result,
            String httpSessionUuid) {
        log.debug("Setting up the request with " + httpSessionUuid);

        IngestorToPolicyService send = result.result();

        if (result.succeeded()) {

            VertxServiceRequest serviceRequest = HttpServiceFactory.buildRequest(request, false);

            send.head(serviceRequest, (Handler<AsyncResult<Void>>) ready -> {
                // Signalled that we can send the body.
                if (ready.succeeded()) {
                    System.out.println("Resuming");
                    request.resume();
                } else {
                    // TODO error on head
                }
            });

            request.handler((Handler<Buffer>) buffer -> {
                send.write(buffer.toString("UTF-8")); // TODO fixme when pluggable marshallers available...
            });

            request.endHandler((Handler<Void>) end -> {
               send.end();
            });

        } else {
            // Handle error
        }
    }

    // Set up our response channel first
    private void setupResponse(HttpServerResponse response, String httpSessionUuid) {
        PolicyToIngestorServiceImpl receive = new PolicyToIngestorServiceImpl();

        ProxyHelper.registerService(PolicyToIngestorService.class, vertx, receive,
                httpSessionUuid + ".response");

        receive.headHandler((Handler<VertxServiceResponse>) apimanResponse -> {
            HttpServiceFactory.buildResponse(response, apimanResponse);
        });

        receive.bodyHandler((Handler<VertxApimanBuffer>) buff -> {
            response.write((Buffer) buff.getNativeBuffer());
        });

        receive.endHandler((Handler<Void>) v -> {
            response.end();
        });
    }



    @Override
    public VerticleType verticleType() {
        return VerticleType.HTTP_GATEWAY;
    }
}