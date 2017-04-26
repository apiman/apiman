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
import io.apiman.gateway.api.rest.contract.IApiResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * API Resource route builder
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class ApiResourceImpl implements IApiResource, IRouteBuilder {
    private static final String ORG_ID = "organizationId";
    private static final String API_ID = "apiId";
    private static final String VER = "version";
    private static final String RETIRE = IRouteBuilder.join(ORG_ID, API_ID, VER);
    private static final String ENDPOINT = IRouteBuilder.join(ORG_ID, API_ID, VER) + "/endpoint";
    private VertxEngineConfig apimanConfig;
    private String host;
    private IRegistry registry;
    private RoutingContext routingContext;
    private IEngine engine;

    public ApiResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.apimanConfig = apimanConfig;
        this.registry = engine.getRegistry();
        this.engine = engine;
        this.routingContext = null;
    }

    private ApiResourceImpl(VertxEngineConfig apimanConfig, IEngine engine, RoutingContext routingContext) {
        this.apimanConfig = apimanConfig;
        this.registry = engine.getRegistry();
        this.engine = engine;
        this.routingContext = routingContext;
    }

    @Override
    public void publish(Api api) throws PublishingException, NotAuthorizedException {
        registry.publishApi(api, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                Throwable e = result.getError();
                if (e instanceof PublishingException) {
                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
                } else if (e instanceof NotAuthorizedException) {
                    error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
                } else {
                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
                }
            } else {
                end(routingContext, HttpResponseStatus.NO_CONTENT);
            }
        });
    }

    public void publish() {
      try {
          publish(Json.decodeValue(routingContext.getBodyAsString(), Api.class));
      } catch (Exception e) {
          error(routingContext, HttpResponseStatus.BAD_REQUEST, e.getMessage(), e);
      }
    }

    @Override
    public void retire(String organizationId, String apiId, String version) throws RegistrationException,
            NotAuthorizedException {
        Api api = new Api();
        api.setOrganizationId(organizationId);
        api.setApiId(apiId);
        api.setVersion(version);

        registry.retireApi(api, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                Throwable e = result.getError();
                if (e instanceof RegistrationException) {
                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
                } else if (e instanceof NotAuthorizedException) {
                    error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
                } else {
                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
                }
            } else {
                end(routingContext, HttpResponseStatus.NO_CONTENT);
            }
        });
    }

    public void retire() {
        String orgId = routingContext.request().getParam(ORG_ID);
        String apiId = routingContext.request().getParam(API_ID);
        String ver = routingContext.request().getParam(VER);

        retire(orgId, apiId, ver);
    }

    @Override
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version)
            throws NotAuthorizedException {
        String scheme = apimanConfig.preferSecure() ? "https" : "http";
        int port = apimanConfig.getPort(scheme);
        String host = this.host;
        String path = "";
        // If endpoint was manually specified
        if (apimanConfig.getPublicEndpoint() != null) {
           URI publicEndpoint = URI.create(apimanConfig.getPublicEndpoint());

           if (publicEndpoint.getPort() != -1) {
               port = publicEndpoint.getPort();
           }
           if (publicEndpoint.getScheme() != null && !publicEndpoint.getScheme().isEmpty()) {
               scheme = publicEndpoint.getScheme();
           }
           if (publicEndpoint.getPath() != null && !publicEndpoint.getPath().isEmpty()) {
               path = publicEndpoint.getPath();
           }
           if (publicEndpoint.getHost() != null && !publicEndpoint.getHost().isEmpty()) {
               host = publicEndpoint.getHost();
           }
        }

        String endpoint = scheme + "://" + host;
        if (port != 443 && port != 80)
            endpoint += ":" + port + "/";
        endpoint += path;
        endpoint += SimpleStringUtils.join("/", organizationId, apiId, version);

        ApiEndpoint endpointObj = new ApiEndpoint();
        endpointObj.setEndpoint(endpoint);
        return endpointObj;
    }

    public void getApiEndpoint(RoutingContext routingContext) {
        if (apimanConfig.getPublicEndpoint() == null) {
            try {
                host = new URL(routingContext.request().absoluteURI()).getHost();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        String orgId = routingContext.request().getParam(ORG_ID);
        String apiId = routingContext.request().getParam(API_ID);
        String ver = routingContext.request().getParam(VER);
        try {
            writeBody(routingContext, getApiEndpoint(orgId, apiId, ver));
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @Override
    public void buildRoutes(Router router) {
        router.put(buildPath("")).handler(routingContext -> {
            new ApiResourceImpl(apimanConfig, engine, routingContext).publish();
        });
        router.delete(buildPath(RETIRE)).handler(routingContext -> {
            new ApiResourceImpl(apimanConfig, engine, routingContext).retire();
        });
        router.get(buildPath(ENDPOINT)).handler(this::getApiEndpoint);
    }

    @Override
    public String getPath() {
        return "apis";
    }
}
