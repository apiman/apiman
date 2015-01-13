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

package io.apiman.manager.api.rest.contract;

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.GatewayTestResultBean;
import io.apiman.manager.api.rest.contract.exceptions.GatewayAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;

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
public interface IGatewayResource {
    
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GatewayTestResultBean test(GatewayBean bean) throws NotAuthorizedException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<GatewaySummaryBean> list() throws NotAuthorizedException;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GatewayBean create(GatewayBean bean) throws GatewayAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{gatewayId}")
    @Produces(MediaType.APPLICATION_JSON)
    public GatewayBean get(@PathParam("gatewayId") String gatewayId) throws GatewayNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{gatewayId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("gatewayId") String gatewayId, GatewayBean bean)
            throws GatewayNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{gatewayId}")
    public void delete(@PathParam("gatewayId") String gatewayId)
            throws GatewayNotFoundException, NotAuthorizedException;

}
