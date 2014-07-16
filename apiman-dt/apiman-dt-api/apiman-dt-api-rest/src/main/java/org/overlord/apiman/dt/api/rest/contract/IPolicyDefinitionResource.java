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

package org.overlord.apiman.dt.api.rest.contract;

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

import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;

/**
 * The Policy Definition API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("policyDefs")
public interface IPolicyDefinitionResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicyDefinitionBean> list() throws NotAuthorizedException;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyDefinitionBean create(PolicyDefinitionBean bean) throws PolicyDefinitionAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{policyDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyDefinitionBean get(@PathParam("policyDefinitionId") String policyDefinitionId) throws PolicyDefinitionNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{policyDefinitionId}")
    public void update(@PathParam("policyDefinitionId") String policyDefinitionId, PolicyDefinitionBean bean)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{policyDefinitionId}")
    public void delete(@PathParam("policyDefinitionId") String policyDefinitionId)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException;

}
