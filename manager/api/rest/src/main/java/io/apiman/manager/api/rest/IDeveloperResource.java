/*
 * Copyright 2020 Scheer PAS Schweiz AG
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

package io.apiman.manager.api.rest;

import io.apiman.common.util.MediaType;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.developers.DeveloperApiVersionBeanDto;
import io.apiman.manager.api.beans.developers.DeveloperBean;
import io.apiman.manager.api.beans.developers.UpdateDeveloperBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.rest.exceptions.DeveloperAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.DeveloperNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A interface for REST calls for the Developer Portal
 */
@Path("/developers")
@Tag(name = "Developers")
@Deprecated(forRemoval = true)
@RolesAllowed("devportaluser")
public interface IDeveloperResource {

    /**
     * Use this endpoint to get a list of all public ApiVersionBeans
     *
     * @return The list of all public api versions
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("apis")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of public apis was successfully returned"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed")
    })
    List<ApiVersionBean> getAllPublicApiVersions() throws NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all developers
     *
     * @return The list of all developers
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the developer list was successfully returned"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed")
    })
    List<DeveloperBean> getDevelopers() throws NotAuthorizedException;

    /**
     * Use this endpoint to create a new developer
     *
     * @param bean Information about the new developer
     * @return Full details about the developer that was created
     * @throws InvalidNameException
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the developer was successfully created"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed")
    })
    DeveloperBean create(DeveloperBean bean) throws InvalidNameException, NotAuthorizedException, DeveloperAlreadyExistsException;

    /**
     * Use this endpoint to update an developer
     *
     * @param bean Information about the updated developer
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @PUT
    @Path("{developerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the developer was successfully updated"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed"),
            @ApiResponse(responseCode = "404", description = "If the developer does not exist.")
    })
    void update(@PathParam("developerId") String id, UpdateDeveloperBean bean) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single developer by its id
     *
     * @param id The id of the developer
     * @return The developer
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @GET
    @Path("{developerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the developer was successfully returned"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed"),
            @ApiResponse(responseCode = "404", description = "If the developer does not exist.")
    })
    DeveloperBean get(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete an developer
     *
     * @param id The id of the developer
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @DELETE
    @Path("{developerId}")
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the developer was successfully deleted"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed"),
            @ApiResponse(responseCode = "404", description = "If the developer does not exist.")
    })
    void delete(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get all list of all ClientVersionSummaryBeans from all
     * clients that are mapped to this developer
     *
     * @param id The id of the developer
     * @return The list of ClientVersionSummaryBeans
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @GET
    @Path("{developerId}/clients")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the the list was successfully returned"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed"),
            @ApiResponse(responseCode = "404", description = "If the developer does not exist.")
    })
    List<ClientVersionSummaryBean> getAllClientVersions(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get all list of all ContractSummaryBeans from all
     * clients that are mapped to this developer
     *
     * @param id The id of the developer
     * @return The list of ContractSummaryBeans
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @GET
    @Path("{developerId}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the the list was successfully returned"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed"),
            @ApiResponse(responseCode = "404", description = "If the developer does not exist.")
    })
    List<ContractSummaryBean> getAllClientContracts(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get all list of all ApiVersionBeans from all
     * clients that are mapped to this developer
     *
     * @param id The id of the developer
     * @return The list of ApiVersionBeans
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to

     */
    @GET
    @Path("{developerId}/apis")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the the list was successfully returned"),
            @ApiResponse(responseCode = "403", description = "If the access is not allowed"),
            @ApiResponse(responseCode = "404", description = "If the developer does not exist.")
    })
    List<DeveloperApiVersionBeanDto> getAllApiVersions(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve the API Definition if the user has a contract to
     * the requested API
     *
     * @param developerId The id of the developer
     * @param organizationId The id of the organization
     * @param apiId The id of the API
     * @param version The version of the API

     * @return The API Definition document (e.g. a Swagger JSON file).
     * @throws DeveloperNotFoundException
     * @throws NotAuthorizedException
     */
    @GET
    @Path("{developerId}/organizations/{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Produces({MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml"})
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API definition is successfully returned."),
            @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
    })
    Response getApiDefinition(@PathParam("developerId") String developerId,
                              @PathParam("organizationId") String organizationId,
                              @PathParam("apiId") String apiId,
                              @PathParam("version") String version)
            throws DeveloperNotFoundException, NotAuthorizedException;


    /**
     * Use this endpoint to retrieve a public API Definition
     *
     * @param organizationId The id of the organization
     * @param apiId The id of the API
     * @param version The version of the API

     * @return The API Definition document (e.g. a Swagger JSON file).
     * @throws NotAuthorizedException
     */
    @GET
    @Path("/organizations/{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Produces({MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml"})
    @Deprecated(forRemoval = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API definition is successfully returned."),
            @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
    })
    Response getPublicApiDefinition(@PathParam("organizationId") String organizationId,
                                    @PathParam("apiId") String apiId,
                                    @PathParam("version") String version)
            throws NotAuthorizedException;


}
