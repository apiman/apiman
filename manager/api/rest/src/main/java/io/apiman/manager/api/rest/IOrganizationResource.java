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
            requestBody = @RequestBody(description = "Information about the new Organization", required = true),
            summary = "Create Organization",
            responses = @ApiResponse(responseCode = "200", description = "If the Organization was successfully created.", useReturnTypeSchema = true)
    )
    public OrganizationBean createOrg(NewOrganizationBean bean) throws OrganizationAlreadyExistsException,
            NotAuthorizedException, InvalidNameException;

    /**
     * Delete an org
     * @throws OrganizationNotFoundException when the specified organization does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws EntityStillActiveException when user attempts to delete an organization which still has active sub-elements
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}")
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID to delete")
            },
            summary = "Delete an organization",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Organization was successfully deleted"),
                    @ApiResponse(responseCode = "409", description = "If the delete preconditions have not been met (i.e. sub-elements are still active, such as still-published APIs).")
            })
    public void deleteOrg(@PathParam("organizationId") String organizationId) throws OrganizationNotFoundException,
            NotAuthorizedException, EntityStillActiveException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization id.")
            },
            summary = "Get Organization By ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Organization was successfully returned", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist")
            })
    public OrganizationBean getOrg(@PathParam("organizationId") String organizationId) throws OrganizationNotFoundException;

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
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
            },
            summary = "Update Organization By ID",
            requestBody = @RequestBody(description = "Updated Organization information."),
            responses = {
            @ApiResponse(responseCode = "200", description = "If the Organization meta-data is successfully updated."),
            @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
    })
    public void updateOrg(@PathParam("organizationId") String organizationId, UpdateOrganizationBean bean)
            throws OrganizationNotFoundException, NotAuthorizedException;

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
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "page", description = "Which page of activity results to return."),
                    @Parameter(name = "pageSize", description = "The number of entries per page.")
            },
            summary = "Get Organization Activity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
            })
    public SearchResultsBean<AuditEntryBean> getOrgActivity(
            @PathParam("organizationId") String organizationId, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws OrganizationNotFoundException, NotAuthorizedException;

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
     * @throws InvalidNameException when the user attempts the create with an invalid name
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            requestBody = @RequestBody(description = "Information about the new Client."),
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID.")
            },
            summary = "Create Client",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Client is successfully created."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
            })
    public ClientBean createClient(@PathParam("organizationId") String organizationId,
                                   NewClientBean bean) throws OrganizationNotFoundException, ClientAlreadyExistsException,
            NotAuthorizedException, InvalidNameException;

    /**
     * Delete a ClientApp
     * @throws OrganizationNotFoundException when the specified organization does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws EntityStillActiveException when user attempts to delete a Client which still has active sub-elements
     */
    @PermitAll
    @DELETE
    @Path("{organizationId}/clients/{clientId}")
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID the client exists within"),
                    @Parameter(name = "clientId", description = "The ClientApp ID to delete")
            },
            summary = "Delete a client",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Organization was successfully deleted"),
                    @ApiResponse(responseCode = "409", description = "If the delete preconditions have not been met (i.e. sub-elements are still active, such as still-registered ClientVersions).")
            })
    public void deleteClient(@PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId)
            throws OrganizationNotFoundException, NotAuthorizedException, EntityStillActiveException;

    /**
     * Use this endpoint to retrieve information about a single Client by ID.  Note
     * that this only returns information about the Client, not about any particular
     * *version* of the Client.
     * @return An Client.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist when trying to get, update, or delete a client that does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @GET
    @Path("{organizationId}/clients/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            parameters = {

                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID.")
            },
            summary = "Get Client By ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Client is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public ClientBean getClient(@PathParam("organizationId") String organizationId,
                                @PathParam("clientId") String clientId) throws ClientNotFoundException,
            NotAuthorizedException;

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
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "page", description = "Which page of activity should be returned."),
                    @Parameter(name = "pageSize", description = "The number of entries per page to return.")

            },
            summary = "Get Client Activity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public SearchResultsBean<AuditEntryBean> getClientActivity(
            @PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws ClientNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID.")
            },
            summary = "List Clients",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Clients is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
            })
    public List<ClientSummaryBean> listClients(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "bean", description = "Updated Client information.")

            },
            summary = "Update Client",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Client is updated successfully."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public void updateClient(@PathParam("organizationId") String organizationId,
                             @PathParam("clientId") String clientId, UpdateClientBean bean)
            throws ClientNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "bean", description = "Initial information about the new Client version.")

            },
            summary = "Create Client Version",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Client version is created successfully."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
                    @ApiResponse(responseCode = "409", description = "If the Client version already exists.")
            })
    public ClientVersionBean createClientVersion(@PathParam("organizationId") String organizationId,
                                                 @PathParam("clientId") String clientId, NewClientVersionBean bean)
            throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException,
            ClientVersionAlreadyExistsException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID.")

            },
            summary = "List Client Versions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Client versions is successfully returned.")
            })
    public List<ClientVersionSummaryBean> listClientVersions(@PathParam("organizationId") String organizationId,
                                                             @PathParam("clientId") String clientId) throws ClientNotFoundException, NotAuthorizedException;


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
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client Version."),
                    @Parameter(name = "bean", description = "The new custom API Key (or empty to auto-generate a new one).")

            },
            summary = "Update API Key",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Client's API Key is successfully updated."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
                    @ApiResponse(responseCode = "409", description = "If the Client has the wrong status.")
            })
    public ApiKeyBean updateClientApiKey(@PathParam("organizationId") String organizationId,
                                         @PathParam("clientId") String clientId, @PathParam("version") String version, ApiKeyBean bean)
            throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException,
            InvalidClientStatusException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client Version.")
            },
            summary = "Get API Key",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Client's API Key is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public ApiKeyBean getClientApiKey(@PathParam("organizationId") String organizationId,
                                      @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version.")

            },
            summary = "Get Client Version",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Client version is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client version does not exist.")
            })
    public ClientVersionBean getClientVersion(@PathParam("organizationId") String organizationId,
                                              @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws ClientVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {

                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "page", description = "Which page of activity data to return."),
                    @Parameter(name = "pageSize", description = "The number of entries per page to return.")
            },
            summary = "Get Client Version Activity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the audit activity entries are successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client version does not exist.")
            })
    public SearchResultsBean<AuditEntryBean> getClientVersionActivity(
            @PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ClientVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "clientId", description = "The client ID."),
                    @Parameter(name = "version", description = "The client version."),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")

            },
            summary = "Get Client Usage Metrics (per API)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.")
            })
    public ClientUsagePerApiBean getClientUsagePerApi(
            @PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;


    /**
     * Use this endpoint to create a Contract between the Client and an API.  In order
     * to create a Contract, the caller must specify the Organization, ID, and Version of the
     * API.  Additionally, the caller must specify the ID of the Plan it wished to use for
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
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version.")
            },
            summary = "Create an API Contract",
            requestBody = @RequestBody(description = "Required information about the new Contract."),
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Contract is successfully created."),
                    @ApiResponse(responseCode = "404", description = "If the Client version does not exist.")
            }
    )
    public ContractBean createContract(@PathParam("organizationId") String organizationId,
                                       @PathParam("clientId") String clientId, @PathParam("version") String version,
                                       NewContractBean bean) throws OrganizationNotFoundException, ClientNotFoundException,
            ApiNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException;

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
            parameters = {

                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "contractId", description = "The ID of the Contract.")
            },
            summary = "Get API Contract",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Contract is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client version does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Contract is not found.")
            })
    public ContractBean getContract(@PathParam("organizationId") String organizationId,
                                    @PathParam("clientId") String clientId, @PathParam("version") String version,
                                    @PathParam("contractId") Long contractId)
            throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to probe a specific API contract's policy state
     * @return A policy probe response
     */
    @PermitAll
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts/{contractId}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version.")
            },
            summary = "Probe a policy associated with a contract",
            requestBody = @RequestBody(description = "The probe payload (refer to the documentation of the probe you want to use for the correct format)."),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Probe executed successfully"),
            })
    public Response probeContractPolicy(@PathParam("organizationId") String organizationId,
                                        @PathParam("clientId") String clientId, @PathParam("version") String version,
                                        @PathParam("contractId") Long contractId, @PathParam("policyId") long policyId,
                                        String rawPayload)
            throws ClientNotFoundException, ContractNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version.")
            },
            summary = "List All Contracts for a Client",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Contracts is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client is not found.")
            })
    public List<ContractSummaryBean> getClientVersionContracts(@PathParam("organizationId") String organizationId,
                                                               @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws ClientNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "download", description = "Query parameter set to true in order to generate a download link.")
            },
            summary = "Get API Registry (JSON)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API Registry information is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public Response getApiRegistryJSON(@PathParam("organizationId") String organizationId,
                                       @PathParam("clientId") String clientId, @PathParam("version") String version,
                                       @QueryParam("download") String download)
            throws ClientNotFoundException, NotAuthorizedException;
    public Response getApiRegistryJSONInternal(String organizationId, String clientId, String version) throws ClientVersionNotFoundException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "download", description = "Query parameter set to true in order to generate a download link.")
            },
            summary = "Get API Registry (XML)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API Registry information is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public Response getApiRegistryXML(@PathParam("organizationId") String organizationId,
                                      @PathParam("clientId") String clientId, @PathParam("version") String version,
                                      @QueryParam("download") String download)
            throws ClientNotFoundException, NotAuthorizedException;
    public Response getApiRegistryXMLInternal(String organizationId, String clientId, String version) throws ClientVersionNotFoundException;

    /**
     * Use this endpoint to break all contracts between this client and its APIs.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PermitAll
    @DELETE
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts")
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version.")
            },
            summary = "Break All Contracts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the operation is successful."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public void deleteAllContracts(@PathParam("organizationId") String organizationId,
                                   @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws ClientNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "contractId", description = "The Contract ID.")
            },
            summary = "Break Contract",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Contract is successfully broken."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Contract does not exist.")
            })
    public void deleteContract(@PathParam("organizationId") String organizationId,
                               @PathParam("clientId") String clientId, @PathParam("version") String version,
                               @PathParam("contractId") Long contractId) throws ClientNotFoundException,
            ContractNotFoundException, NotAuthorizedException, InvalidClientStatusException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "bean", description = "Information about the new Policy.")
            },
            summary = "Add Client Policy",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Policy is successfully added."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public PolicyBean createClientPolicy(@PathParam("organizationId") String organizationId,
                                         @PathParam("clientId") String clientId, @PathParam("version") String version,
                                         NewPolicyBean bean) throws OrganizationNotFoundException, ClientVersionNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")

            },
            summary = "Get Client Policy",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Policy is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public PolicyBean getClientPolicy(@PathParam("organizationId") String organizationId,
                                      @PathParam("clientId") String clientId, @PathParam("version") String version,
                                      @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ClientVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "policyId", description = "The Policy ID."),
                    @Parameter(name = "bean", description = "New meta-data and/or configuration for the Policy.")

            },
            summary = "Update Client Policy",

            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Policy was successfully updated."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
            })
    public void updateClientPolicy(@PathParam("organizationId") String organizationId,
                                   @PathParam("clientId") String clientId, @PathParam("version") String version,
                                   @PathParam("policyId") long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")
            },
            summary = "Remove Client Policy",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Policy was successfully deleted."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
            })
    public void deleteClientPolicy(@PathParam("organizationId") String organizationId,
                                   @PathParam("clientId") String clientId, @PathParam("version") String version,
                                   @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ClientVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version.")

            },
            summary = "List All Client Policies",

            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Policies is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public List<PolicySummaryBean> listClientPolicies(@PathParam("organizationId") String organizationId,
                                                      @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ClientVersionNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "clientId", description = "The Client ID."),
                    @Parameter(name = "version", description = "The Client version."),
                    @Parameter(name = "policyChain", description = "The Policies in the desired order.")

            },
            summary = "Re-Order Client Policies",

            responses = {
                    @ApiResponse(responseCode = "204", description = "If the re-ordering of Policies was successful."),
                    @ApiResponse(responseCode = "404", description = "If the Client does not exist.")
            })
    public void reorderClientPolicies(@PathParam("organizationId") String organizationId,
                                      @PathParam("clientId") String clientId, @PathParam("version") String version,
                                      PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ClientVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "bean", description = "Information about the new API.")

            },
            summary = "Create API",

            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API is successfully created."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
            })
    public ApiBeanDto createApi(@PathParam("organizationId") String organizationId, @NotNull NewApiBean bean)
            throws OrganizationNotFoundException, ApiAlreadyExistsException, NotAuthorizedException,
            InvalidNameException;
    /**
     * Use this endpoint to get a list of all APIs in the Organization.
     * @return A list of APIs.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     */
    @PermitAll
    @GET
    @Path("{organizationId}/apis")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID.")
            },
            summary = "List APIs",

            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of APIs is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
            })
    public List<ApiSummaryBean> listApis(@PathParam("organizationId") String organizationId)
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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID.")

            },
            summary = "Get API By ID",

            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public ApiBeanDto getApi(@PathParam("organizationId") String organizationId,
                             @PathParam("apiId") String apiId) throws ApiNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "bean", description = "Updated API information.")
            },
            summary = "Update API",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the API is updated successfully."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public void updateApi(@PathParam("organizationId") String organizationId,
                          @PathParam("apiId") String apiId, UpdateApiBean bean)
            throws ApiNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete an API Image
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @RolesAllowed("apiuser")
    @DELETE
    @Path("{organizationId}/apis/{apiId}/image")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID.")
            },
            summary = "Delete API Image",

            responses = {
                    @ApiResponse(responseCode = "204", description = "If the API is updated successfully."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public void deleteApiImage(@PathParam("organizationId") String organizationId,
                               @PathParam("apiId") String apiId)
            throws ApiNotFoundException, NotAuthorizedException;

    @RolesAllowed("apiuser")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{organizationId}/apis/{apiId}/tags")
    @Operation(
            parameters = {
                @Parameter(name = "organizationId", description = "Organization ID"),
                @Parameter(name = "apiId", description = "API ID")
            },
            summary = "Tag an API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tag was created successfully.")
            })
    void tagApi(@PathParam("organizationId") String organizationId,
                @PathParam("apiId") String apiId,
                KeyValueTagDto bean)
            throws ApiNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID.")
            },
            summary = "Delete API",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the API is updated successfully."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist."),
                    @ApiResponse(responseCode = "409", description = "If the API cannot be deleted.")
            })
    public void deleteApi(@PathParam("organizationId") String organizationId,
                          @PathParam("apiId") String apiId)
            throws ApiNotFoundException, NotAuthorizedException, InvalidApiStatusException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "page", description = "Which page of activity should be returned."),
                    @Parameter(name = "pageSize", description = "The number of entries per page to return.")
            },
            summary = "Get API Activity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public SearchResultsBean<AuditEntryBean> getApiActivity(
            @PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId,
            @QueryParam("page") int page,
            @QueryParam("count") int pageSize
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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "bean", description = "Initial information about the new API version.")
            },
            summary = "Create API Version",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API version is created successfully.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist."),
                    @ApiResponse(responseCode = "409", description = "If the API version already exists.")
            })
    public ApiVersionBeanDto createApiVersion(@PathParam("organizationId") String organizationId,
                                              @PathParam("apiId") String apiId,
                                              NewApiVersionBean bean)
            throws ApiNotFoundException, NotAuthorizedException, InvalidVersionException,
            ApiVersionAlreadyExistsException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID.")
            },
            summary = "List API Versions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of API versions is successfully returned.", useReturnTypeSchema = true)
            })
    public List<ApiVersionSummaryBean> listApiVersions(@PathParam("organizationId") String organizationId,
                                                       @PathParam("apiId") String apiId) throws ApiNotFoundException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "Get API Version",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API version is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
            })
    public ApiVersionBeanDto getApiVersion(@PathParam("organizationId") String organizationId,
                                           @PathParam("apiId") String apiId,
                                           @PathParam("version") String version)
            throws ApiVersionNotFoundException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "Get API Version Status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the status information is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
            })
    public ApiVersionStatusBean getApiVersionStatus(@PathParam("organizationId") String organizationId,
                                                    @PathParam("apiId") String apiId,
                                                    @PathParam("version") String version)
            throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve the API's definition document.  An API
     * definition document can be several different types, depending on the API
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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "Get API Definition",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API definition is successfully returned."),
                    @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
            })
    public Response getApiDefinition(@PathParam("organizationId") String organizationId,
                                     @PathParam("apiId") String apiId,
                                     @PathParam("version") String version)
            throws ApiVersionNotFoundException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "Get API Endpoint",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the endpoint information is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    @Produces(MediaType.APPLICATION_JSON)
    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(@PathParam("organizationId") String organizationId,
                                                                   @PathParam("apiId") String apiId,
                                                                   @PathParam("version") String version)
            throws ApiVersionNotFoundException, InvalidApiStatusException, GatewayNotFoundException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
            },
            summary = "Update API Version",
            requestBody = @RequestBody(description = "Updated information about the API version."),
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the API version information was successfully updated.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public ApiVersionBeanDto updateApiVersion(@PathParam("organizationId") String organizationId,
                                              @PathParam("apiId") String apiId, @PathParam("version") String version,
                                              @Valid UpdateApiVersionBean bean) throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "Update API Definition",

            responses = {
                    @ApiResponse(responseCode = "204", description = "If the API definition was successfully updated."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public void updateApiDefinition(@PathParam("organizationId") String organizationId,
                                    @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;


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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
            },
            summary = "Update API Definition from URL",
            requestBody = @RequestBody(description = "The API definition reference information."),
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the API definition was successfully updated."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public void updateApiDefinitionFromURL(@PathParam("organizationId") String organizationId,
                                           @PathParam("apiId") String apiId, @PathParam("version") String version, NewApiDefinitionBean bean)
            throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "page", description = "Which page of activity data to return."),
                    @Parameter(name = "pageSize", description = "The number of entries per page to return.")
            },
            summary = "Get API Version Activity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the audit activity entries are successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API version does not exist.")
            })
    public SearchResultsBean<AuditEntryBean> getApiVersionActivity(@PathParam("organizationId") String organizationId,
                                                                   @PathParam("apiId") String apiId,
                                                                   @PathParam("version") String version,
                                                                   @QueryParam("page") int page,
                                                                   @QueryParam("count") int pageSize
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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "List API Plans",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the API plans are successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API cannot be found.")
            })
    public List<ApiPlanSummaryBean> getApiVersionPlans(@PathParam("organizationId") String organizationId,
                                                       @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException;

    @RolesAllowed("apiuser")
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}/reorderApiPlans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reordering of plans was successful.")
            })
    public void reorderApiPlans(@PathParam("organizationId") String organizationId,
                                @PathParam("apiId") String apiId,
                                @PathParam("version") String version,
                                ApiPlanOrderDto apiPlanOrder)
            throws ApiVersionNotFoundException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "Add API Policy",
            requestBody = @RequestBody(description = "Information about the new Policy."),
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Policy is successfully added.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public PolicyBean createApiPolicy(@PathParam("organizationId") String organizationId,
                                      @PathParam("apiId") String apiId,
                                      @PathParam("version") String version,
                                      NewPolicyBean bean) throws OrganizationNotFoundException, ApiVersionNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")
            },
            summary = "Get API Policy",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Policy is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public PolicyBean getApiPolicy(@PathParam("organizationId") String organizationId,
                                   @PathParam("apiId") String apiId,
                                   @PathParam("version") String version,
                                   @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApiVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")
            },
            summary = "Update API Policy",
            requestBody = @RequestBody(description = "New meta-data and/or configuration for the Policy."),
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Policy was successfully updated."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
            })
    public void updateApiPolicy(@PathParam("organizationId") String organizationId,
                                @PathParam("apiId") String apiId,
                                @PathParam("version") String version,
                                @PathParam("policyId") long policyId,
                                UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")
            },
            summary = "Remove API Policy",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Policy was successfully deleted."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
            })
    public void deleteApiPolicy(@PathParam("organizationId") String organizationId,
                                @PathParam("apiId") String apiId,
                                @PathParam("version") String version,
                                @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApiVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "Remove API Definition",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the API definition was successfully deleted."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public void deleteApiDefinition(@PathParam("organizationId") String organizationId,
                                    @PathParam("apiId") String apiId,
                                    @PathParam("version") String version)
            throws OrganizationNotFoundException, ApiVersionNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version.")
            },
            summary = "List All API Policies",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Policies is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public List<PolicySummaryBean> listApiPolicies(@PathParam("organizationId") String organizationId,
                                                   @PathParam("apiId") String apiId,
                                                   @PathParam("version") String version)
            throws OrganizationNotFoundException, ApiVersionNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "policyChain", description = "The Policies in the desired order.")
            },
            summary = "Re-Order API Policies",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the re-ordering of Policies was successful."),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public void reorderApiPolicies(@PathParam("organizationId") String organizationId,
                                   @PathParam("apiId") String apiId,
                                   @PathParam("version") String version,
                                   PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ApiVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "planId", description = "The Plan ID.")
            },
            summary = "Get API Policy Chain",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Policy Chain is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public PolicyChainBean getApiPolicyChain(@PathParam("organizationId") String organizationId,
                                             @PathParam("apiId") String apiId,
                                             @PathParam("version") String version,
                                             @PathParam("planId") String planId) throws ApiVersionNotFoundException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "page", description = "Which page of Contracts to return."),
                    @Parameter(name = "pageSize", description = "The number of Contracts per page to return.")
            },
            summary = "List API Contracts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Contracts is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the API does not exist.")
            })
    public List<ContractSummaryBean> getApiVersionContracts(
            @PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId,
            @PathParam("version") String version,
            @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ApiVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            summary = "Create Plan",
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID.")
            },
            requestBody = @RequestBody(description = "Information about the new Plan."),
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Plan is successfully created.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
            })
    public PlanBean createPlan(@PathParam("organizationId") String organizationId, NewPlanBean bean)
            throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException,
            InvalidNameException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID.")

            },
            summary = "Get Plan By ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Plan is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
            })
    public PlanBean getPlan(@PathParam("organizationId") String organizationId,
                            @PathParam("planId") String planId) throws PlanNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "page", description = "Which page of activity should be returned."),
                    @Parameter(name = "pageSize", description = "The number of entries per page to return.")
            },
            summary = "Get Plan Activity",

            responses = {
                    @ApiResponse(responseCode = "200", description = "If the audit information is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
            })
    public SearchResultsBean<AuditEntryBean> getPlanActivity(@PathParam("organizationId") String organizationId,
                                                             @PathParam("planId") String planId, @QueryParam("page") int page,
                                                             @QueryParam("count") int pageSize) throws PlanNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID.")
            },
            summary = "List Plans",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Plans is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Organization does not exist.")
            })
    public List<PlanSummaryBean> listPlans(@PathParam("organizationId") String organizationId)
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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID.")
            },
            requestBody = @RequestBody(description = "Updated Plan information."),
            summary = "Update Plan",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Plan is updated successfully."),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
            })
    public void updatePlan(@PathParam("organizationId") String organizationId,
                           @PathParam("planId") String planId, UpdatePlanBean bean)
            throws PlanNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID.")
            },
            summary = "Create Plan Version",
            requestBody = @RequestBody(description = "Initial information about the new Plan version."),
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Plan version is created successfully.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
                    @ApiResponse(responseCode = "409", description = "If the Plan version already exists.")
            })
    public PlanVersionBean createPlanVersion(@PathParam("organizationId") String organizationId,
                                             @PathParam("planId") String planId, NewPlanVersionBean bean) throws PlanNotFoundException,
            NotAuthorizedException, InvalidVersionException, PlanVersionAlreadyExistsException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID.")
            },
            summary = "List Plan Versions",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Plan versions is successfully returned.", useReturnTypeSchema = true)
            })
    public List<PlanVersionSummaryBean> listPlanVersions(@PathParam("organizationId") String organizationId,
                                                         @PathParam("planId") String planId) throws PlanNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version.")
            },
            summary = "Get Plan Version",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Plan version is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Plan version does not exist.")
            })
    public PlanVersionBean getPlanVersion(@PathParam("organizationId") String organizationId,
                                          @PathParam("planId") String planId, @PathParam("version") String version)
            throws PlanVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version."),
                    @Parameter(name = "page", description = "Which page of activity data to return."),
                    @Parameter(name = "pageSize", description = "The number of entries per page to return.")
            },
            summary = "Get Plan Version Activity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the audit activity entries are successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Plan version does not exist.")
            })
    public SearchResultsBean<AuditEntryBean> getPlanVersionActivity(@PathParam("organizationId") String organizationId,
                                                                    @PathParam("planId") String planId, @PathParam("version") String version,
                                                                    @QueryParam("page") int page, @QueryParam("count") int pageSize)
            throws PlanVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version.")
            },
            summary = "Add Plan Policy",
            requestBody = @RequestBody(description = "Information about the new Policy."),
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Policy is successfully added.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
            })
    public PolicyBean createPlanPolicy(@PathParam("organizationId") String organizationId,
                                       @PathParam("planId") String planId,
                                       @PathParam("version") String version,
                                       NewPolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version.")
            },
            summary = "List All Plan Policies",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of Policies is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
            })
    public List<PolicySummaryBean> listPlanPolicies(@PathParam("organizationId") String organizationId,
                                                    @PathParam("planId") String planId,
                                                    @PathParam("version") String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")
            },
            summary = "Get Plan Policy",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the Policy is successfully returned.", useReturnTypeSchema = true),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
            })
    public PolicyBean getPlanPolicy(@PathParam("organizationId") String organizationId,
                                    @PathParam("planId") String planId, @PathParam("version") String version,
                                    @PathParam("policyId") long policyId) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")
            },
            summary = "Update Plan Policy",
            requestBody = @RequestBody(description = "New meta-data and/or configuration for the Policy."),
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Policy was successfully updated."),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
            })
    public void updatePlanPolicy(@PathParam("organizationId") String organizationId,
                                 @PathParam("planId") String planId, @PathParam("version") String version,
                                 @PathParam("policyId") long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version."),
                    @Parameter(name = "policyId", description = "The Policy ID.")
            },
            summary = "Remove Plan Policy",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Policy was successfully deleted."),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
                    @ApiResponse(responseCode = "404", description = "If the Policy does not exist.")
            })
    public void deletePlanPolicy(@PathParam("organizationId") String organizationId,
                                 @PathParam("planId") String planId, @PathParam("version") String version,
                                 @PathParam("policyId") long policyId) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID.")
            },
            summary = "Delete Plan",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the Plan was successfully deleted"),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist."),
                    @ApiResponse(responseCode = "409", description = "If the Plan cannot be deleted.")
            })
    public void deletePlan(@PathParam("organizationId") String organizationId,
                           @PathParam("planId") String planId)
            throws PlanNotFoundException, NotAuthorizedException, InvalidPlanStatusException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
                    @Parameter(name = "planId", description = "The Plan ID."),
                    @Parameter(name = "version", description = "The Plan version."),
                    @Parameter(name = "policyChain", description = "The Policies in the desired order.")
            },
            summary = "Re-Order Plan Policies",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the re-ordering of Policies was successful."),
                    @ApiResponse(responseCode = "404", description = "If the Plan does not exist.")
            })
    public void reorderPlanPolicies(@PathParam("organizationId") String organizationId,
                                    @PathParam("planId") String planId,
                                    @PathParam("version") String version,
                                    PolicyChainBean policyChain) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The Organization ID."),
            },
            summary = "Grant Membership(s)",
            requestBody = @RequestBody(description = "Roles to grant, and the ID of the user."),
            responses = @ApiResponse(responseCode = "204", description = "If the membership(s) were successfully granted.")
    )
    public void grant(@PathParam("organizationId") String organizationId, GrantRolesBean bean)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "roleId", description = "The role ID."),
                    @Parameter(name = "userId", description = "The user ID.")
            },
            summary = "Revoke Single Membership",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the membership was successfully revoked.")
            })
    public void revoke(@PathParam("organizationId") String organizationId,
                       @PathParam("roleId") String roleId,
                       @PathParam("userId") String userId)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
            NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "userId", description = "The user ID.")
            },
            summary = "Revoke All Memberships",
            responses = {
                    @ApiResponse(responseCode = "204", description = "If the user's memberships were successfully revoked."),
                    @ApiResponse(responseCode = "404", description = "If the user does not exist.")
            })
    public void revokeAll(@PathParam("organizationId") String organizationId,
                          @PathParam("userId") String userId) throws OrganizationNotFoundException, RoleNotFoundException,
            UserNotFoundException, NotAuthorizedException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID.")
            },
            summary = "List Organization Members",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the list of members is returned successfully.", useReturnTypeSchema = true)
            })
    public List<MemberBean> listMembers(@PathParam("organizationId") String organizationId)
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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "interval", description = "A valid interval (month, week, day, hour, minute)"),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")
            },
            summary = "Get API Usage Metrics",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
            })
    public UsageHistogramBean getUsage(@PathParam("organizationId") String organizationId,
                                       @PathParam("apiId") String apiId,
                                       @PathParam("version") String version,
                                       @QueryParam("interval") HistogramIntervalType interval,
                                       @QueryParam("from") String fromDate,
                                       @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")
            },
            summary = "Get API Usage Metrics (per Client)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
            })
    public UsagePerClientBean getUsagePerClient(
            @PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId,
            @PathParam("version") String version,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;


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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")
            },
            summary = "Get API Usage Metrics (per Plan)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
            })
    public UsagePerPlanBean getUsagePerPlan(
            @PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId,
            @PathParam("version") String version,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "interval", description = "A valid interval (month, week, day, hour, minute)"),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")
            },
            summary = "Get API Response Statistics (Histogram)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
            })
    public ResponseStatsHistogramBean getResponseStats(@PathParam("organizationId") String organizationId,
                                                       @PathParam("apiId") String apiId,
                                                       @PathParam("version") String version,
                                                       @QueryParam("interval") HistogramIntervalType interval,
                                                       @QueryParam("from") String fromDate,
                                                       @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")
            },
            summary = "Get API Response Statistics (Summary)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
            })
    public ResponseStatsSummaryBean getResponseStatsSummary(
            @PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId,
            @PathParam("version") String version,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")
            },
            summary = "Get API Response Statistics (per Client)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
            })
    public ResponseStatsPerClientBean getResponseStatsPerClient(
            @PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId,
            @PathParam("version") String version,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

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
    @Operation(
            parameters = {
                    @Parameter(name = "organizationId", description = "The organization ID."),
                    @Parameter(name = "apiId", description = "The API ID."),
                    @Parameter(name = "version", description = "The API version."),
                    @Parameter(name = "fromDate", description = "The start of a valid date range."),
                    @Parameter(name = "toDate", description = "The end of a valid date range.")
            },
            summary = "Get API Response Statistics (per Plan)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "If the metrics data is successfully returned.", useReturnTypeSchema = true)
            })
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(
            @PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId,
            @PathParam("version") String version,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

}
