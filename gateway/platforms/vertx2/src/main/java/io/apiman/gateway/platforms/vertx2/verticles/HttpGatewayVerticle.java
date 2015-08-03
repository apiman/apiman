package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.http.HttpExecutor;
import io.vertx.core.http.HttpServerOptions;

public class HttpGatewayVerticle extends ApimanVerticleBase {

    @Override
    public void start() {
        super.start();

        HttpServerOptions standardOptions = new HttpServerOptions()
            .setHost(apimanConfig.getHostname());

        vertx.createHttpServer(standardOptions)
            .requestHandler(new HttpExecutor(vertx, log, apimanConfig))
            .listen(apimanConfig.getPort(VerticleType.HTTP));
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.HTTP;
    }
}