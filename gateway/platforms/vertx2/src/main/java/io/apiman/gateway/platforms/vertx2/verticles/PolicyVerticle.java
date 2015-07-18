package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.vertx.apiman.gateway.platforms.vertx2.services.InitializeIngestorService;
import io.vertx.apiman.gateway.platforms.vertx2.services.impl2.InitializeIngestorServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ProxyHelper;

public class PolicyVerticle extends AbstractVerticle {

  private InitializeIngestorService service;

  @Override
  public void start() {
      service = new InitializeIngestorServiceImpl(vertx);

      // Listen for anyone who wants to initialise a PolicyIngestion connection
      ProxyHelper.registerService(InitializeIngestorService.class, vertx, service,
              VertxEngineConfig.GATEWAY_ENDPOINT_POLICY_INGESTION);
  }
}