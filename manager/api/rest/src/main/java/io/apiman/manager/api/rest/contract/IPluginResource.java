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

import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.PluginAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PluginNotFoundException;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PluginSummaryBean> list() throws NotAuthorizedException;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PluginBean create(PluginBean bean) throws PluginAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{pluginId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PluginBean get(@PathParam("pluginId") Long pluginId) throws PluginNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{pluginId}")
    public void delete(@PathParam("pluginId") Long pluginId)
            throws PluginNotFoundException, NotAuthorizedException;

}
