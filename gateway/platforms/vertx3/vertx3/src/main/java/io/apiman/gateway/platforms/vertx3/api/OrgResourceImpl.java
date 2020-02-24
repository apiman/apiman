/*
 * Copyright 2017 JBoss Inc
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

import io.apiman.gateway.api.rest.IOrgResource;
import io.apiman.gateway.api.rest.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;

import javax.ws.rs.container.AsyncResponse;

/**
 * Implementation of the Org API
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class OrgResourceImpl extends AbstractResource implements IOrgResource {

    private IRegistry registry;

    public OrgResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.registry = engine.getRegistry();
    }

    @Override
    public void listOrgs(AsyncResponse response) throws NotAuthorizedException {
        registry.listOrgs(handlerWithResult(response));
    }

}
