package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.http.HttpServiceFactory;
import io.apiman.gateway.platforms.vertx2.io.VertxApimanBuffer;
import io.apiman.gateway.platforms.vertx2.io.VertxPolicyFailure;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceRequest;
import io.apiman.gateway.platforms.vertx2.io.VertxServiceResponse;
import io.apiman.gateway.platforms.vertx2.services.IngestorToPolicyService;
import io.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.apiman.gateway.platforms.vertx2.services.PolicyToIngestorService;
import io.apiman.gateway.platforms.vertx2.services.impl.PolicyToIngestorServiceImpl;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.JksOptions;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

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

        HttpServerOptions standardOptions = new HttpServerOptions()
            .setHost(apimanConfig.getHostname());

        vertx.createHttpServer(standardOptions)
            .requestHandler(this::plaintextHandler)
            .listen(apimanConfig.getPort(VerticleType.HTTP));

        if (apimanConfig.isSSL()) {
            HttpServerOptions sslOptions = new HttpServerOptions(standardOptions)
                    .setSsl(true)
                    .setKeyStoreOptions(
                            new JksOptions()
                                .setPath(apimanConfig.getKeyStore())
                                .setPassword(apimanConfig.getKeyStorePassword()))
                    .setTrustStoreOptions(
                            new JksOptions()
                                .setPath(apimanConfig.getTrustStore())
                                .setPassword(apimanConfig.getTrustStorePassword()));

            vertx.createHttpServer(sslOptions)
                .requestHandler(this::sslHandler)
                .listen(apimanConfig.getPort(VerticleType.HTTPS));
        }
    }

    private void requestHandler(HttpServerRequest req, boolean isSecure) {
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
            setupRequest(req, result, httpSessionUuid, true);
        });
    }

    private void plaintextHandler(HttpServerRequest req) {
        requestHandler(req, false);
    }

    private void sslHandler(HttpServerRequest req) {
        requestHandler(req, true);
    }

    // Setup request leg
    private void setupRequest(HttpServerRequest request, AsyncResult<IngestorToPolicyService> result,
            String httpSessionUuid, boolean isSecure) {
        log.debug("Setting up the request with " + httpSessionUuid);

        IngestorToPolicyService send = result.result();

        if (result.succeeded()) {
            VertxServiceRequest serviceRequest = HttpServiceFactory.buildRequest(request, isSecure);

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
                                if (sendResult.failed())
                                    setError(request.response(), sendResult.cause());
                            });
                        });

                        System.out.println("Resuming");
                        request.resume();
                    } else { // It didn't work - probably a policy failure?. Just call end immediately.
                        System.out.println("There was a policy failure");
//
                        System.out.println("End called....");
                        send.end((Handler<AsyncResult<Void>>) sendResult -> {
                            if (sendResult.failed()) {
                                System.out.println("There was an error, we'll catch it here...");
                                setError(request.response(), sendResult.cause());
                            }
                        });
                    }

                } else {
                    System.out.println("There was a failure - likely an exception. Should see it come through end()");
                    setError(request.response(), ready.cause());
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

            if (!response.ended())
                response.end();
        });

        receive.policyFailureHandler((Handler<VertxPolicyFailure>) failure -> {
            setPolicyFailure(response, failure);
        });
    }

    private void setError(HttpServerResponse response, Throwable error) {
        response.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        response.setStatusMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
        response.headers().add("X-Exception", String.valueOf(error.getMessage())); //$NON-NLS-1$
        response.end(ExceptionUtils.getStackTrace(error));
    }

    private void setPolicyFailure(HttpServerResponse response, VertxPolicyFailure failure) {
        response.headers().add("X-Policy-Failure-Type", String.valueOf(failure.getType())); //$NON-NLS-1$
        response.headers().add("X-Policy-Failure-Message", failure.getMessage()); //$NON-NLS-1$
        response.headers().add("X-Policy-Failure-Code", String.valueOf(failure.getFailureCode())); //$NON-NLS-1$
        response.headers().add(HttpHeaders.CONTENT_TYPE,  MediaType.APPLICATION_JSON);

        int code = HttpResponseStatus.INTERNAL_SERVER_ERROR.code();

        switch(failure.getType()) {
        case Authentication:
            code = HttpResponseStatus.UNAUTHORIZED.code();
            break;
        case Authorization:
            code = HttpResponseStatus.FORBIDDEN.code();
            break;
        case NotFound:
            code = HttpResponseStatus.NOT_FOUND.code();
            break;
        case Other:
            code = failure.getResponseCode();
            break;
        }

        response.setStatusCode(code);
        response.setStatusMessage(failure.getMessage());

        for (Entry<String, String> entry : failure.getHeaders().entrySet()) {
            response.headers().add(entry.getKey(), entry.getValue());
        }

        response.write(failure.getRaw());
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.HTTP;
    }
}