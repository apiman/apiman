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

import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.UpdatePolicyDefinitionBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.PolicyDefinitionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PolicyDefinitionNotFoundException;
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
 * The Policy Definition API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("policyDefs")
@Api
public interface IPolicyDefinitionResource {

    /**
     * This endpoint returns a list of all policy definitions that have been added
     * to apiman.
     * @summary List Policy Definitions
     * @statuscode 200 If the policy definition list is successfully returned.
     * @return A list of policy definitions.
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicyDefinitionSummaryBean> list() throws NotAuthorizedException;

    /**
     * Use this endpoint to add a policy definition to apiman.  The policy definition
     * can optionall include the 'id' property.  If no 'id' is supplied, one will be
     * generated based on the name.
     * @summary Add Policy Definition
     * @servicetag admin
     * @param bean The policy definition to add.
     * @statuscode 200 If the policy definition is added successfully.
     * @return Details about the policy definition that was added.
     * @throws PolicyDefinitionAlreadyExistsException when trying to create a Policy Definition that already exists
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyDefinitionBean create(PolicyDefinitionBean bean) throws PolicyDefinitionAlreadyExistsException, NotAuthorizedException;
    
    /**
     * Use this endpoint to get a single policy definition by its ID.
     * @summary Get Policy Definition by ID
     * @param policyDefinitionId The ID of the policy definition.
     * @statuscode 200 If the policy definition is returned successfully.
     * @return A policy definition if found.
     * @throws PolicyDefinitionNotFoundException when trying to get, update, or delete a policy definition that does not exist
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @GET
    @Path("{policyDefinitionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyDefinitionBean get(@PathParam("policyDefinitionId") String policyDefinitionId) throws PolicyDefinitionNotFoundException, NotAuthorizedException;

    /**
     * Update the meta information about a policy definition.
     * @summary Update Policy Definition
     * @servicetag admin
     * @param policyDefinitionId The policy definition ID.
     * @param bean New meta-data for the policy definition.
     * @statuscode 204 If the update was successful.
     * @throws PolicyDefinitionNotFoundException when trying to get, update, or delete a policy definition that does not exist
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @PUT
    @Path("{policyDefinitionId}")
    public void update(@PathParam("policyDefinitionId") String policyDefinitionId, UpdatePolicyDefinitionBean bean)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete a policy definition by its ID.  If the policy definition
     * was added automatically from an installed plugin, this will fail.  The only way to 
     * remove such policy definitions is to remove the plugin.
     * @summary Delete policy definition.
     * @servicetag admin
     * @param policyDefinitionId The policy definition ID.
     * @statuscode 204 If the policy definition is successfully deleted.
     * @throws PolicyDefinitionNotFoundException when trying to get, update, or delete a policy definition that does not exist
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @DELETE
    @Path("{policyDefinitionId}")
    public void delete(@PathParam("policyDefinitionId") String policyDefinitionId)
            throws PolicyDefinitionNotFoundException, NotAuthorizedException;

}
