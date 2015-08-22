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

import io.apiman.gateway.api.rest.contract.IApplicationResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Implement {@link ApplicationResourceImpl} using Vert.x Web.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ApplicationResourceImpl implements IApplicationResource, IRouteBuilder {

    private static final String ORG_ID = "organizationId"; //$NON-NLS-1$
    private static final String APP_ID = "applicationId"; //$NON-NLS-1$
    private static final String VER = "version"; //$NON-NLS-1$
    private static final String UNREGISTER = IRouteBuilder.join(ORG_ID, APP_ID, VER);
    private IRegistry registry;

    public ApplicationResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.registry = engine.getRegistry();
    }

    @Override
    public void register(Application application) throws RegistrationException, NotAuthorizedException {
        registry.registerApplication(application, (IAsyncResultHandler<Void>) result -> {
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

    public void register(RoutingContext routingContext) {
        routingContext.request().bodyHandler((Handler<Buffer>) buffer -> {
            try {
                register(Json.decodeValue(buffer.toString("utf-8"), Application.class)); //$NON-NLS-1$
                end(routingContext, HttpResponseStatus.NO_CONTENT);
            } catch (RegistrationException e) {
                error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
            } catch (NotAuthorizedException e) {
                error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
            }
        });
    }

    @Override
    public void unregister(String organizationId, String applicationId, String version)
            throws RegistrationException, NotAuthorizedException {
        Application application = new Application();
        application.setOrganizationId(organizationId);
        application.setApplicationId(applicationId);
        application.setVersion(version);
        registry.unregisterApplication(application, (IAsyncResultHandler<Void>) result -> {
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

    public void unregister(RoutingContext routingContext) {
        String orgId = routingContext.request().getParam(ORG_ID);
        String appId = routingContext.request().getParam(APP_ID);
        String ver = routingContext.request().getParam(VER);
        try {
            unregister(orgId, appId, ver);
            end(routingContext, HttpResponseStatus.NO_CONTENT);
        } catch (RegistrationException e) {
            error(routingContext, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        } catch (NotAuthorizedException e) {
            error(routingContext, HttpResponseStatus.UNAUTHORIZED, e.getMessage(), e);
        }
    }

    @Override
    public String getPath() {
        return "applications"; //$NON-NLS-1$
    }

    @Override
    public void buildRoutes(Router router) {
        router.put(buildPath("")).handler(this::register); //$NON-NLS-1$
        router.delete(buildPath(UNREGISTER)).handler(this::unregister);
    }
}
