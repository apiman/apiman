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
package io.apiman.gateway.platforms.vertx2.api;

import io.apiman.gateway.api.rest.contract.ISystemResource;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.platforms.vertx2.config.VertxEngineConfig;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class SystemResourceImpl implements ISystemResource, IRouteBuilder {

    private static final String STATUS = "status"; //$NON-NLS-1$
    private IEngine engine;

    public SystemResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.engine = engine;
    }

    @Override
    public SystemStatus getStatus() {
        SystemStatus status = new SystemStatus();
        status.setUp(true);
        status.setVersion(engine.getVersion());
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
