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
import io.apiman.manager.api.beans.developers.DeveloperBean;
import io.apiman.manager.api.beans.developers.UpdateDeveloperBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.rest.exceptions.DeveloperAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.DeveloperNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * A interface for REST calls for the Developer Portal
 */
@Path("/developers")
@Api(tags = "Developers")
public interface IDeveloperResource {

    /**
     * Use this endpoint to get a list of all public ApiVersionBeans
     *
     * @return The list of all public api versions
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 200 If the list of public apis was successfully returned
     * @statuscode 403 If the access is not allowed
     */
    @GET
    @Path("apis")
    @Produces(MediaType.APPLICATION_JSON)
    List<ApiVersionBean> getAllPublicApiVersions() throws NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all developers
     *
     * @return The list of all developers
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 200 If the developer list was successfully returned
     * @statuscode 403 If the access is not allowed
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    List<DeveloperBean> getDevelopers() throws NotAuthorizedException;

    /**
     * Use this endpoint to create a new developer
     *
     * @param bean Information about the new developer
     * @return Full details about the developer that was created
     * @throws InvalidNameException
     * @throws NotAuthorizedException          when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 200 If the developer was successfully created
     * @statuscode 403 If the access is not allowed
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    DeveloperBean create(DeveloperBean bean) throws InvalidNameException, NotAuthorizedException, DeveloperAlreadyExistsException;

    /**
     * Use this endpoint to update an developer
     *
     * @param bean Information about the updated developer
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 204 If the developer was successfully updated
     * @statuscode 403 If the access is not allowed
     * @statuscode 404 If the developer does not exist.
     */
    @PUT
    @Path("{developerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    void update(@PathParam("developerId") String id, UpdateDeveloperBean bean) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single developer by its id
     *
     * @param id The id of the developer
     * @return The developer
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 200 If the developer was successfully returned
     * @statuscode 403 If the access is not allowed
     * @statuscode 404 If the developer does not exist.
     */
    @GET
    @Path("{developerId}")
    @Produces(MediaType.APPLICATION_JSON)
    DeveloperBean get(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete an developer
     *
     * @param id The id of the developer
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 204 If the developer was successfully deleted
     * @statuscode 403 If the access is not allowed
     * @statuscode 404 If the developer does not exist.
     */
    @DELETE
    @Path("{developerId}")
    void delete(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get all list of all ClientVersionSummaryBeans from all
     * clients that are mapped to this developer
     *
     * @param id The id of the developer
     * @return The list of ClientVersionSummaryBeans
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 200 If the the list was successfully returned
     * @statuscode 403 If the access is not allowed
     * @statuscode 404 If the developer does not exist.
     */
    @GET
    @Path("{developerId}/clients")
    @Produces(MediaType.APPLICATION_JSON)
    List<ClientVersionSummaryBean> getAllClientVersions(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get all list of all ContractSummaryBeans from all
     * clients that are mapped to this developer
     *
     * @param id The id of the developer
     * @return The list of ContractSummaryBeans
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 200 If the the list was successfully returned
     * @statuscode 403 If the access is not allowed
     * @statuscode 404 If the developer does not exist.
     */
    @GET
    @Path("{developerId}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    List<ContractSummaryBean> getAllClientContracts(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get all list of all ApiVersionBeans from all
     * clients that are mapped to this developer
     *
     * @param id The id of the developer
     * @return The list of ApiVersionBeans
     * @throws DeveloperNotFoundException when trying to get, update, or delete a developer that does not exist
     * @throws NotAuthorizedException     when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @statuscode 200 If the the list was successfully returned
     * @statuscode 403 If the access is not allowed
     * @statuscode 404 If the developer does not exist.
     */
    @GET
    @Path("{developerId}/apis")
    @Produces(MediaType.APPLICATION_JSON)
    List<ApiVersionBean> getAllApiVersions(@PathParam("developerId") String id) throws DeveloperNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve the API Definition if the user has a contract to
     * the requested API
     *
     * @param developerId The id of the developer
     * @param organizationId The id of the organization
     * @param apiId The id of the API
     * @param version The version of the API
     * @statuscode 200 If the API definition is successfully returned.
     * @statuscode 404 If the API version does not exist.
     * @return The API Definition document (e.g. a Swagger JSON file).
     * @throws DeveloperNotFoundException
     * @throws NotAuthorizedException
     */
    @GET
    @Path("{developerId}/organizations/{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Produces({MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml"})
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
     * @statuscode 200 If the API definition is successfully returned.
     * @statuscode 404 If the API version does not exist.
     * @return The API Definition document (e.g. a Swagger JSON file).
     * @throws NotAuthorizedException
     */
    @GET
    @Path("/organizations/{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Produces({MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml"})
    Response getPublicApiDefinition(@PathParam("organizationId") String organizationId,
                                    @PathParam("apiId") String apiId,
                                    @PathParam("version") String version)
            throws NotAuthorizedException;


}
