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

import io.apiman.gateway.api.rest.IClientResource;
import io.apiman.gateway.api.rest.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Implement {@link ClientResourceImpl} using Vert.x Web.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ClientResourceImpl extends AbstractResource implements IClientResource {

    private IRegistry registry;

    public ClientResourceImpl(VertxEngineConfig apimanConfig, IEngine engine) {
        this.registry = engine.getRegistry();
    }

    @Override
    public void register(Client client) throws RegistrationException, NotAuthorizedException {
        registry.registerClient(client, handlerWithEmptyResult());
    }

    @Override
    public void unregister(String organizationId, String clientId, String version) throws RegistrationException, NotAuthorizedException {
        Client client = new Client();
        client.setOrganizationId(organizationId);
        client.setClientId(clientId);
        client.setVersion(version);
        registry.unregisterClient(client, handlerWithEmptyResult());
    }

    @Override
    public void unregister(String organizationId, String clientId, String version, AsyncResponse response)
            throws RegistrationException, NotAuthorizedException {
        Client client = new Client();
        client.setOrganizationId(organizationId);
        client.setClientId(clientId);
        client.setVersion(version);
        registry.unregisterClient(client, handlerWithEmptyResult(response));
    }

    @Override
    public void listClients(String organizationId, int page, int pageSize, AsyncResponse response) throws NotAuthorizedException {
        registry.listClients(organizationId, page, pageSize, handlerWithResult(response));
    }

    @Override
    public void listClientVersions(String organizationId, String clientId, int page, int pageSize, AsyncResponse response)
            throws NotAuthorizedException {
        registry.listClientVersions(organizationId, clientId, page, pageSize, handlerWithResult(response));
    }

    @Override
    public void getClientVersion(String organizationId, String clientId, String version, AsyncResponse response) throws NotAuthorizedException {
        registry.getClient(organizationId, clientId, version, result -> {
            if (result.isSuccess()) {
                Client client = result.getResult();
                if (client == null) {
                    response.resume(Response.status(Status.NOT_FOUND).build());
                } else {
                    response.resume(Response.ok(client).build());
                }
            } else {
                throwError(result.getError());
            }
        });
    }

}
