/*
 * Copyright 2014 JBoss Inc
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

package io.apiman.gateway.api.rest.contract;

import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.swagger.annotations.Api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * The lient API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("clients")
@Api
public interface IClientResource {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void register(Client client) throws RegistrationException, NotAuthorizedException;

    @DELETE
    @Path("{organizationId}/{clientId}/{version}")
    public void unregister(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws RegistrationException, NotAuthorizedException;
}
