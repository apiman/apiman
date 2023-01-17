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

package io.apiman.gateway.api.rest;

import io.apiman.gateway.api.rest.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

/**
 * The client API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("/")
public interface IClientResource {

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("clients")
    void register(Client client) throws RegistrationException, NotAuthorizedException;

    @DELETE
    @Path("clients/{organizationId}/{clientId}/{version}")
    @Deprecated
    void unregister(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId,
            @PathParam("version") String version)
            throws RegistrationException, NotAuthorizedException;

    // New API
    @DELETE
    @Path("organizations/{organizationId}/clients/{clientId}/versions/{version}")
    void unregister(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId,
            @PathParam("version") String version,
            @Suspended final AsyncResponse response)
            throws RegistrationException, NotAuthorizedException;

    @GET
    @Path("organizations/{organizationId}/clients/")
    @Produces(MediaType.APPLICATION_JSON)
    void listClients(@PathParam("organizationId") String organizationId,
                         @QueryParam("page") int page,
                         @QueryParam("pageSize") int pageSize,
                         @Suspended final AsyncResponse response) throws NotAuthorizedException;

    @GET
    @Path("organizations/{organizationId}/clients/{clientId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    void listClientVersions(@PathParam("organizationId") String organizationId,
                         @PathParam("clientId") String clientId,
                         @QueryParam("page") int page,
                         @QueryParam("pageSize") int pageSize,
                         @Suspended final AsyncResponse response) throws NotAuthorizedException;

    @GET
    @Path("organizations/{organizationId}/clients/{clientId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    void getClientVersion(@PathParam("organizationId") String organizationId,
                              @PathParam("clientId") String clientId,
                              @PathParam("version") String version,
                              @Suspended final AsyncResponse response) throws NotAuthorizedException;

}
