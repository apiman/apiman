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
import io.apiman.manager.api.beans.idm.CurrentUserBean;
import io.apiman.manager.api.beans.idm.UpdateUserBean;
import io.apiman.manager.api.beans.idm.UserDto;
import io.apiman.manager.api.beans.idm.UserPermissionsBean;
import io.apiman.manager.api.beans.notifications.NotificationCriteriaBean;
import io.apiman.manager.api.beans.notifications.dto.CreateNotificationFilterDto;
import io.apiman.manager.api.beans.notifications.dto.NotificationActionDto;
import io.apiman.manager.api.beans.notifications.dto.NotificationDto;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * The User API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("users")
@Tag(name = "Users")
public interface IUserResource {

    /**
     * Use this endpoint to get information about a specific user by the User ID.
     * @summary Get User by ID
     * @param userId The user ID.
     * @return Full user information.
     * @throws UserNotFoundException when specified user not found
     */
    @GET
    @Path("{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the user exists and information is returned.", useReturnTypeSchema = true)
    })
    UserDto get(@PathParam("userId") String userId) throws UserNotFoundException;

    /**
     * Use this endpoint to get information about the currently authenticated user.
     * @summary Get Current User Information
     * @return Information about the authenticated user.
     */
    @GET
    @Path("currentuser/info")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the information is correctly returned.", useReturnTypeSchema = true)
    })
    @Produces(MediaType.APPLICATION_JSON)
    CurrentUserBean getInfo();

    /**
     * Use this endpoint to update the information about a user.  This will fail
     * unless the authenticated user is an admin or identical to the user being
     * updated.
     * @summary Update a User by ID
     * @param userId The user ID.
     * @param user Updated user information.
     * @throws UserNotFoundException when specified user not found
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the user information is successfully updated.")
    })
    void update(@PathParam("userId") String userId, UpdateUserBean user) throws UserNotFoundException, NotAuthorizedException;

    /**
     * This endpoint returns the list of organizations that the user is a member of.  The
     * user is a member of an organization if she has at least one role for the org.
     * @summary List User Organizations
     * @param userId The user ID.
     * @return List of organizations.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/organizations")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the organization list is successfully returned.", useReturnTypeSchema = true)
    })
    List<OrganizationSummaryBean> getOrganizations(@PathParam("userId") String userId) throws NotAuthorizedException;

    /**
     * This endpoint returns all clients that the user has permission to view.
     * @summary List User Clients
     * @param userId The user ID.
     * @return List of clients.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/viewable-clients")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the client list is successfully returned.", useReturnTypeSchema = true)
    })
    List<ClientSummaryBean> getClients(@PathParam("userId") String userId) throws NotAuthorizedException;

    /**
     * This endpoint returns all clients that the user has permission to edit.
     * This endpoint is used in the UI for creating a contract - only show the clients the user has permissions to edit
     * @summary List User Clients
     * @param userId The user ID.
     * @return List of clients.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/editable-clients")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the client list is successfully returned.", useReturnTypeSchema = true)
    })
    List<ClientSummaryBean> getEditableClients(@PathParam("userId") String userId) throws NotAuthorizedException;

    /**
     * This endpoint returns a list of all the organizations for which the current user
     * has permission to edit clients.  For example, when creating a new Client,
     * the user interface must ask the user to choose within which Organization to create
     * it.  This endpoint lists the valid choices for the current user.
     * @summary Get Organizations (app-edit)
     * @return A list of organizations.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/clientorgs")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the organizations are successfully returned.", useReturnTypeSchema = true)
    })
    List<OrganizationSummaryBean> getClientOrganizations(@PathParam("userId") String userId) throws NotAuthorizedException;

    /**
     * This endpoint returns all APIs that the user has permission to view.
     * @summary List User APIs
     * @param userId The user ID.
     * @return List of APIs.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/apis")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API list is successfully returned.", useReturnTypeSchema = true)
    })
    List<ApiSummaryBean> getApis(@PathParam("userId") String userId) throws NotAuthorizedException;

    /**
     * This endpoint returns a list of all the organizations for which the
     * current user has permission to edit APIs. For example, when creating a
     * new API, the user interface must ask the user to choose within which
     * Organization to create it. This endpoint lists the valid choices for the
     * current user.
     * @summary Get Organizations (api-edit)
     * @return A list of organizations.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/apiorgs")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the organizations are successfully returned.", useReturnTypeSchema = true)
    })
    List<OrganizationSummaryBean> getApiOrganizations(@PathParam("userId") String userId) throws NotAuthorizedException;

    /**
     * This endpoint returns a list of all the organizations for which the current user
     * has permission to edit plans.  For example, when creating a new Plan,
     * the user interface must ask the user to choose within which Organization to create
     * it.  This endpoint lists the valid choices for the current user.
     * @summary Get Organizations (plan-edit)
     * @return A list of organizations.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/planorgs")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the organizations are successfully returned.", useReturnTypeSchema = true)
    })
    List<OrganizationSummaryBean> getPlanOrganizations(@PathParam("userId") String userId) throws NotAuthorizedException;

    /**
     * Use this endpoint to get information about the user's audit history.  This
     * returns audit entries corresponding to each of the actions taken by the
     * user.  For example, when a user creates a new Organization, an audit entry
     * is recorded and would be included in the result of this endpoint.
     * @summary Get User Activity
     * @param userId The user ID.
     * @param page The page of the results to return.
     * @param pageSize The number of results per page to return.
     * @return List of audit entries.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the activity is successfully returned.", useReturnTypeSchema = true)
    })
    SearchResultsBean<AuditEntryBean> getActivity(@PathParam("userId") String userId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws NotAuthorizedException;

    /**
     * This endpoint returns all the permissions assigned to a specific user.
     * @summary Get User's Permissions
     * @servicetag admin
     * @param userId The user's ID.
     * @return All the user's permissions.
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{userId}/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the permissions are successfully retrieved.", useReturnTypeSchema = true)
    })
    UserPermissionsBean getPermissionsForUser(@PathParam("userId") String userId)
            throws UserNotFoundException, NotAuthorizedException;

    /**
     * Search and filter user notifications.
     * <p>By default, this returns a paged list sorted in descending order (i.e. the latest notification first).
     *
     * @param userId The user ID.
     * @param criteria The search & filter criteria.
     * @return List of notifications.
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{userId}/notifications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the notifications are successfully retrieved.", useReturnTypeSchema = true)
    })
    SearchResultsBean<NotificationDto<?>> getNotificationsForUser(@PathParam("userId") String userId, NotificationCriteriaBean criteria)
         throws UserNotFoundException, NotAuthorizedException;

    /**
     * Get the number of notifications for a given user. Inspect the <sample>X-Total-Count</sample> header.
     *
     * <p>Users are only able to get information about their own notifications.
     *
     * @param userId the user ID.
     * @param includeDismissed whether to only count unread notifications (default: true).
     * @return X-Total-Count header with the number of unread notifications.
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @HEAD
    @Path("{userId}/notifications")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If user's notification metadata is successfully retrieved")
    })
    Response getNotificationCountForUser(@PathParam("userId") String userId, @DefaultValue("false") @QueryParam("includeDismissed") boolean includeDismissed)
         throws UserNotFoundException, NotAuthorizedException;

    /**
     * Mark a user's notifications with a specific state by ID (or all, if desired).
     *
     * <p>Users are only able to operate on their own notifications. Any attempt to operate on unowned
     * notifications will either be rejected or silently ignored.
     *
     * @param userId the user ID.
     * @param notificationAction the action to take on the notifications.
     * @return X-Total-Count header with the number of unread notifications.
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{userId}/notifications")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "202", description = "If the command to mark the user's notification was accepted")
    Response markNotifications(@PathParam("userId") String userId, @NotNull NotificationActionDto notificationAction)
         throws UserNotFoundException, NotAuthorizedException;


    @POST
    @Path("{userId}/notifications/filters")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createNotificationFilter(@PathParam("userId") String userId, CreateNotificationFilterDto createFilter);

}
