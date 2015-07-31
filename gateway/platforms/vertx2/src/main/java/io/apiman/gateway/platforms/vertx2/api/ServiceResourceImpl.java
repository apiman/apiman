package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.common.util.SimpleStringUtils;
import io.apiman.gateway.api.rest.contract.IServiceResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.net.MalformedURLException;
import java.net.URL;

public class ServiceResourceImpl implements IServiceResource, IRouteBuilder {
    private static final String ORG_ID = "organizationId"; //$NON-NLS-1$
    private static final String SVC_ID = "serviceId"; //$NON-NLS-1$
    private static final String VER = "version"; //$NON-NLS-1$
    private static final String RETIRE = IRouteBuilder.join(ORG_ID, SVC_ID, VER);
    private static final String ENDPOINT = IRouteBuilder.join(ORG_ID, SVC_ID, VER) + "/endpoint"; //$NON-NLS-1$
    private VertxEngineConfig apimanConfig;
    private String host;
    private IRegistry registry;

    public ServiceResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.apimanConfig = apimanConfig;
        this.registry = engine.getRegistry();
    }

    @Override
    public void publish(Service service) throws PublishingException, NotAuthorizedException {
        registry.publishService(service, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                if (result.getError() instanceof PublishingException) {
                    throw (PublishingException) result.getError();
                } else if (result.getError() instanceof NotAuthorizedException) {
                    throw (NotAuthorizedException) result.getError();
                } else {
                    throw new RuntimeException(result.getError());
                }
            }
        });
    }

    public void publish(RoutingContext routingContext) {
        try {
            routingContext.request().bodyHandler((Handler<Buffer>) buffer -> {
                publish(Json.decodeValue(buffer.toString("utf-8"), Service.class)); //$NON-NLS-1$
                end(routingContext, HttpResponseStatus.CREATED);
            });
        } catch (PublishingException e) {
            error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @Override
    public void retire(String organizationId, String serviceId, String version) throws RegistrationException,
            NotAuthorizedException {
        Service service = new Service();
        service.setOrganizationId(organizationId);
        service.setServiceId(serviceId);
        service.setVersion(version);
        registry.retireService(service, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                if (result.getError() instanceof RegistrationException) {
                    throw (RegistrationException) result.getError();
                } else if (result.getError() instanceof NotAuthorizedException) {
                    throw (NotAuthorizedException) result.getError();
                } else {
                    throw new RuntimeException(result.getError());
                }
            }
        });
    }

    public void retire(RoutingContext routingContext) {
        String orgId = routingContext.request().getParam(ORG_ID);
        String svcId = routingContext.request().getParam(SVC_ID);
        String ver = routingContext.request().getParam(VER);

        try {
            retire(orgId, svcId, ver);
            end(routingContext, HttpResponseStatus.NO_CONTENT);
        } catch (RegistrationException e) {
            error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    // TODO refactor to look up serviceId in engine, we can then determine more accurately what the URL scheme should be.
    @Override
    @SuppressWarnings("nls")
    public ServiceEndpoint getServiceEndpoint(String organizationId, String serviceId, String version)
            throws NotAuthorizedException {
        String scheme = apimanConfig.preferSecure() ? "https" : "http";
        int port = apimanConfig.getPort(scheme);
        StringBuilder sb = new StringBuilder(100);
        sb.append(scheme + "://");

        if (apimanConfig.getEndpoint() == null) {
            sb.append(host);
        } else {
            sb.append(apimanConfig.getEndpoint());
        }

        if (port != 443 && port != 80)
            sb.append(":" + port + "/");
        sb.append(SimpleStringUtils.join("/", organizationId, serviceId, version));

        ServiceEndpoint endpoint = new ServiceEndpoint();
        endpoint.setEndpoint(sb.toString());
        return endpoint;
    }

    public void getServiceEndpoint(RoutingContext routingContext) {
        if (apimanConfig.getEndpoint() == null) {
            try {
                host = new URL(routingContext.request().absoluteURI()).getHost();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        String orgId = routingContext.request().getParam(ORG_ID);
        String svcId = routingContext.request().getParam(SVC_ID);
        String ver = routingContext.request().getParam(VER);
        try {
            writeBody(routingContext, getServiceEndpoint(orgId, svcId, ver));
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @Override
    public void buildRoutes(Router router) {
        router.put(buildPath("")).handler(this::publish); //$NON-NLS-1$
        router.delete(buildPath(RETIRE)).handler(this::retire);
        router.get(buildPath(ENDPOINT)).handler(this::getServiceEndpoint);
    }

    @Override
    public String getPath() {
        return "services"; //$NON-NLS-1$
    }
}
