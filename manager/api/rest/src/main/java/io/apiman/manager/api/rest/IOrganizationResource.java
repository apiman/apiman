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

import io.apiman.manager.api.beans.apis.ApiVersionStatusBean;
import io.apiman.manager.api.beans.apis.NewApiBean;
import io.apiman.manager.api.beans.apis.NewApiDefinitionBean;
import io.apiman.manager.api.beans.apis.NewApiVersionBean;
import io.apiman.manager.api.beans.apis.UpdateApiBean;
import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;
import io.apiman.manager.api.beans.apis.dto.ApiBeanDto;
import io.apiman.manager.api.beans.apis.dto.ApiPlanOrderDto;
import io.apiman.manager.api.beans.apis.dto.ApiVersionBeanDto;
import io.apiman.manager.api.beans.apis.dto.KeyValueTagDto;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ApiKeyBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.clients.NewClientBean;
import io.apiman.manager.api.beans.clients.NewClientVersionBean;
import io.apiman.manager.api.beans.clients.UpdateClientBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.idm.GrantRolesBean;
import io.apiman.manager.api.beans.members.MemberBean;
import io.apiman.manager.api.beans.metrics.ClientUsagePerApiBean;
import io.apiman.manager.api.beans.metrics.HistogramIntervalType;
import io.apiman.manager.api.beans.metrics.ResponseStatsHistogramBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerClientBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsPerPlanBean;
import io.apiman.manager.api.beans.metrics.ResponseStatsSummaryBean;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsagePerClientBean;
import io.apiman.manager.api.beans.metrics.UsagePerPlanBean;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.orgs.UpdateOrganizationBean;
import io.apiman.manager.api.beans.plans.NewPlanBean;
import io.apiman.manager.api.beans.plans.NewPlanVersionBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plans.UpdatePlanBean;
import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.policies.UpdatePolicyBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.rest.exceptions.ApiAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.exceptions.ApiVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ClientNotFoundException;
import io.apiman.manager.api.rest.exceptions.ClientVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ClientVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.exceptions.EntityStillActiveException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidClientStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidMetricCriteriaException;
import io.apiman.manager.api.rest.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.exceptions.InvalidPlanStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanVersionAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
 * The Organization API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("organizations")
@Tag(name = "Organizations")
public interface IOrganizationResource {

    /**
     * Use this endpoint to create a new Organization.
     * @return Full details about the Organization that was created.
     * @throws OrganizationAlreadyExistsException when trying to create an Organization that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts to create an Organization with an invalid name
     */
    @RolesAllowed("apiuser")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create Organization",
            description = "Create a new Organization. This can be considered a type of namespace. " +
                    "APIs, Clients and Plans are defined within an organization. " +
                    "Using other API calls, you can add users to an organization and assign them fine-grained permissions"
    )
    @ApiResponse(responseCode = "200", description = "If the Organization was successfully created.", useReturnTypeSchema = true)
    OrganizationBean createOrg(NewOrganizationBean bean)
            throws OrganizationAlreadyExistsException, NotAuthorizedException, InvalidNameException;

    /**
     * Delete an org
     * @throws OrganizationNotFoundException when the specified organization does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws EntityStillActiveException when user attempts to delete an organization which still has active sub-elements
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}")
    @Operation(summary = "Delete an organization")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Organization was successfully deleted"),
            @ApiResponse(responseCode = "409", description = "If the delete preconditions have not been met (i.e. sub-elements are still active, such as still-published APIs).")
    })
    void deleteOrg(
            @PathParam("organizationId") @Parameter(description = "The Organization ID to delete") String organizationId
    ) throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException;

    /**
     * Use this endpoint to get information about a single Organization
     * by its ID.
     * @return The Organization.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     */
    @PermitAll
    @GET
    @Path("{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Organization By ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Organization was successfully returned", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist")
    })
    OrganizationBean getOrg(
            @PathParam("organizationId") @Parameter(description = "The Organization id.") String organizationId
    ) throws OrganizationNotFoundException;

    /**
     * Updates meta-information about a single Organization.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @PUT
    @Path("{organizationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update Organization By ID",
            description = "Updates meta-information about a single Organization"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Organization meta-data is successfully updated."),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    void updateOrg(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            UpdateOrganizationBean bean
    ) throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Returns audit activity information for a single Organization.  The audit
     * information that is returned represents all the activity associated
     * with this Organization (i.e. an audit log for everything in the Organization).
     * @return List of audit/activity entries.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get Organization Activity",
            description = "Returns audit activity information for a single Organization. " +
                    "The audit information that is returned represents all the activity associated with the specified Organization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    SearchResultsBean<AuditEntryBean> getOrgActivity(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @QueryParam("page") @Parameter(description = "Which page of activity results to return.") int page,
            @QueryParam("count") @Parameter(description = "The number of entries per page.") int pageSize
    ) throws OrganizationNotFoundException, NotAuthorizedException;

    /*
     * APPLICATIONS
     */

    /**
     * Use this endpoint to create a new Client.  Note that it is important to also
     * create an initial version of the Client (e.g. 1.0).  This can either be done
     * by including the 'initialVersion' property in the request, or by immediately following
     * up with a call to "Create Client Version".  If the former is done, then a first
     * Client version will be created automatically by this endpoint.
     * @return Full details about the newly created Client.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientAlreadyExistsException when trying to create a Client that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts to create a client with an invalid name
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create Client",
            description = "Use this endpoint to create a new Client." +
                    "It is important to also create an initial version of the Client (e.g. 1.0). " +
                    "This can either be done by including the 'initialVersion' property in the request, or by immediately following up with a call to Create Client Version " +
                    "If the former is done, then a first  Client version will be created automatically by this endpoint."
            )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Client is successfully created.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    ClientBean createClient(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            NewClientBean bean
    ) throws OrganizationNotFoundException, ClientAlreadyExistsException, NotAuthorizedException, InvalidNameException;

    /**
     * Delete a ClientApp
     * @throws OrganizationNotFoundException when the specified organization does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws EntityStillActiveException when user attempts to delete a Client which still has active sub-elements
     */
    @PermitAll
    @DELETE
    @Path("{organizationId}/clients/{clientId}")
    @Operation(summary = "Delete a client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Organization was successfully deleted"),
            @ApiResponse(responseCode = "409", description = "If the delete preconditions have not been met (i.e. sub-elements are still active, such as still-registered ClientVersions).")
    })
    void deleteClient(
            @PathParam("organizationId") @Parameter(description = "The Organization ID the client exists within") String organizationId,
            @PathParam("clientId") @Parameter(description = "The ClientApp ID to delete") String clientId
    ) throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException;

    /**
     * Use this endpoint to retrieve information about a single Client by ID.  Note
     * that this only returns information about the Client, not about any particular
     * *version* of the Client.
     * @return A Client.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist when trying to get, update, or delete a client that does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get Client By ID",
            description = "Use this endpoint to retrieve information about a single Client by ID. " +
                    "This only returns information about the Client, not any particular version of the client."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Client is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    ClientBean getClient(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId
    ) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * This endpoint returns audit activity information about the Client.
     * @return A list of audit activity entries.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get Client Activity",
            description = "audit activity information about the Client."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    SearchResultsBean<AuditEntryBean> getClientActivity(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @QueryParam("page") @Parameter(description = "Which page of activity should be returned.") int page,
            @QueryParam("count") @Parameter(description = "The number of entries per page to return.") int pageSize
    ) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Clients in the Organization.
     * @return A list of Clients.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Clients")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Clients is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    List<ClientSummaryBean> listClients(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId
    ) throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about an Client.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @PUT
    @Path("{organizationId}/clients/{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update Client")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Client is updated successfully."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    void updateClient(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            UpdateClientBean bean
    ) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the Client.
     * @return Full details about the newly created Client version.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version value
     * @throws ClientVersionAlreadyExistsException when the client version for the given ID already exists
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients/{clientId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create Client Version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Client version is created successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
            @ApiResponse(responseCode = "409", description = "If the Client version already exists.")
    })
    ClientVersionBean createClientVersion(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            NewClientVersionBean bean
    ) throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException, ClientVersionAlreadyExistsException;

    /**
     * Use this endpoint to list all the versions of a Client.
     * @return A list of Clients.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Client Versions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Client versions is successfully returned.", useReturnTypeSchema = true)
    })
    List<ClientVersionSummaryBean> listClientVersions(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId
    ) throws ClientNotFoundException, NotAuthorizedException;


    /**
     * Use this endpoint to update the API Key for the given client.  You can either
     * provide your own custom (must be unique) API Key, or you can send an empty request
     * and apiman will generate a new API key for you.  Note that if the client is already
     * registered with one or more Gateways, this call will fail (the API Key can only be
     * modified if the client is not currently registered).
     * @return The new API Key value.
     * @throws ClientNotFoundException when the client does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version of the client
     * @throws InvalidClientStatusException when the client is not in the proper status
     */
    @PermitAll
    @PUT
    @Path("{organizationId}/clients/{clientId}/versions/{version}/apikey")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Update API Key",
            description = "Update the API Key for the given client. " +
                    "You can either provide your own custom (must be unique) API Key, or you can send an empty request and Apiman will generate a new API key for you. " +
                    "If the client is registered with one or more gateways, this call will fail (API Key can only be modified if the client is not currently registered).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Client's API Key is successfully updated.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
            @ApiResponse(responseCode = "409", description = "If the Client has the wrong status.")
    })
    ApiKeyBean updateClientApiKey(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client Version.") String version,
            ApiKeyBean bean
    ) throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException, InvalidClientStatusException;

    /**
     * Use this endpoint to get the client's current API Key.  This call will fail if
     * you do not have the proper permission to see the information.
     * @return The API Key value.
     * @throws ClientNotFoundException when the client does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version of the client
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/apikey")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Key")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Client's API Key is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    ApiKeyBean getClientApiKey(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client Version.") String version
    ) throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * a Client.
     * @return A Client version.
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Client Version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Client version is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client version does not exist.")
    })
    ClientVersionBean getClientVersion(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version
    ) throws ClientVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * Client.
     * @return A list of audit entries.
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Client Version Activity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the audit activity entries are successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client version does not exist.")
    })
    SearchResultsBean<AuditEntryBean> getClientVersionActivity(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @QueryParam("page") @Parameter(description = "Which page of activity data to return.") int page,
            @QueryParam("count") @Parameter(description = "The number of entries per page to return.") int pageSize
    ) throws ClientVersionNotFoundException, NotAuthorizedException;

    /**
     * Retrieves metrics/analytics information for a specific client.  This will
     * return request count data broken down by API.  It basically answers
     * the question "which APIs is my client really using?".
     *
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/metrics/apiUsage")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Client Usage Metrics (per API)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    ClientUsagePerApiBean getClientUsagePerApi(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The client version.") String version,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;


    /**
     * Use this endpoint to create a Contract between the Client and an API.  In order
     * to create a Contract, the caller must specify the Organization, ID, and Version of the
     * API.  Additionally, the caller must specify the ID of the Plan it wants to use for
     * the Contract with the API.
     * @return Full details about the newly created Contract.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws ContractAlreadyExistsException when trying to create an Contract that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create an API Contract",
            description = "Create a Contract between the Client and an API. " +
                    "In order to create a Contract, the caller must specify the Organization, ID, and Version of the API." +
                    "Additionally, the caller must specify the ID of the plan it wants to use for the contract with the API."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Contract is successfully created.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client version does not exist.")
    })
    ContractBean createContract(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            NewContractBean bean
    ) throws OrganizationNotFoundException, ClientNotFoundException, ApiNotFoundException, PlanNotFoundException,
            ContractAlreadyExistsException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve detailed information about a single API Contract
     * for a Client.
     * @return Details about a single Contract.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws ContractNotFoundException when trying to get, update, or delete a contract that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts/{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get API Contract",
            description = "Detailed information about a single API Contract for a Client"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Contract is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client version does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Contract is not found.")
    })
    ContractBean getContract(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @PathParam("contractId") @Parameter(description = "The ID of the Contract.") Long contractId
    ) throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to probe a specific API contract's policy state
     * @return A policy probe response
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts/{contractId}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Probe a policy associated with a contract")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Probe executed successfully"),
    })
    Response probeContractPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @PathParam("contractId") @Parameter(description = "The contract ID") Long contractId,
            @PathParam("policyId") @Parameter(description = "The policy ID (policy you want to probe)") long policyId,
            @RequestBody(description = "The probe payload (refer to the documentation of the probe you want to use for the correct format).") String rawProbePayload
    ) throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Contracts for an Client.
     * @return A list of Contracts.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List All Contracts for a Client")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Contracts is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client is not found.")
    })
    List<ContractSummaryBean> getClientVersionContracts(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version
    ) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get registry style information about all APIs that this
     * Client consumes.  This is a useful endpoint to invoke in order to retrieve
     * a summary of every API consumed by the client.  The information returned
     * by this endpoint could potentially be included directly in a client
     * as a way to lookup endpoint information for the APIs it wishes to consume.  This
     * variant of the API Registry is formatted as JSON data.
     * <p>
     * Note that, optionally, you can generate a temporary download link instead of
     * getting the registry file directly.  To do this, simply pass download=true as
     * a query parameter.  The result will then be a JSON object with information about
     * the temporary download link.  The ID of the download can then be used when making
     * a call to the /downloads/{downloadId} endpoint to fetch the actual content.
     *
     * @return API Registry information or temporary download information.
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/apiregistry/json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Registry (JSON)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API Registry information is successfully returned."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    Response getApiRegistryJSON(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @QueryParam("download") @Parameter(description = "Query parameter set to true in order to generate a download link.") String download
    ) throws ClientNotFoundException, NotAuthorizedException;

    Response getApiRegistryJSONInternal(String organizationId, String clientId, String version) throws ClientVersionNotFoundException;

    /**
     * Use this endpoint to get registry style information about all APIs that this
     * Client consumes.  This is a useful endpoint to invoke in order to retrieve
     * a summary of every API consumed by the client.  The information returned
     * by this endpoint could potentially be included directly in a client
     * as a way to lookup endpoint information for the APIs it wishes to consume.  This
     * variant of the API Registry is formatted as XML data.
     * <p>
     * Note that, optionally, you can generate a temporary download link instead of
     * getting the registry file directly.  To do this, simply pass download=true as
     * a query parameter.  The result will then be a JSON object with information about
     * the temporary download link.  The ID of the download can then be used when making
     * a call to the /downloads/{downloadId} endpoint to fetch the actual content.
     *
     * @return API Registry information.
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/apiregistry/xml")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Operation(summary = "Get API Registry (XML)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API Registry information is successfully returned."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    Response getApiRegistryXML(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @QueryParam("download") @Parameter(description = "Query parameter set to true in order to generate a download link.") String download
    ) throws ClientNotFoundException, NotAuthorizedException;
    Response getApiRegistryXMLInternal(String organizationId, String clientId, String version) throws ClientVersionNotFoundException;

    /**
     * Use this endpoint to break all contracts between this client and its APIs.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @DELETE
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts")
    @Operation(summary = "Break All Contracts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the operation is successful."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    void deleteAllContracts(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version
    ) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to break a Contract with an API.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws ContractNotFoundException when trying to get, update, or delete a contract that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidClientStatusException when the client is not in the proper status
     */
    @PermitAll
    @DELETE
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts/{contractId}")
    @Operation(summary = "Break Contract")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Contract is successfully broken."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Contract does not exist.")
    })
    void deleteContract(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @PathParam("contractId") @Parameter(description = "The Contract ID.") Long contractId
    ) throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException, InvalidClientStatusException;

    /**
     * Use this endpoint to add a new Policy to the Client version.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add Client Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Policy is successfully added.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    PolicyBean createClientPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId")  @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            NewPolicyBean bean
    ) throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the Client version.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Client Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Policy is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    PolicyBean getClientPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @PathParam("policyId") @Parameter(description = "The Policy ID.") long policyId
    ) throws OrganizationNotFoundException, ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single Client Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @PUT
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update Client Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Policy was successfully updated."),
            @ApiResponse(responseCode = "404", description = "If the Organization, Client, or Policy does not exist.")
    })
    void updateClientPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            @PathParam("policyId") @Parameter(description = "The Policy ID.") long policyId,
            UpdatePolicyBean bean
    ) throws OrganizationNotFoundException, ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the Client.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @DELETE
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies/{policyId}")
    @Operation(summary = "Remove Client Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Policy was successfully deleted."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
    })
    void deleteClientPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version")  @Parameter(description = "The Client version.") String version,
            @PathParam("policyId") @Parameter(description = "The Policy ID.") long policyId
    ) throws OrganizationNotFoundException, ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all the Policies configured for the Client.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List All Client Policies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Policies is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    List<PolicySummaryBean> listClientPolicies(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.")  String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version
    ) throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to change the order of Policies for an Client.  When a
     * Policy is added to the Client, it is added as the last Policy in the list
     * of Client Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all the Policies for the Client, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Re-Order Client Policies")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the re-ordering of Policies was successful."),
            @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
    })
    void reorderClientPolicies(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("clientId") @Parameter(description = "The Client ID.") String clientId,
            @PathParam("version") @Parameter(description = "The Client version.") String version,
            PolicyChainBean policyChain
    ) throws OrganizationNotFoundException, ClientVersionNotFoundException, NotAuthorizedException;

    /*
     * APIS
     */

    /**
     * Use this endpoint to create a new API.  Note that it is important to also
     * create an initial version of the API (e.g. 1.0).  This can either be done
     * by including the 'initialVersion' property in the request, or by immediately following
     * up with a call to "Create API Version".  If the former is done, then a first
     * API version will be created automatically by this endpoint.
     * @return Full details about the newly created API.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiAlreadyExistsException when trying to create an API that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts the create with an invalid name
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/apis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API is successfully created.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    ApiBeanDto createApi(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @NotNull NewApiBean bean
    ) throws OrganizationNotFoundException, ApiAlreadyExistsException, NotAuthorizedException, InvalidNameException;

    /**
     * Use this endpoint to get a list of all APIs in the Organization.
     * @return A list of APIs.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List APIs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of APIs is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    List<ApiSummaryBean> listApis(@PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId)
            throws OrganizationNotFoundException;

    /**
     * Use this endpoint to retrieve information about a single API by ID.  Note
     * that this only returns information about the API, not about any particular
     * *version* of the API.
     * @return A API.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API By ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    ApiBeanDto getApi(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId
    ) throws ApiNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about an API.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/apis/{apiId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update API")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the API is updated successfully."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    void updateApi(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            UpdateApiBean bean
    ) throws ApiNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete an API Image
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/apis/{apiId}/image")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete API Image")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the API is updated successfully."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    void deleteApiImage(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId
    ) throws ApiNotFoundException, NotAuthorizedException;

    @RolesAllowed("apiuser")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{organizationId}/apis/{apiId}/tags")
    @Operation(summary = "Tag an API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag was created successfully.")
    })
    void tagApi(
            @PathParam("organizationId") @Parameter(description = "Organization ID") String organizationId,
            @PathParam("apiId") @Parameter(description = "API ID") String apiId,
            KeyValueTagDto bean
    ) throws ApiNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete an API.  There are multiple restrictions on this capability.  Specifically,
     * the API must not have any published versions.  If you try to delete an API with one or more published
     * versions, it will fail with an {@link EntityStillActiveException} error.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws EntityStillActiveException when user attempts to delete an API which still has active sub-elements
     * @throws InvalidApiStatusException when the API's status is invalid for the current action
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/apis/{apiId}")
    @Operation(summary = "Delete API")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the API is updated successfully."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist."),
            @ApiResponse(responseCode = "409", description = "If the API cannot be deleted.")
    })
    void deleteApi(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId
    ) throws ApiNotFoundException, NotAuthorizedException, InvalidApiStatusException;

    /**
     * This endpoint returns audit activity information about the API.
     * @return A list of audit activity entries.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Activity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    SearchResultsBean<AuditEntryBean> getApiActivity(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @QueryParam("page") @Parameter(description = "Which page of activity should be returned.") int page,
            @QueryParam("count") @Parameter(description = "The number of entries per page to return.") int pageSize
    ) throws ApiNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the API.
     * @return Full details about the newly created API version.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version value
     * @throws ApiVersionAlreadyExistsException when the API version with the given ID already exists
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/apis/{apiId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create API Version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API version is created successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist."),
            @ApiResponse(responseCode = "409", description = "If the API version already exists.")
    })
    ApiVersionBeanDto createApiVersion(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            NewApiVersionBean bean
    ) throws ApiNotFoundException, NotAuthorizedException, InvalidVersionException, ApiVersionAlreadyExistsException;

    /**
     * Use this endpoint to list all the versions of an API.
     * If the user has not "apiView" permissions, sensitive data won't be returned.
     * @return A list of APIs.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List API Versions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of API versions is successfully returned.", useReturnTypeSchema = true)
    })
    List<ApiVersionSummaryBean> listApiVersions(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId
    ) throws ApiNotFoundException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * an API. If the user has not "apiView" permissions, sensitive data won't be returned.
     * @return A API version.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API version is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
    })
    ApiVersionBeanDto getApiVersion(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version")  @Parameter(description = "The API version.") String version
    ) throws ApiVersionNotFoundException;

    /**
     * Use this endpoint to get detailed status information for a single version of an
     * API.  This is useful to figure out why an API is not yet in the 'Ready'
     * state (which is required before it can be published to a Gateway).
     *
     * @return Status information about an API version.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Version Status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the status information is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
    })
    ApiVersionStatusBean getApiVersionStatus(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version
    ) throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve the API's definition document.  An API
     * definition document can be several types, depending on the API
     * type and technology used to define the API.  For example, this endpoint
     * might return a WSDL document, or a Swagger JSON document.
     * @return The API Definition document (e.g. a Swagger JSON file).
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Produces({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    @Operation(summary = "Get API Definition")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API definition is successfully returned."),
            @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
    })
    Response getApiDefinition(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version")  @Parameter(description = "The API version.") String version
    ) throws ApiVersionNotFoundException;

    /**
     * Use this endpoint to get information about the Managed API's gateway
     * endpoint.  In other words, this returns the actual live endpoint on the
     * API Gateway - the endpoint that a client should use when invoking the API.
     * @return The live API endpoint information.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws InvalidApiStatusException when the API's status is invalid for the current action
     * @throws GatewayNotFoundException when trying to get, update, or delete a gateway that does not exist
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/endpoint")
    @Operation(summary = "Get API Endpoint")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the endpoint information is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    @Produces(MediaType.APPLICATION_JSON)
    ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version
    ) throws ApiVersionNotFoundException, InvalidApiStatusException, GatewayNotFoundException;

    /**
     * Use this endpoint to update information about a single version of an API.
     * @return The updated API Version.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidApiStatusException when the user attempts some action on the API when it is not in an appropriate state/status
     */
    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update API Version")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the API version information was successfully updated.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    ApiVersionBeanDto updateApiVersion(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @Valid UpdateApiVersionBean bean
    ) throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;

    /**
     * Use this endpoint to update the API's definition document.  An API
     * definition will vary depending on the type of API, and the type of
     * definition used.  For example, it might be a Swagger document or a WSDL file.
     * To use this endpoint, simply PUT the updated API Definition document
     * in its entirety, making sure to set the Content-Type appropriately for the
     * type of definition document.  The content will be stored and the API's
     * "Definition Type" field will be updated.
     * <br />
     * Whenever an API's definition is updated, the "definitionType" property of
     * that API is automatically set based on the request Content-Type.  There
     * is no other way to set the API's definition type property.  The following
     * is a map of Content-Type to API definition type.
     *
     * <table>
     *   <thead>
     *     <tr><th>Content Type</th><th>API Definition Type</th></tr>
     *   </thead>
     *   <tbody>
     *     <tr><td>application/json</td><td>SwaggerJSON</td></tr>
     *     <tr><td>application/x-yaml</td><td>SwaggerYAML</td></tr>
     *     <tr><td>application/wsdl+xml</td><td>WSDL</td></tr>
     *   </tbody>
     * </table>
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidApiStatusException when the user attempts some action on the API when it is not in an appropriate state/status
     */
    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Consumes({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    @Operation(summary = "Update API Definition")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the API definition was successfully updated."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    void updateApiDefinition(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version
    ) throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;

    /**
     * Use this endpoint to update the API's definition document by providing
     * a URL (reference) to the definition.  This is an alternative to providing the
     * full API definition document via a PUT to the same endpoint.  This endpoint
     * can be used to either add a definition if one does not already exist, as well
     * as update/replace an existing definition.
     * <p>
     * Note that apiman will not store the definition reference, but instead will
     * download the API definition document and store it.  Additionally, the
     * API's "Definition Type" field will be updated.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidApiStatusException when the user attempts some action on the API when it is not in an appropriate state/status
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update API Definition from URL")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the API definition was successfully updated."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    void updateApiDefinitionFromURL(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            NewApiDefinitionBean bean
    ) throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * API.
     * @return A list of audit entries.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Version Activity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit activity entries", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "API version does not exist.")
    })
    SearchResultsBean<AuditEntryBean> getApiVersionActivity(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @QueryParam("page") @Parameter(description = "Which page of activity data to return.") int page,
            @QueryParam("count") @Parameter(description = "The number of entries per page to return.") int pageSize
    ) throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list the Plans configured for the given API version.
     * @return A list of API plans.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List API Plans")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the API plans are successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API cannot be found.")
    })
    List<ApiPlanSummaryBean> getApiVersionPlans(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version
    ) throws ApiVersionNotFoundException;

    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}/reorderApiPlans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Reorder API plans",
            description = "Reorder API plans, which affects the order they are displayed in the API Developer Portal."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reordering of plans was successful."),
            @ApiResponse(responseCode = "404", description = "If the Organization, API or Api Version cannot be found"),
            @ApiResponse(responseCode = "401", description = "If invalid data is provided, such as plans that exist but have not been attached to the API")
    })
    void reorderApiPlans(
            @PathParam("organizationId") @Parameter(description = "The Organization ID") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.")  String version,
            ApiPlanOrderDto apiPlanOrder
    ) throws ApiVersionNotFoundException;

    /**
     * Use this endpoint to add a new Policy to the API version.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add API Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Full details about the newly added Policy", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    PolicyBean createApiPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            NewPolicyBean bean
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the API version.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Policy is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    PolicyBean getApiPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @PathParam("policyId") @Parameter(description = "The Policy ID.") long policyId
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single API Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update API Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Policy was successfully updated."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
    })
    void updateApiPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @PathParam("policyId") @Parameter(description = "The Policy ID.") long policyId,
            UpdatePolicyBean requestBody
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the API.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies/{policyId}")
    @Operation(summary = "Remove API Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Policy was successfully deleted."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
    })
    void deleteApiPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @PathParam("policyId") @Parameter(description = "The Policy ID.") long policyId
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete an API's definition document.  When this
     * is done, the "definitionType" field on the API will be set to None.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Operation(summary = "Remove API Definition")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the API definition was successfully deleted."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    void deleteApiDefinition(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all the Policies configured for the API.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List All API Policies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Policies is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    List<PolicySummaryBean> listApiPolicies(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to change the order of Policies for an API.  When a
     * Policy is added to the API, it is added as the last Policy in the list
     * of API Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all the Policies for the API, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/apis/{apiId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Re-Order API Policies")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the re-ordering of Policies was successful."),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    void reorderApiPolicies(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.")  String version,
            PolicyChainBean policyChain
    ) throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a Policy Chain for the specific API version.  A
     * Policy Chain is a useful summary to better understand which Policies would be
     * executed for a request to this API through a particular Plan offered by the
     * API.  Often this information is interesting prior to create a Contract with
     * the API.
     * @return A Policy Chain.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/plans/{planId}/policyChain")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Policy Chain")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Policy Chain is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    PolicyChainBean getApiPolicyChain(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @PathParam("planId")  @Parameter(description = "The Plan ID.") String planId
    ) throws ApiVersionNotFoundException;

    /**
     * Use this endpoint to get a list of all Contracts created with this API.  This
     * will return Contracts created by between any Client and through any Plan.
     * @return A list of Contracts.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List API Contracts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Contracts is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the API does not exist.")
    })
    List<ContractSummaryBean> getApiVersionContracts(
            @PathParam("organizationId")  @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @QueryParam("page") @Parameter(description = "Which page of Contracts to return.") int page,
            @QueryParam("count") @Parameter(description = "The number of Contracts per page to return.") int pageSize
    ) throws ApiVersionNotFoundException, NotAuthorizedException;

    /*
     * PLANS
     */

    /**
     * Use this endpoint to create a new Plan.  Note that it is important to also
     * create an initial version of the Plan (e.g. 1.0).  This can either be done
     * by including the 'initialVersion' property in the request, or by immediately following
     * up with a call to "Create Plan Version".  If the former is done, then a first
     * Plan version will be created automatically by this endpoint.
     * @return Full details about the newly created Plan.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanAlreadyExistsException when trying to create an Plan that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts the create with an invalid name
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/plans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create Plan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Plan is successfully created.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    PlanBean createPlan(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            NewPlanBean bean
    ) throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException, InvalidNameException;

    /**
     * Use this endpoint to retrieve information about a single Plan by ID.  Note
     * that this only returns information about the Plan, not about any particular
     * *version* of the Plan.
     * @return A Plan.
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/plans/{planId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Plan By ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Plan is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
    })
    PlanBean getPlan(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId
    ) throws PlanNotFoundException, NotAuthorizedException;

    /**
     * This endpoint returns audit activity information about the Plan.
     * @return A list of audit activity entries.
     * when trying to get, update, or delete a plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete a plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @GET
    @Path("{organizationId}/plans/{planId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Plan Activity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
    })
    SearchResultsBean<AuditEntryBean> getPlanActivity(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            @QueryParam("page") @Parameter(description = "Which page of activity should be returned.") int page,
            @QueryParam("count") @Parameter(description = "The number of entries per page to return.") int pageSize
    ) throws PlanNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Plans in the Organization.
     * @return A list of Plans.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Plans")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Plans is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    List<PlanSummaryBean> listPlans(@PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about a Plan.
     * @throws PlanNotFoundException  when trying to get, update, or delete a plan that does not exist
     * when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/plans/{planId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update Plan")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Plan is updated successfully."),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
    })
    void updatePlan(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            UpdatePlanBean bean
    ) throws PlanNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the Plan.
     * @return Full details about the newly created Plan version.
     * when trying to get, update, or delete a plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete a plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version value
     * @throws PlanVersionAlreadyExistsException when the plan version with the given ID already exists
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/plans/{planId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create Plan Version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Plan version is created successfully.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
            @ApiResponse(responseCode = "409", description = "If the Plan version already exists.")
    })
    PlanVersionBean createPlanVersion(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId")  @Parameter(description = "The Plan ID.") String planId,
            NewPlanVersionBean bean
    ) throws PlanNotFoundException, NotAuthorizedException, InvalidVersionException, PlanVersionAlreadyExistsException;

    /**
     * Use this endpoint to list all the versions of a Plan.
     * @return A list of Plans.
     *
     * @throws PlanNotFoundException when trying to get, update, or delete a plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/plans/{planId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Plan Versions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Plan versions is successfully returned.", useReturnTypeSchema = true)
    })
    List<PlanVersionSummaryBean> listPlanVersions(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId
    ) throws PlanNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * a Plan.
     * @return A Plan version.
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Plan Version")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Plan version is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Plan version does not exist.")
    })
    PlanVersionBean getPlanVersion(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version") @Parameter(description = "The Plan version.") String version
    ) throws PlanVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * Plan.
     * @return A list of audit entries.
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Plan Version Activity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the audit activity entries are successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Plan version does not exist.")
    })
    SearchResultsBean<AuditEntryBean> getPlanVersionActivity(
            @PathParam("organizationId")  @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version") @Parameter(description = "The Plan version.") String version,
            @QueryParam("page")  @Parameter(description = "Which page of activity data to return.") int page,
            @QueryParam("count") @Parameter(description = "The number of entries per page to return.") int pageSize
    ) throws PlanVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to add a new Policy to the Plan version.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add Plan Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Policy is successfully added.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
    })
    PolicyBean createPlanPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version")  @Parameter(description = "The Plan version.") String version,
            NewPolicyBean bean
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all the Policies configured for the Plan.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List All Plan Policies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of Policies is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
    })
    List<PolicySummaryBean> listPlanPolicies(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version")  @Parameter(description = "The Plan version.") String version
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the Plan version.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Plan Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the Policy is successfully returned.", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
    })
    PolicyBean getPlanPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version") @Parameter(description = "The Plan version.") String version,
            @PathParam("policyId")  @Parameter(description = "The Policy ID.") long policyId
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single Plan Policy.

     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update Plan Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Policy was successfully updated."),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
    })
    void updatePlanPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version") @Parameter(description = "The Plan version.") String version,
            @PathParam("policyId") @Parameter(description = "The Policy ID.") long policyId,
            UpdatePolicyBean bean
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the Plan.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    @Operation(summary = "Remove Plan Policy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Policy was successfully deleted."),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
            @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
    })
    void deletePlanPolicy(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId")  @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version") @Parameter(description = "The Plan version.") String version,
            @PathParam("policyId")  @Parameter(description = "The Policy ID.") long policyId
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete a plan. Only an unlocked plan may be deleted.
     *
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidPlanStatusException when the user attempts some action on the Plan when it is not in an appropriate state/status
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/plans/{planId}")
    @Operation(summary = "Delete Plan")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the Plan was successfully deleted"),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
            @ApiResponse(responseCode = "409", description = "If the Plan cannot be deleted.")
    })
    void deletePlan(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId") @Parameter(description = "The Plan ID.") String planId
    ) throws PlanNotFoundException, NotAuthorizedException, InvalidPlanStatusException;

    /**
     * Use this endpoint to change the order of Policies for a Plan.  When a
     * Policy is added to the Plan, it is added as the last Policy in the list
     * of Plan Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all the Policies for the Plan, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/plans/{planId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Re-Order Plan Policies")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the re-ordering of Policies was successful."),
            @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
    })
    void reorderPlanPolicies(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            @PathParam("planId")  @Parameter(description = "The Plan ID.") String planId,
            @PathParam("version") @Parameter(description = "The Plan version.") String version,
            PolicyChainBean policyChain
    ) throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException;

    /*
     * MEMBERS
     */

    /**
     * Grant membership in a role to a user.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @POST
    @Path("{organizationId}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Grant Membership(s)")
    @ApiResponse(responseCode = "204", description = "If the membership(s) were successfully granted.")
    void grant(
            @PathParam("organizationId") @Parameter(description = "The Organization ID.") String organizationId,
            GrantRolesBean bean
    ) throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException;

    /**
     * Revoke membership in a role.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/roles/{roleId}/{userId}")
    @Operation(summary = "Revoke Single Membership")
    @ApiResponse(responseCode = "204", description = "If the membership was successfully revoked.")
    void revoke(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("roleId") @Parameter(description = "The role ID.") String roleId,
            @PathParam("userId") @Parameter(description = "The user ID.")  String userId
    ) throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException;

    /**
     * Revoke all of a user's role memberships from the org.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @DELETE
    @Path("{organizationId}/members/{userId}")
    @Operation(summary = "Revoke All Memberships")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "If the user's memberships were successfully revoked."),
            @ApiResponse(responseCode = "404", description = "If the user does not exist.")
    })
    void revokeAll(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("userId") @Parameter(description = "The user ID.") String userId
    ) throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException;

    /**
     * Lists all members of the organization.
     * @return List of members.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/members")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "List Organization Members")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the list of members is returned successfully.", useReturnTypeSchema = true)
    })
    List<MemberBean> listMembers(@PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;


    /* -----------------------------------------------------------------
     *                             Metrics
     * ----------------------------------------------------------------- */

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return a full histogram of request count data based on the provided date range
     * and interval.  Valid intervals are:  month, week, day, hour, minute
     *
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/usage")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Usage Metrics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    UsageHistogramBean getUsage(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version")  @Parameter(description = "The API version.") String version,
            @QueryParam("interval") @Parameter(description = "A valid interval (month, week, day, hour, minute)") HistogramIntervalType interval,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return request count data broken down by client.  It basically answers
     * the question "who is calling my API?".
     *
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/clientUsage")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Usage Metrics (per Client)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    UsagePerClientBean getUsagePerClient(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;


    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return request count data broken down by plan.  It basically answers
     * the question "which API plans are most used?".
     *
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/planUsage")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Usage Metrics (per Plan)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    UsagePerPlanBean getUsagePerPlan(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.")  String version,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return a full histogram of response statistics data based on the provided date range
     * and interval.  Valid intervals are:  month, week, day, hour, minute
     * <p>
     * The data returned includes total request counts, failure counts, and error counts
     * for each data point in the histogram.
     *
     * @return Response statistics metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/responseStats")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Response Statistics (Histogram)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    ResponseStatsHistogramBean getResponseStats(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("apiId")  @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @QueryParam("interval") @Parameter(description = "A valid interval (month, week, day, hour, minute)") HistogramIntervalType interval,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return total response type statistics over the given date range.  Basically
     * this will return three numbers: total request, # failed responses, # error
     * responses.
     *
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/summaryResponseStats")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Response Statistics (Summary)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    ResponseStatsSummaryBean getResponseStatsSummary(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return response type statistics broken down by client.
     *
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/clientResponseStats")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Response Statistics (per Client)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    ResponseStatsPerClientBean getResponseStatsPerClient(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return response type statistics broken down by plan.
     *
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidMetricCriteriaException when the metric criteria is not valid.
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/planResponseStats")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get API Response Statistics (per Plan)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
    })
    ResponseStatsPerPlanBean getResponseStatsPerPlan(
            @PathParam("organizationId") @Parameter(description = "The organization ID.") String organizationId,
            @PathParam("apiId") @Parameter(description = "The API ID.") String apiId,
            @PathParam("version") @Parameter(description = "The API version.") String version,
            @QueryParam("from") @Parameter(description = "The start of a valid date range.") String fromDate,
            @QueryParam("to") @Parameter(description = "The end of a valid date range.") String toDate
    ) throws NotAuthorizedException, InvalidMetricCriteriaException;

}
