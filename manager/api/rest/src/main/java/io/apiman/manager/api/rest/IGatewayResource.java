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
import io.swagger.annotations.Api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The Gateway API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("gateways")
@Api
public interface IGatewayResource {

    /**
     * This endpoint is used to test the Gateway's settings prior to either creating
     * or updating it.  The information will be used to attempt to create a link between
     * the API Manager and the Gateway, by simply trying to ping the Gateway's "status"
     * endpoint.
     * @summary Test a Gateway
     * @servicetag admin
     * @param bean Details of the Gateway for testing.
     * @statuscode 200 If the test is performed (regardless of the outcome of the test).
     * @return The result of testing the Gateway settings.
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GatewayTestResultBean test(NewGatewayBean bean) throws NotAuthorizedException;

    /**
     * This endpoint returns a list of all the Gateways that have been configured.
     * @summary List All Gateways
     * @statuscode 200 If the gateways are successfully returned.
     * @return A list of configured Gateways.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<GatewaySummaryBean> list();

    /**
     * This endpoint is called to create a new Gateway.
     * @summary Create a Gateway
     * @servicetag admin
     * @param bean The details of the new Gateway.
     * @statuscode 200 If the Gateway is created successfully.
     * @return The newly created Gateway.
     * @throws GatewayAlreadyExistsException when the gateway already exists
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GatewayBean create(NewGatewayBean bean) throws GatewayAlreadyExistsException, NotAuthorizedException;

    /**
     * Call this endpoint to get the details of a single configured Gateway.
     * @summary Get a Gateway by ID
     * @servicetag admin
     * @param gatewayId The ID of the Gateway to get.
     * @statuscode If the Gateway is returned successfully.
     * @return The Gateway identified by {gatewayId}
     * @throws GatewayNotFoundException when gateway is not found
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @GET
    @Path("{gatewayId}")
    @Produces(MediaType.APPLICATION_JSON)
    public GatewayBean get(@PathParam("gatewayId") String gatewayId) throws GatewayNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update an existing Gateway.  Note that the name of the
     * Gateway cannot be changed, as the name is tied closely with the Gateway's
     * ID.  If you wish to rename the Gateway you must delete it and create a new
     * one.
     * @summary Update a Gateway
     * @servicetag admin
     * @param gatewayId The ID of the Gateway to update.
     * @param bean The Gateway information to update.  All fields are optional.
     * @statuscode 204 If the update is successful.
     * @throws GatewayNotFoundException when gateway is not found
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @PUT
    @Path("{gatewayId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("gatewayId") String gatewayId, UpdateGatewayBean bean)
            throws GatewayNotFoundException, NotAuthorizedException;

    /**
     * This endpoint deletes a Gateway by its unique ID.
     * @summary Delete a Gateway
     * @servicetag admin
     * @param gatewayId The ID of the Gateway to delete.
     * @statuscode 204 If the delete is successful.
     * @throws GatewayNotFoundException when gateway is not found
     * @throws NotAuthorizedException when attempt to do something user is not authorized to do
     */
    @DELETE
    @Path("{gatewayId}")
    public void delete(@PathParam("gatewayId") String gatewayId)
            throws GatewayNotFoundException, NotAuthorizedException;

    /**
     * This endpoint delivers the gateway endpoint for the corresponding gateway id
     * @param gatewayId gateway id
     * @return The corresponding gateway endpoint
     * @throws GatewayNotFoundException when gateway is not found
     */
    @GET
    @Path("{gatewayId}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public GatewayEndpointSummaryBean getGatewayEndpoint(@PathParam("gatewayId") String gatewayId) throws GatewayNotFoundException;
}
