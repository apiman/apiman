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

import io.apiman.gateway.api.rest.contract.IClientResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Implement {@link ClientResourceImpl} using Vert.x Web.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ClientResourceImpl implements IClientResource, IRouteBuilder {

    private static final String ORG_ID = "organizationId"; //$NON-NLS-1$
    private static final String CLIENT_ID = "clientId"; //$NON-NLS-1$
    private static final String VER = "version"; //$NON-NLS-1$
    private static final String UNREGISTER = IRouteBuilder.join(ORG_ID, CLIENT_ID, VER);
    private IRegistry registry;
    private RoutingContext routingContext;
    private VertxEngineConfig apimanConfig;
    private IEngine engine;

    public ClientResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.registry = engine.getRegistry();
        this.apimanConfig = apimanConfig;
        this.engine = engine;
        this.routingContext = null;
    }

    private ClientResourceImpl(VertxEngineConfig apimanConfig, IEngine engine, RoutingContext routingContext) {
        this.registry = engine.getRegistry();
        this.apimanConfig = apimanConfig;
        this.engine = engine;
        this.routingContext = routingContext;
    }

    @Override
    public void register(Client client) throws RegistrationException, NotAuthorizedException {
        registry.registerClient(client, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                error(routingContext, result.getError());
//                Throwable e = result.getError();
//                if (e instanceof RegistrationException) {
//                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
//                } else if (e instanceof NotAuthorizedException) {
//                    error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
//                } else {
//                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
//                }
            } else {
                end(routingContext, HttpResponseStatus.NO_CONTENT);
            }
        });
    }

    public void register() {
        try {
            register(Json.decodeValue(routingContext.getBodyAsString(), Client.class));
        } catch (Exception e) {
            error(routingContext, e);
//            error(routingContext, HttpResponseStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Override
    public void unregister(String organizationId, String clientId, String version)
            throws RegistrationException, NotAuthorizedException {
        Client client = new Client();
        client.setOrganizationId(organizationId);
        client.setClientId(clientId);
        client.setVersion(version);
        client.setContracts(null);

        registry.unregisterClient(client, (IAsyncResultHandler<Void>) result -> {
            if (result.isError()) {
                Throwable e = result.getError();
                error(routingContext, e);
//                if (e instanceof RegistrationException) {
//                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
//                } else if (e instanceof NotAuthorizedException) {
//                    error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
//                } else {
//                    error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
//                }
            } else {
                end(routingContext, HttpResponseStatus.NO_CONTENT);
            }
        });
    }

    public void unregister() {
        String orgId = routingContext.request().getParam(ORG_ID);
        String appId = routingContext.request().getParam(CLIENT_ID);
        String ver = routingContext.request().getParam(VER);
        unregister(orgId, appId, ver);
    }

    @Override
    public String getPath() {
        return "clients"; //$NON-NLS-1$
    }

    @Override
    public void buildRoutes(Router router) {
        router.put(buildPath("")).handler( routingContext -> { //$NON-NLS-1$
            new ClientResourceImpl(apimanConfig, engine, routingContext).register();
        });

        router.delete(buildPath(UNREGISTER)).handler( routingContext -> {
            new ClientResourceImpl(apimanConfig, engine, routingContext).unregister();
        });
    }
}
