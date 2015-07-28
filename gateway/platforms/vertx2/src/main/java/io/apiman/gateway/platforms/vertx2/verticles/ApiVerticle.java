package io.apiman.gateway.platforms.vertx2.verticles;

import io.apiman.gateway.platforms.vertx2.api.ApplicationResourceImpl;
import io.apiman.gateway.platforms.vertx2.api.RouteBuilder;
import io.apiman.gateway.platforms.vertx2.api.ServiceResourceImpl;
import io.apiman.gateway.platforms.vertx2.api.SystemResourceImpl;
import io.vertx.ext.web.Router;

public class ApiVerticle extends ApimanVerticleBase {

    private RouteBuilder applicationResource = new ApplicationResourceImpl();
    private RouteBuilder serviceResource = new ServiceResourceImpl();
    private RouteBuilder systemResource = new SystemResourceImpl();

    @Override
    public void start() {
        Router router = Router.router(vertx);

        applicationResource.buildRoutes(router);
        serviceResource.buildRoutes(router);
        systemResource.buildRoutes(router);

        vertx.createHttpServer().requestHandler(router::accept).listen(7070);
    }

    @Override
    public VerticleType verticleType() {
        return VerticleType.API;
    }
}
