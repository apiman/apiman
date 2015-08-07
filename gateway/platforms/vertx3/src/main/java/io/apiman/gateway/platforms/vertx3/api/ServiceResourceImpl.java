/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.platforms.vertx3.api;

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
import io.apiman.gateway.platforms.vertx3.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("nls")
public class ServiceResourceImpl implements IServiceResource, IRouteBuilder {
    private static final String ORG_ID = "organizationId";
    private static final String SVC_ID = "serviceId";
    private static final String VER = "version";
    private static final String RETIRE = IRouteBuilder.join(ORG_ID, SVC_ID, VER);
    private static final String ENDPOINT = IRouteBuilder.join(ORG_ID, SVC_ID, VER) + "/endpoint";
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
        routingContext.request().bodyHandler((Handler<Buffer>) buffer -> {
            try {
                publish(Json.decodeValue(buffer.toString("utf-8"), Service.class));
                end(routingContext, HttpResponseStatus.NO_CONTENT);
            } catch (PublishingException e) {
                error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
            } catch (NotAuthorizedException e) {
                error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
            }
        });
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
        router.put(buildPath("")).handler(this::publish);
        router.delete(buildPath(RETIRE)).handler(this::retire);
        router.get(buildPath(ENDPOINT)).handler(this::getServiceEndpoint);
    }

    @Override
    public String getPath() {
        return "services";
    }
}
