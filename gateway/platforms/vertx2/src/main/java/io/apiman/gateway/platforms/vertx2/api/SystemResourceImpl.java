package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.gateway.api.rest.contract.ISystemResource;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class SystemResourceImpl implements ISystemResource, IRouteBuilder {

    private static final String STATUS = "status"; //$NON-NLS-1$

    public SystemResourceImpl(VertxEngineConfig apimanConfig) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public SystemStatus getStatus() {
        SystemStatus status = new SystemStatus();
        status.setUp(true);
        status.setVersion("1"); // TODO do something more sensible //$NON-NLS-1$
        return status;
    }

    public void getStatus(RoutingContext routingContext) {
        if (getStatus() == null) {
            error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Status invalid", null); //$NON-NLS-1$
        } else {
            writeBody(routingContext, getStatus());
        }
    }

    @Override
    public void buildRoutes(Router router) {
        router.get(buildPath(STATUS)).handler(this::getStatus);
    }

    @Override
    public String getPath() {
        return "system"; //$NON-NLS-1$
    }
}
