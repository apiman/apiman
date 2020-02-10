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

import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.idm.UpdateUserBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.idm.UserPermissionsBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.swagger.annotations.Api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * The User API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("users")
@Api
public interface IUserResource {

    /**
     * Use this endpoint to get information about a specific user by the User ID.
     * @summary Get User by ID
     * @param userId The user ID.
     * @statuscode 200 If the user exists and information is returned.
     * @return Full user information.
     * @throws UserNotFoundException when specified user not found
     */
    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserBean get(@PathParam("userId") String userId) throws UserNotFoundException;

    /**
     * Use this endpoint to update the information about a user.  This will fail
     * unless the authenticated user is an admin or identical to the user being
     * updated.
     * @summary Update a User by ID
     * @param userId The user ID.
     * @param user Updated user information.
     * @statuscode 204 If the user information is successfully updated.
     * @throws UserNotFoundException when specified user not found
     * @throws NotAuthorizedException when not authorized to invoke this method
     */
    @PUT
    @Path("{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("userId") String userId, UpdateUserBean user) throws UserNotFoundException, NotAuthorizedException;

    /**
     * This endpoint returns the list of organizations that the user is a member of.  The
     * user is a member of an organization if she has at least one role for the org.
     * @summary List User Organizations
     * @param userId The user ID.
     * @statuscode 200 If the organization list is successfully returned.
     * @return List of organizations.
     */
    @GET
    @Path("{userId}/organizations")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrganizationSummaryBean> getOrganizations(@PathParam("userId") String userId);

    /**
     * This endpoint returns all clients that the user has permission to edit.
     * @summary List User Clients
     * @param userId The user ID.
     * @statuscode 200 If the client list is successfully returned.
     * @return List of clients.
     */
    @GET
    @Path("{userId}/clients")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientSummaryBean> getClients(@PathParam("userId") String userId);

    /**
     * This endpoint returns all APIs that the user has permission to edit.
     * @summary List User APIs
     * @param userId The user ID.
     * @statuscode 200 If the API list is successfully returned.
     * @return List of APIs.
     */
    @GET
    @Path("{userId}/apis")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiSummaryBean> getApis(@PathParam("userId") String userId);

    /**
     * Use this endpoint to get information about the user's audit history.  This
     * returns audit entries corresponding to each of the actions taken by the
     * user.  For example, when a user creates a new Organization, an audit entry
     * is recorded and would be included in the result of this endpoint.
     * @summary Get User Activity
     * @param userId The user ID.
     * @param page The page of the results to return.
     * @param pageSize The number of results per page to return.
     * @statuscode 200 If the activity is successfully returned.
     * @return List of audit entries.
     */
    @GET
    @Path("{userId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getActivity(@PathParam("userId") String userId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize);

    /**
     * This endpoint returns all of the permissions assigned to a specific user.
     * @summary Get User's Permissions
     * @servicetag admin
     * @param userId The user's ID.
     * @statuscode 200 If the permissions are successfully retrieved.
     * @return All of the user's permissions.
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user is not authorized to perform this action
     */
    @GET
    @Path("{userId}/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    public UserPermissionsBean getPermissionsForUser(@PathParam("userId") String userId)
            throws UserNotFoundException, NotAuthorizedException;

}
