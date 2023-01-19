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

package io.apiman.manager.api.rest;

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.NewGatewayBean;
import io.apiman.manager.api.beans.gateways.UpdateGatewayBean;
import io.apiman.manager.api.beans.summary.GatewayEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.GatewayTestResultBean;
import io.apiman.manager.api.rest.exceptions.GatewayAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * The Gateway API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("gateways")
@Tag(name = "Gateways")
@PermitAll // Nobody without Apiman admin permissions can do these actions anyway, so no need to guard with IDM roles.
public interface IGatewayResource {

    /**
     * This endpoint is used to test the Gateway's settings prior to either creating
     * or updating it.  The information will be used to attempt to create a link between
     * the API Manager and the Gateway, by simply trying to ping the Gateway's "status"
     * endpoint.
     * @summary Test a Gateway
     * @servicetag admin
     * @param bean Details of the Gateway for testing.
     * @return The result of testing the Gateway settings.
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the test is performed (regardless of the outcome of the test).", useReturnTypeSchema = true)
    })
    GatewayTestResultBean test(@RequestBody NewGatewayBean bean) throws NotAuthorizedException;

    /**
     * This endpoint returns a list of all the Gateways that have been configured.
     * @summary List All Gateways
     * @return A list of configured Gateways.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the gateways are successfully returned.", useReturnTypeSchema = true)
    })
    List<GatewaySummaryBean> list();

    /**
     * This endpoint is called to create a new Gateway.
     * @summary Create a Gateway
     * @servicetag admin
     * @param bean The details of the new Gateway.
     * @return The newly created Gateway.
     * @throws GatewayAlreadyExistsException when the gateway already exists
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Gateway is created successfully.", useReturnTypeSchema = true)
    })
    GatewayBean create(@RequestBody NewGatewayBean bean) throws GatewayAlreadyExistsException, NotAuthorizedException;

    /**
     * Call this endpoint to get the details of a single configured Gateway.
     * @summary Get a Gateway by ID
     * @servicetag admin
     * @param gatewayId The ID of the Gateway to get.
     * @return The Gateway identified by {gatewayId}
     * @throws GatewayNotFoundException when gateway is not found
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @GET
    @Path("{gatewayId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "the Gateway is returned successfully.", useReturnTypeSchema = true)
    })
    GatewayBean get(
            @PathParam("gatewayId") @Parameter(description = "The ID of the Gateway to get") String gatewayId
    ) throws GatewayNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update an existing Gateway.  Note that the name of the
     * Gateway cannot be changed, as the name is tied closely with the Gateway's
     * ID.  If you wish to rename the Gateway you must delete it and create a new
     * one.
     * @summary Update a Gateway
     * @servicetag admin
     * @param gatewayId The ID of the Gateway to update.
     * @param bean The Gateway information to update.  All fields are optional.
     * @throws GatewayNotFoundException when gateway is not found
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @PUT
    @Path("{gatewayId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the update is successful.")
    })
    void update(
            @PathParam("gatewayId") @Parameter(description = "The ID of the Gateway to update") String gatewayId,
            @RequestBody UpdateGatewayBean bean
    ) throws GatewayNotFoundException, NotAuthorizedException;

    /**
     * This endpoint deletes a Gateway by its unique ID.
     * @summary Delete a Gateway
     * @servicetag admin
     * @param gatewayId The ID of the Gateway to delete.
     * @throws GatewayNotFoundException when gateway is not found
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @DELETE
    @Path("{gatewayId}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the delete is successful.")
    })
    void delete(
            @PathParam("gatewayId") @Parameter(description = "The ID of the Gateway to delete") String gatewayId
    ) throws GatewayNotFoundException, NotAuthorizedException;

    /**
     * This endpoint delivers the gateway endpoint for the corresponding gateway id
     * @deprecated no longer needed since addition of new developer portal (can use existing endpoints).
     * @param gatewayId gateway id
     * @return The corresponding gateway endpoint
     * @throws GatewayNotFoundException when gateway is not found
     */
    @GET
    @Path("{gatewayId}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(since = "1.3.0.Final", forRemoval = true)
    GatewayEndpointSummaryBean getGatewayEndpoint(
            @PathParam("gatewayId") String gatewayId
    ) throws GatewayNotFoundException;
}
