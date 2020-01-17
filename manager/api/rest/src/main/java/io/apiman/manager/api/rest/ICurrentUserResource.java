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

import io.apiman.manager.api.beans.idm.CurrentUserBean;
import io.apiman.manager.api.beans.idm.UpdateUserBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.swagger.annotations.Api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The Current User API.  Returns information about the authenticated
 * user.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("currentuser")
@Api
public interface ICurrentUserResource {

    /**
     * Use this endpoint to get information about the currently authenticated user.
     * @summary Get Current User Information
     * @statuscode 200 If the information is correctly returned.
     * @return Information about the authenticated user.
     */
    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public CurrentUserBean getInfo();

    /**
     * This endpoint allows updating information about the authenticated user.
     * @summary Update Current User Information
     * @param info Data to use when updating the user.
     * @statuscode 204 If the update is successful.
     */
    @PUT
    @Path("info")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateInfo(UpdateUserBean info);

    /**
     * This endpoint returns a list of all the organizations for which the current user
     * has permission to edit clients.  For example, when creating a new Client,
     * the user interface must ask the user to choose within which Organization to create
     * it.  This endpoint lists the valid choices for the current user.
     * @summary Get Organizations (app-edit)
     * @statuscode 200 If the organizations are successfully returned.
     * @return A list of organizations.
     */
    @GET
    @Path("clientorgs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrganizationSummaryBean> getClientOrganizations();

    /**
     * This endpoint returns a list of all the organizations for which the
     * current user has permission to edit APIs. For example, when creating a
     * new API, the user interface must ask the user to choose within which
     * Organization to create it. This endpoint lists the valid choices for the
     * current user.
     * @summary Get Organizations (api-edit)
     * @statuscode 200 If the organizations are successfully returned.
     * @return A list of organizations.
     */
    @GET
    @Path("apiorgs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrganizationSummaryBean> getApiOrganizations();

    /**
     * This endpoint returns a list of all the organizations for which the current user
     * has permission to edit plans.  For example, when creating a new Plan,
     * the user interface must ask the user to choose within which Organization to create
     * it.  This endpoint lists the valid choices for the current user.
     * @summary Get Organizations (plan-edit)
     * @statuscode 200 If the organizations are successfully returned.
     * @return A list of organizations.
     */
    @GET
    @Path("planorgs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrganizationSummaryBean> getPlanOrganizations();

    /**
     * Use this endpoint to list all of the Clients the current user has permission
     * to edit.  This includes all Clients from all Organizations the user has
     * client edit privileges for.
     * @summary Get Current User's Clients
     * @statuscode 200 If the clients are successfully returned.
     * @return A list of Clients.
     */
    @GET
    @Path("clients")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientSummaryBean> getClients();

    /**
     * Use this endpoint to list all of the APIs the current user has permission
     * to edit.  This includes all APIs from all Organizations the user has
     * API edit privileges for.
     * @summary Get Current User's APIs
     * @statuscode 200 If the APIs are successfully returned.
     * @return A list of APIs.
     */
    @GET
    @Path("apis")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiSummaryBean> getApis();

}
