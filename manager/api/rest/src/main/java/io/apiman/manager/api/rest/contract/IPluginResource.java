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

import io.apiman.manager.api.beans.plugins.NewPluginBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.PluginAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PluginNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PluginResourceNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyDefinitionNotFoundException;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The Plugin API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("plugins")
public interface IPluginResource {

    /**
     * This endpoint returns a list of all plugins that have been added to the
     * system.
     * @summary List All Plugins
     * @statuscode 200 If the list of plugins is successfully returned.
     * @return A list of plugins.
     * @throws NotAuthorizedException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PluginSummaryBean> list() throws NotAuthorizedException;

    /**
     * Use this endpoint to add a plugin to apiman.  A plugin consists of the maven
     * coordinates of an artifact deployed to a remote maven repository (e.g. maven
     * central).
     * @summary Add a Plugin
     * @servicetag admin
     * @param bean The plugin to add.
     * @statuscode 200 If the plugin was added successfully.
     * @return Full details about the plugin that was added.
     * @throws PluginAlreadyExistsException
     * @throws PluginNotFoundException
     * @throws NotAuthorizedException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PluginBean create(NewPluginBean bean) throws PluginAlreadyExistsException, PluginNotFoundException, NotAuthorizedException;
    
    /**
     * This endpoint can be used to access the full information about an apiman
     * plugin.  The plugin is retrieved using the ID it was given when it was 
     * added.  The ID information can be retrieved by listing all plugins or 
     * remembered when a plugin is first added.
     * @summary Get Plugin by ID
     * @servicetag admin
     * @param pluginId
     * @statuscode 200 If the plugin exists and is returned.
     * @return An apiman plugin.
     * @throws PluginNotFoundException
     * @throws NotAuthorizedException
     */
    @GET
    @Path("{pluginId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PluginBean get(@PathParam("pluginId") Long pluginId) throws PluginNotFoundException, NotAuthorizedException;

    /**
     * Call this endpoint to delete a plugin.
     * @summary Delete a Plugin by ID
     * @servicetag admin
     * @statuscode 204 If the plugin was deleted successfully.
     * @param pluginId The plugin's ID.
     * @throws PluginNotFoundException
     * @throws NotAuthorizedException
     */
    @DELETE
    @Path("{pluginId}")
    public void delete(@PathParam("pluginId") Long pluginId)
            throws PluginNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all policy definitions contributed by the plugin.
     * @summary Get Plugin Policy Definitions
     * @param pluginId The plugin ID.
     * @statuscode 200 If the list of policy definitions is returned successfully.
     * @statuscode 404 If the plugin does not exist.
     * @return A list of policy definitions.
     * @throws PluginNotFoundException
     */
    @GET
    @Path("{pluginId}/policyDefs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicyDefinitionSummaryBean> getPolicyDefs(@PathParam("pluginId") Long pluginId)
            throws PluginNotFoundException;
    
    /**
     * Use this endpoint to retrieve the form associated with a particular policy
     * definition.  Plugins may contribute policy definitions to apiman.  Part of that
     * contribution *may* include a form for the UI to display when configuring an 
     * instance of the policy.  This endpoint returns this form.
     * @summary Get Plugin Policy Form
     * @param pluginId The plugin ID.
     * @param policyDefId The policy definition ID.
     * @statuscode 200 If the form is returned successfully.
     * @statuscode 404 If the plugin does not exist.
     * @statuscode 404 If the policy definition does not exist.
     * @statuscode 404 If the form does not exist.
     * @return A policy configuration form.
     * @throws PluginNotFoundException
     * @throws PluginResourceNotFoundException
     */
    @GET
    @Path("{pluginId}/policyDefs/{policyDefId}/form")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPolicyForm(@PathParam("pluginId") Long pluginId,
            @PathParam("policyDefId") String policyDefId) throws PluginNotFoundException,
            PolicyDefinitionNotFoundException, PluginResourceNotFoundException;

}
