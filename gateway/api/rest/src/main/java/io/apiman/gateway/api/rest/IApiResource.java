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
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
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
 * The API API.  Ha!
 *
 * @author eric.wittmann@redhat.com
 */
@io.swagger.annotations.Api
@Path("")
public interface IApiResource {

    // Legacy API, plus simple publishing endpoint
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("apis")
    public void publish(Api api) throws PublishingException, NotAuthorizedException;

    /**
     * @see #retire(String, String, String, AsyncResponse)
     */
    @DELETE
    @Path("apis/{organizationId}/{apiId}/{version}")
    @Deprecated
    public void retire(@PathParam("organizationId") String organizationId,
                       @PathParam("apiId") String apiId,
                       @PathParam("version") String version)
            throws RegistrationException, NotAuthorizedException;

    @GET
    @Path("apis/{organizationId}/{apiId}/{version}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated
    public ApiEndpoint getApiEndpoint(@PathParam("organizationId") String organizationId,
                                      @PathParam("apiId") String apiId,
                                      @PathParam("version") String version)
            throws NotAuthorizedException;

    // New API
    @DELETE
    @Path("organizations/{organizationId}/apis/{apiId}/versions/{version}")
    public void retire(@PathParam("organizationId") String organizationId,
                       @PathParam("apiId") String apiId,
                       @PathParam("version") String version,
                       @Suspended final AsyncResponse response)
            throws RegistrationException, NotAuthorizedException;

    @GET
    @Path("organizations/{organizationId}/apis/{apiId}/versions/{version}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public void getApiEndpoint(@PathParam("organizationId") String organizationId,
                               @PathParam("apiId") String apiId,
                               @PathParam("version") String version,
                               @Suspended final AsyncResponse response) // ApiEndpoint
            throws NotAuthorizedException;

    @GET
    @Path("organizations/{organizationId}/apis/")
    @Produces(MediaType.APPLICATION_JSON)
    public void listApis(@PathParam("organizationId") String organizationId,
                         @QueryParam("page") int page,
                         @QueryParam("pageSize") int pageSize,
                         @Suspended final AsyncResponse response) throws NotAuthorizedException;

    @GET
    @Path("organizations/{organizationId}/apis/{apiId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public void listApiVersions(@PathParam("organizationId") String organizationId,
                                @PathParam("apiId") String apiId,
                                @QueryParam("page") int page,
                                @QueryParam("pageSize") int pageSize,
                                @Suspended final AsyncResponse response) throws NotAuthorizedException;

    @GET
    @Path("organizations/{organizationId}/apis/{apiId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getApiVersion(@PathParam("organizationId") String organizationId,
                              @PathParam("apiId") String apiId,
                              @PathParam("version") String version,
                              @Suspended final AsyncResponse response) throws NotAuthorizedException;
}
