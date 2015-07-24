package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.http.HttpServiceFactory;
import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxPolicyFailure;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceRequest;
import io.vertx.apiman.gateway.platforms.vertx2.services.VertxServiceResponse;
import io.vertx.apiman.gateway.platforms.vertx2.services.impl2.PolicyToIngestorServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;

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
            req.response().setChunked(true);

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

            send.head(serviceRequest, (Handler<AsyncResult<Boolean>>) ready -> {
                // Signalled that we can send the body.
                if (ready.succeeded()) {
                    if (ready.result()) {
                        request.handler((Handler<Buffer>) buffer -> {
                            send.write(buffer.toString("UTF-8")); // TODO fixme when pluggable marshallers available...
                        });

                        request.endHandler((Handler<Void>) end -> {
                            // Finish *send* to Policy
                            send.end((Handler<AsyncResult<Void>>) sendResult -> {
                                setError(request.response(), sendResult.cause());
                            });
                        });

                        System.out.println("Resuming");
                        request.resume();
                    } else { // It didn't work. Just call end immediately.

                        System.out.println("Setting end");
                        send.end((Handler<AsyncResult<Void>>) sendResult -> {
                            System.out.println("There was an error, we'll catch it here...");
                            setError(request.response(), sendResult.cause());
                        });
                    }

                } else {
                    System.out.println("There was a failure. Should see it come through end()");
                }
            });

        } else {
            setError(request.response(), result.cause());
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

        receive.endHandler((Handler<Void>) vertxEngineResult -> {
            System.out.println("Response has been written");
            response.end();
        });

        receive.policyFailureHandler((Handler<VertxPolicyFailure>) failure -> {
            setPolicyFailure(response, failure);
        });
    }

    private void setError(HttpServerResponse response, Throwable error) {
        System.out.println("SetError called");

        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        response.setStatusMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
        response.headers().add("X-Exception", String.valueOf(error.getMessage())); //$NON-NLS-1$
        response.write(ExceptionUtils.getStackTrace(error));
    }

    private void setPolicyFailure(HttpServerResponse response, VertxPolicyFailure failure) {
        System.out.println("SetPolicyFailure called");

        response.headers().add("X-Policy-Failure-Type", String.valueOf(failure.getType())); //$NON-NLS-1$
        response.headers().add("X-Policy-Failure-Message", failure.getMessage()); //$NON-NLS-1$
        response.headers().add("X-Policy-Failure-Code", String.valueOf(failure.getFailureCode())); //$NON-NLS-1$
        response.headers().add(HttpHeaders.CONTENT_TYPE, "application/json"); //$NON-NLS-1$

        HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;

        if (failure.getType() == PolicyFailureType.Authentication) {
            status = HttpResponseStatus.UNAUTHORIZED;
        } else if (failure.getType() == PolicyFailureType.Authorization) {
            status = HttpResponseStatus.FORBIDDEN;
        }

        response.setStatusCode(status.code());
        response.setStatusMessage(failure.getMessage());

        for (Entry<String, String> entry : failure.getHeaders().entrySet()) {
            response.headers().add(entry.getKey(), entry.getValue());
        }

        response.write(failure.getRaw());
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.HTTP_GATEWAY;
    }
}