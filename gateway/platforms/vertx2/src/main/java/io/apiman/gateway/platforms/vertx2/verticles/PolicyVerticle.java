package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.apiman.gateway.platforms.vertx2.services.impl.InitializeIngestorServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;

public class PolicyVerticle extends ApimanVerticleWithEngine {

    private InitializeIngestorServiceImpl service;

    @Override
    public void start() {
        super.start();

        service = new InitializeIngestorServiceImpl(vertx, apimanConfig, engine, log);

        // Listen for anyone who wants to initialise a PolicyIngestion connection
        ProxyHelper.registerService(InitializeIngestorService.class, vertx, service,
                VertxEngineConfig.GATEWAY_ENDPOINT_POLICY_INGESTION);
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.POLICY;
    }
}