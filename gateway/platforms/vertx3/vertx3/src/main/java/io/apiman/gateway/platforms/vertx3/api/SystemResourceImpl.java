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

import io.apiman.gateway.api.rest.contract.ISystemResource;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.engine.beans.GatewayEndpoint;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.helpers.EndpointHelper;

/**
 * System Resource route builder
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class SystemResourceImpl implements ISystemResource {

    private IEngine engine;
    private VertxEngineConfig apimanConfig;

    public SystemResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.apimanConfig = apimanConfig;
        this.engine = engine;
    }

    @Override
    public SystemStatus getStatus() {
        SystemStatus status = new SystemStatus();
        status.setUp(true);
        status.setVersion(engine.getVersion());
        return status;
    }

    @Override
    public GatewayEndpoint getEndpoint() {
        EndpointHelper endpointHelper = new EndpointHelper(apimanConfig);
        String endpoint = endpointHelper.getGatewayEndpoint();
        GatewayEndpoint endpointObj = new GatewayEndpoint();
        endpointObj.setEndpoint(endpoint);
        return endpointObj;
    }
}
