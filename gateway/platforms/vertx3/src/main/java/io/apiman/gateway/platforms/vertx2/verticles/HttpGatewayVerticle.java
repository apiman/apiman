package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.http.HttpExecutor;
import io.vertx.core.http.HttpServerOptions;

public class HttpGatewayVerticle extends ApimanVerticleBase {
    public static final VerticleType VERTICLE_TYPE = VerticleType.HTTP;

    @Override
    public void start() {
        super.start();

        HttpServerOptions standardOptions = new HttpServerOptions()
            .setHost(apimanConfig.getHostname());

        vertx.createHttpServer(standardOptions)
            .requestHandler(new HttpExecutor(vertx, log, false))
            .listen(apimanConfig.getPort(VERTICLE_TYPE));
    }

    @Override
    public VerticleType verticleType() {
        return VERTICLE_TYPE;
    }
}