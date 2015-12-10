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

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apis.ApiVersionStatusBean;
import io.apiman.manager.api.beans.apis.NewApiBean;
import io.apiman.manager.api.beans.apis.NewApiDefinitionBean;
import io.apiman.manager.api.beans.apis.NewApiVersionBean;
import io.apiman.manager.api.beans.apis.UpdateApiBean;
import io.apiman.manager.api.beans.apis.UpdateApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
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
import io.apiman.manager.api.rest.contract.exceptions.ApiAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApiNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ApiVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ClientAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ClientNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ClientVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ClientVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidClientStatusException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidMetricCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidNameException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidVersionException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.UserNotFoundException;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Organization API.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("organizations")
@Api
public interface IOrganizationResource {

    /**
     * Use this endpoint to create a new Organization.
     * @summary Create Organization
     * @param bean Information about the new Organization.
     * @statuscode 200 If the Organization was successfully created.
     * @return Full details about the Organization that was created.
     * @throws OrganizationAlreadyExistsException when trying to create an Organization that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts to create an Organization with an invalid name
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationBean create(NewOrganizationBean bean) throws OrganizationAlreadyExistsException,
            NotAuthorizedException, InvalidNameException;

    /**
     * Use this endpoint to get information about a single Organization
     * by its ID.
     * @summary Get Organization By ID
     * @param organizationId The Organization id.
     * @statuscode 200 If the Organization was successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @return The Organization.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationBean get(@PathParam("organizationId") String organizationId) throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Updates meta-information about a single Organization.
     * @summary Update Organization By ID
     * @param organizationId The Organization ID.
     * @param bean Updated Organization information.
     * @statuscode 200 If the Organization meta-data is successfully updated.
     * @statuscode 404 If the Organization does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("organizationId") String organizationId, UpdateOrganizationBean bean)
            throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Returns audit activity information for a single Organization.  The audit
     * information that is returned represents all of the activity associated
     * with this Organization (i.e. an audit log for everything in the Organization).
     * @summary Get Organization Activity
     * @param organizationId The Organization ID.
     * @param page Which page of activity results to return.
     * @param pageSize The number of entries per page.
     * @statuscode 200 If the audit information is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @return List of audit/activity entries.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> activity(
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
     * @summary Create Client
     * @param organizationId The Organization ID.
     * @param bean Information about the new Client.
     * @statuscode 200 If the Client is successfully created.
     * @statuscode 404 If the Organization does not exist.
     * @return Full details about the newly created Client.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientAlreadyExistsException when trying to create an Client that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts the create with an invalid name
     */
    @POST
    @Path("{organizationId}/clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ClientBean createClient(@PathParam("organizationId") String organizationId,
            NewClientBean bean) throws OrganizationNotFoundException, ClientAlreadyExistsException,
            NotAuthorizedException, InvalidNameException;

    /**
     * Use this endpoint to retrieve information about a single Client by ID.  Note
     * that this only returns information about the Client, not about any particular
     * *version* of the Client.
     * @summary Get Client By ID
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @statuscode 200 If the Client is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Client does not exist.
     * @return An Client.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist when trying to get, update, or delete a client that does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ClientBean getClient(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId) throws ClientNotFoundException,
            NotAuthorizedException;

    /**
     * This endpoint returns audit activity information about the Client.
     * @summary Get Client Activity
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param page Which page of activity should be returned.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit information is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Client does not exist.
     * @return A list of audit activity entries.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getClientActivity(
            @PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws ClientNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Clients in the Organization.
     * @summary List Clients
     * @param organizationId The Organization ID.
     * @statuscode 200 If the list of Clients is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @return A list of Clients.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientSummaryBean> listClients(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about an Client.
     * @summary Update Client
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param bean Updated Client information.
     * @statuscode 204 If the Client is updated successfully.
     * @statuscode 404 If the Client does not exist.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/clients/{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateClient(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, UpdateClientBean bean)
            throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the Client.
     * @summary Create Client Version
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param bean Initial information about the new Client version.
     * @statuscode 200 If the Client version is created successfully.
     * @statuscode 404 If the Client does not exist.
     * @statuscode 409 If the Client version already exists.
     * @return Full details about the newly created Client version.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version value
     */
    @POST
    @Path("{organizationId}/clients/{clientId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ClientVersionBean createClientVersion(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, NewClientVersionBean bean)
            throws ClientNotFoundException, NotAuthorizedException, InvalidVersionException,
            ClientVersionAlreadyExistsException;

    /**
     * Use this endpoint to list all of the versions of an Client.
     * @summary List Client Versions
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @statuscode 200 If the list of Client versions is successfully returned.
     * @return A list of Clients.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClientVersionSummaryBean> listClientVersions(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * an Client.
     * @summary Get Client Version
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @statuscode 200 If the Client version is successfully returned.
     * @statuscode 404 If the Client version does not exist.
     * @return An Client version.
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ClientVersionBean getClientVersion(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws ClientVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * Client.
     * @summary Get Client Version Activity
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param page Which page of activity data to return.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit activity entries are successfully returned.
     * @statuscode 404 If the Client version does not exist.
     * @return A list of audit entries.
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getClientVersionActivity(
            @PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ClientVersionNotFoundException, NotAuthorizedException;

    /**
     * Retrieves metrics/analytics information for a specific client.  This will
     * return request count data broken down by API.  It basically answers
     * the question "which APIs is my client really using?".
     *
     * @summary Get Client Usage Metrics (per API)
     * @param organizationId The organization ID.
     * @param clientId The client ID.
     * @param version The client version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/metrics/apiUsage")
    @Produces(MediaType.APPLICATION_JSON)
    public ClientUsagePerApiBean getClientUsagePerApi(
            @PathParam("organizationId") String organizationId, @PathParam("clientId") String clientId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;


    /**
     * Use this endpoint to create a Contract between the Client and an API.  In order
     * to create a Contract, the caller must specify the Organization, ID, and Version of the
     * API.  Additionally the caller must specify the ID of the Plan it wished to use for
     * the Contract with the API.
     * @summary Create an API Contract
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param bean Required information about the new Contract.
     * @statuscode 200 If the Contract is successfully created.
     * @statuscode 404 If the Client version does not exist.
     * @return Full details about the newly created Contract.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws ContractAlreadyExistsException when trying to create an Contract that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ContractBean createContract(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            NewContractBean bean) throws OrganizationNotFoundException, ClientNotFoundException,
            ApiNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException;

    /**
     * Use this endpoint to retrieve detailed information about a single API Contract
     * for an Client.
     * @summary Get API Contract
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param contractId The ID of the Contract.
     * @statuscode 200 If the Contract is successfully returned.
     * @statuscode 404 If the Client version does not exist.
     * @statuscode 404 If the Contract is not found.
     * @return Details about a single Contract.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws ContractNotFoundException when trying to get, update, or delete a contract that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts/{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ContractBean getContract(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            @PathParam("contractId") Long contractId) throws ClientNotFoundException,
            ContractNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Contracts for an Client.
     * @summary List All Contracts for an Client
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @statuscode 200 If the list of Contracts is successfully returned.
     * @statuscode 404 If the Client is not found.
     * @return A list of Contracts.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContractSummaryBean> getclientVersionContracts(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get registry style information about all APIs that this
     * Client consumes.  This is a useful endpoint to invoke in order to retrieve
     * a summary of every API consumed by the client.  The information returned
     * by this endpoint could potentially be included directly in a client
     * as a way to lookup endpoint information for the APIs it wishes to consume.  This
     * variant of the API Registry is formatted as JSON data.
     *
     * Note that, optionally, you can generate a temporary download link instead of
     * getting the registry file directly.  To do this, simply pass download=true as
     * a query parameter.  The result will then be a JSON object with information about
     * the temporary download link.  The ID of the download can then be used when making
     * a call to the /downloads/{downloadId} endpoint to fetch the actual content.
     *
     * @summary Get API Registry (JSON)
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param download Query parameter set to true in order to generate a download link.
     * @statuscode 200 If the API Registry information is successfully returned.
     * @statuscode 404 If the Client does not exist.
     * @return API Registry information or temporary download information.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/apiregistry/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiRegistryJSON(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            @QueryParam("download") String download)
                    throws ClientNotFoundException, NotAuthorizedException;
    public Response getApiRegistryJSON(String organizationId, String clientId, String version,
            boolean hasPermission) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get registry style information about all APIs that this
     * Client consumes.  This is a useful endpoint to invoke in order to retrieve
     * a summary of every API consumed by the client.  The information returned
     * by this endpoint could potentially be included directly in a client
     * as a way to lookup endpoint information for the APIs it wishes to consume.  This
     * variant of the API Registry is formatted as XML data.
     *
     * Note that, optionally, you can generate a temporary download link instead of
     * getting the registry file directly.  To do this, simply pass download=true as
     * a query parameter.  The result will then be a JSON object with information about
     * the temporary download link.  The ID of the download can then be used when making
     * a call to the /downloads/{downloadId} endpoint to fetch the actual content.
     *
     * @summary Get API Registry (XML)
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param download Query parameter set to true in order to generate a download link.
     * @statuscode 200 If the API Registry information is successfully returned.
     * @statuscode 404 If the Client does not exist.
     * @return API Registry information.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/apiregistry/xml")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response getApiRegistryXML(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            @QueryParam("download") String download)
                    throws ClientNotFoundException, NotAuthorizedException;
    public Response getApiRegistryXML(String organizationId, String clientId, String version,
            boolean hasPermission) throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to break all contracts between this client and its APIs.
     * @summary Break All Contracts
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @statuscode 200 If the operation is successful.
     * @statuscode 404 If the Client does not exist.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts")
    public void deleteAllContracts(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws ClientNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to break a Contract with an API.
     * @summary Break Contract
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param contractId The Contract ID.
     * @statuscode 204 If the Contract is successfully broken.
     * @statuscode 404 If the Client does not exist.
     * @statuscode 404 If the Contract does not exist.
     * @throws ClientNotFoundException when trying to get, update, or delete a client that does not exist
     * @throws ContractNotFoundException when trying to get, update, or delete a contract that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/clients/{clientId}/versions/{version}/contracts/{contractId}")
    public void deleteContract(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            @PathParam("contractId") Long contractId) throws ClientNotFoundException,
            ContractNotFoundException, NotAuthorizedException, InvalidClientStatusException;

    /**
     * Use this endpoint to add a new Policy to the Client version.
     * @summary Add Client Policy
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param bean Information about the new Policy.
     * @statuscode 200 If the Policy is successfully added.
     * @statuscode 404 If the Client does not exist.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createClientPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, ClientVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the Client version.
     * @summary Get Client Policy
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param policyId The Policy ID.
     * @statuscode 200 If the Policy is successfully returned.
     * @statuscode 404 If the Client does not exist.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getClientPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ClientVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single Client Policy.
     * @summary Update Client Policy
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param policyId The Policy ID.
     * @param bean New meta-data and/or configuration for the Policy.
     * @statuscode 204 If the Policy was successfully updated.
     * @statuscode 404 If the Client does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateClientPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ClientVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the Client.
     * @summary Remove Client Policy
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param policyId The Policy ID.
     * @statuscode 204 If the Policy was successfully deleted.
     * @statuscode 404 If the Client does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies/{policyId}")
    public void deleteClientPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ClientVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all of the Policies configured for the Client.
     * @summary List All Client Policies
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @statuscode 200 If the list of Policies is successfully returned.
     * @statuscode 404 If the Client does not exist.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/clients/{clientId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listClientPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("clientId") String clientId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ClientVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to change the order of Policies for an Client.  When a
     * Policy is added to the Client, it is added as the last Policy in the list
     * of Client Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all of the Policies for the Client, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @summary Re-Order Client Policies
     * @param organizationId The Organization ID.
     * @param clientId The Client ID.
     * @param version The Client version.
     * @param policyChain The Policies in the desired order.
     * @statuscode 204 If the re-ordering of Policies was successful.
     * @statuscode 404 If the Client does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ClientVersionNotFoundException when trying to get, update, or delete a client version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/clients/{clientId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
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
     * @summary Create API
     * @param organizationId The Organization ID.
     * @param bean Information about the new API.
     * @statuscode 200 If the API is successfully created.
     * @statuscode 404 If the Organization does not exist.
     * @return Full details about the newly created API.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiAlreadyExistsException when trying to create an API that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts the create with an invalid name
     */
    @POST
    @Path("{organizationId}/apis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApiBean createApi(@PathParam("organizationId") String organizationId, NewApiBean bean)
            throws OrganizationNotFoundException, ApiAlreadyExistsException, NotAuthorizedException,
            InvalidNameException;

    /**
     * Use this endpoint to retrieve information about a single API by ID.  Note
     * that this only returns information about the API, not about any particular
     * *version* of the API.
     * @summary Get API By ID
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @statuscode 200 If the API is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the API does not exist.
     * @return A API.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiBean getApi(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId) throws ApiNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all APIs in the Organization.
     * @summary List APIs
     * @param organizationId The Organization ID.
     * @statuscode 200 If the list of APIs is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @return A list of APIs.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiSummaryBean> listApi(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about an API.
     * @summary Update API
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param bean Updated API information.
     * @statuscode 204 If the API is updated successfully.
     * @statuscode 404 If the API does not exist.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/apis/{apiId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateApi(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, UpdateApiBean bean)
            throws ApiNotFoundException, NotAuthorizedException;

    /**
     * This endpoint returns audit activity information about the API.
     * @summary Get API Activity
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param page Which page of activity should be returned.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit information is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the API does not exist.
     * @return A list of audit activity entries.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getApiActivity(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws ApiNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the API.
     * @summary Create API Version
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param bean Initial information about the new API version.
     * @statuscode 200 If the API version is created successfully.
     * @statuscode 404 If the API does not exist.
     * @statuscode 409 If the API version already exists.
     * @return Full details about the newly created API version.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version value
     */
    @POST
    @Path("{organizationId}/apis/{apiId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApiVersionBean createApiVersion(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, NewApiVersionBean bean)
            throws ApiNotFoundException, NotAuthorizedException, InvalidVersionException,
            ApiVersionAlreadyExistsException;

    /**
     * Use this endpoint to list all of the versions of an API.
     * @summary List API Versions
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @statuscode 200 If the list of API versions is successfully returned.
     * @return A list of APIs.
     * @throws ApiNotFoundException when trying to get, update, or delete an API that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiVersionSummaryBean> listApiVersions(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId) throws ApiNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * an API.
     * @summary Get API Version
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 200 If the API version is successfully returned.
     * @statuscode 404 If the API version does not exist.
     * @return A API version.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiVersionBean getApiVersion(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get detailed status information for a single version of an
     * API.  This is useful to figure out why an API is not yet in the 'Ready'
     * state (which is required before it can be published to a Gateway).
     *
     * @summary Get API Version Status
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 200 If the status information is successfully returned.
     * @statuscode 404 If the API version does not exist.
     * @return Status information about an API version.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiVersionStatusBean getApiVersionStatus(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve the API's definition document.  An API
     * definition document can be several different types, depending on the API
     * type and technology used to define the API.  For example, this endpoint
     * might return a WSDL document, or a Swagger JSON document.
     * @summary Get API Definition
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 200 If the API definition is successfully returned.
     * @statuscode 404 If the API version does not exist.
     * @return The API Definition document (e.g. a Swagger JSON file).
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Produces({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    public Response getApiDefinition(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get information about the Managed API's gateway
     * endpoint.  In other words, this returns the actual live endpoint on the
     * API Gateway - the endpoint that a client should use when invoking the API.
     * @summary Get API Endpoint
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 200 If the endpoint information is successfully returned.
     * @statuscode 404 If the API does not exist.
     * @return The live API endpoint information.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws InvalidApiStatusException when the user attempts some action on the API when it is not in an appropriate state/status
     * @throws GatewayNotFoundException when trying to get, update, or delete a gateay that does not exist
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException, InvalidApiStatusException, GatewayNotFoundException;

    /**
     * Use this endpoint to update information about a single version of an API.
     * @summary Update API Version
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param bean Updated information about the API version.
     * @return The updated API Version.
     * @statuscode 204 If the API version information was successfully updated.
     * @statuscode 404 If the API does not exist.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidApiStatusException when the user attempts some action on the API when it is not in an appropriate state/status
     */
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApiVersionBean updateApiVersion(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            UpdateApiVersionBean bean) throws ApiVersionNotFoundException, NotAuthorizedException,
            InvalidApiStatusException;

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
     * @summary Update API Definition
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 204 If the API definition was successfully updated.
     * @statuscode 404 If the API does not exist.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidApiStatusException when the user attempts some action on the API when it is not in an appropriate state/status
     */
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Consumes({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    public void updateApiDefinition(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;


    /**
     * Use this endpoint to update the API's definition document by providing
     * a URL (reference) to the definition.  This is an alternative to providing the
     * full API definition document via a PUT to the same endpoint.  This endpoint
     * can be used to either add a definition if one does not already exist, as well
     * as update/replace an existing definition.
     *
     * Note that apiman will not store the definition reference, but instead will
     * download the API definition document and store it.  Additionally, the
     * the API's "Definition Type" field will be updated.
     * @summary Update API Definition from URL
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param bean The API definition reference information.
     * @statuscode 204 If the API definition was successfully updated.
     * @statuscode 404 If the API does not exist.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidApiStatusException when the user attempts some action on the API when it is not in an appropriate state/status
     */
    @POST
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateApiDefinitionFromURL(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version, NewApiDefinitionBean bean)
            throws ApiVersionNotFoundException, NotAuthorizedException, InvalidApiStatusException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * API.
     * @summary Get API Version Activity
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param page Which page of activity data to return.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit activity entries are successfully returned.
     * @statuscode 404 If the API version does not exist.
     * @return A list of audit entries.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getApiVersionActivity(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list the Plans configured for the given API version.
     * @summary List API Plans
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 200 If the API plans are successfully returned.
     * @statuscode 404 If the API cannot be found.
     * @return A list of API plans.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiPlanSummaryBean> getApiVersionPlans(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to add a new Policy to the API version.
     * @summary Add API Policy
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param bean Information about the new Policy.
     * @statuscode 200 If the Policy is successfully added.
     * @statuscode 404 If the API does not exist.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createApiPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, ApiVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the API version.
     * @summary Get API Policy
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param policyId The Policy ID.
     * @statuscode 200 If the Policy is successfully returned.
     * @statuscode 404 If the API does not exist.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getApiPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApiVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single API Policy.
     * @summary Update API Policy
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param policyId The Policy ID.
     * @param bean New meta-data and/or configuration for the Policy.
     * @statuscode 204 If the Policy was successfully updated.
     * @statuscode 404 If the API does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateApiPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ApiVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the API.
     * @summary Remove API Policy
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param policyId The Policy ID.
     * @statuscode 204 If the Policy was successfully deleted.
     * @statuscode 404 If the API does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies/{policyId}")
    public void deleteApiPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApiVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete an API's definition document.  When this
     * is done, the "definitionType" field on the API will be set to None.
     * @summary Remove API Definition
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 204 If the API definition was successfully deleted.
     * @statuscode 404 If the API does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/apis/{apiId}/versions/{version}/definition")
    public void deleteApiDefinition(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ApiVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to list all of the Policies configured for the API.
     * @summary List All API Policies
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @statuscode 200 If the list of Policies is successfully returned.
     * @statuscode 404 If the API does not exist.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listApiPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ApiVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to change the order of Policies for an API.  When a
     * Policy is added to the API, it is added as the last Policy in the list
     * of API Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all of the Policies for the API, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @summary Re-Order API Policies
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param policyChain The Policies in the desired order.
     * @statuscode 204 If the re-ordering of Policies was successful.
     * @statuscode 404 If the API does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/apis/{apiId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    public void reorderApiPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ApiVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a Policy Chain for the specific API version.  A
     * Policy Chain is a useful summary to better understand which Policies would be
     * executed for a request to this API through a particular Plan offered by the
     * API.  Often this information is interesting prior to create a Contract with
     * the API.
     * @summary Get API Policy Chain
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param planId The Plan ID.
     * @statuscode 200 If the Policy Chain is successfully returned.
     * @statuscode 404 If the API does not exist.
     * @return A Policy Chain.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/plans/{planId}/policyChain")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyChainBean getApiPolicyChain(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            @PathParam("planId") String planId) throws ApiVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Contracts created with this API.  This
     * will return Contracts created by between any Client and through any Plan.
     * @summary List API Contracts
     * @param organizationId The Organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param page Which page of Contracts to return.
     * @param pageSize The number of Contracts per page to return.
     * @statuscode 200 If the list of Contracts is successfully returned.
     * @statuscode 404 If the API does not exist.
     * @return A list of Contracts.
     * @throws ApiVersionNotFoundException when trying to get, update, or delete an API version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContractSummaryBean> getApiVersionContracts(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @PathParam("version") String version, @QueryParam("page") int page,
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
     * @summary Create Plan
     * @param organizationId The Organization ID.
     * @param bean Information about the new Plan.
     * @statuscode 200 If the Plan is successfully created.
     * @statuscode 404 If the Organization does not exist.
     * @return Full details about the newly created Plan.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanAlreadyExistsException when trying to create an Plan that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidNameException when the user attempts the create with an invalid name
     */
    @POST
    @Path("{organizationId}/plans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanBean createPlan(@PathParam("organizationId") String organizationId, NewPlanBean bean)
            throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException,
            InvalidNameException;

    /**
     * Use this endpoint to retrieve information about a single Plan by ID.  Note
     * that this only returns information about the Plan, not about any particular
     * *version* of the Plan.
     * @summary Get Plan By ID
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @statuscode 200 If the Plan is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Plan does not exist.
     * @return An Plan.
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans/{planId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PlanBean getPlan(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId) throws PlanNotFoundException,
            NotAuthorizedException;

    /**
     * This endpoint returns audit activity information about the Plan.
     * @summary Get Plan Activity
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param page Which page of activity should be returned.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit information is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Plan does not exist.
     * @return A list of audit activity entries.
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans/{planId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getPlanActivity(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws PlanNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Plans in the Organization.
     * @summary List Plans
     * @param organizationId The Organization ID.
     * @statuscode 200 If the list of Plans is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @return A list of Plans.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlanSummaryBean> listPlans(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about a Plan.
     * @summary Update Plan
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param bean Updated Plan information.
     * @throws PlanNotFoundException  when trying to get, update, or delete a plan that does not exist
     * @statuscode 204 If the Plan is updated successfully.
     * @statuscode 404 If the Plan does not exist.
     * when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/plans/{planId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updatePlan(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, UpdatePlanBean bean)
            throws PlanNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the Plan.
     * @summary Create Plan Version
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param bean Initial information about the new Plan version.
     * @statuscode 200 If the Plan version is created successfully.
     * @statuscode 404 If the Plan does not exist.
     * @statuscode 409 If the Plan version already exists.
     * @return Full details about the newly created Plan version.
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidVersionException when the user attempts to use an invalid version value
     */
    @POST
    @Path("{organizationId}/plans/{planId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanVersionBean createPlanVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, NewPlanVersionBean bean) throws PlanNotFoundException,
            NotAuthorizedException, InvalidVersionException, PlanVersionAlreadyExistsException;

    /**
     * Use this endpoint to list all of the versions of a Plan.
     * @summary List Plan Versions
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @statuscode 200 If the list of Plan versions is successfully returned.
     * @return A list of Plans.
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans/{planId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlanVersionSummaryBean> listPlanVersions(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId) throws PlanNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * a Plan.
     * @summary Get Plan Version
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @statuscode 200 If the Plan version is successfully returned.
     * @statuscode 404 If the Plan version does not exist.
     * @return An Plan version.
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public PlanVersionBean getPlanVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version)
            throws PlanVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * Plan.
     * @summary Get Plan Version Activity
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @param page Which page of activity data to return.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit activity entries are successfully returned.
     * @statuscode 404 If the Plan version does not exist.
     * @return A list of audit entries.
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getPlanVersionActivity(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @QueryParam("page") int page, @QueryParam("count") int pageSize)
            throws PlanVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to add a new Policy to the Plan version.
     * @summary Add Plan Policy
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @param bean Information about the new Policy.
     * @statuscode 200 If the Policy is successfully added.
     * @statuscode 404 If the Plan does not exist.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createPlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the Plan version.
     * @summary Get Plan Policy
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @param policyId The Policy ID.
     * @statuscode 200 If the Policy is successfully returned.
     * @statuscode 404 If the Plan does not exist.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getPlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single Plan Policy.
     * @summary Update Plan Policy
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @param policyId The Policy ID.
     * @param bean New meta-data and/or configuration for the Policy.
     * @statuscode 204 If the Policy was successfully updated.
     * @statuscode 404 If the Plan does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updatePlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the Plan.
     * @summary Remove Plan Policy
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @param policyId The Policy ID.
     * @statuscode 204 If the Policy was successfully deleted.
     * @statuscode 404 If the Plan does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    public void deletePlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all of the Policies configured for the Plan.
     * @summary List All Plan Policies
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @statuscode 200 If the list of Policies is successfully returned.
     * @statuscode 404 If the Plan does not exist.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listPlanPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to change the order of Policies for a Plan.  When a
     * Policy is added to the Plan, it is added as the last Policy in the list
     * of Plan Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all of the Policies for the Plan, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @summary Re-Order Plan Policies
     * @param organizationId The Organization ID.
     * @param planId The Plan ID.
     * @param version The Plan version.
     * @param policyChain The Policies in the desired order.
     * @statuscode 204 If the re-ordering of Policies was successful.
     * @statuscode 404 If the Plan does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws PlanVersionNotFoundException when trying to get, update, or delete a plan version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/plans/{planId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    public void reorderPlanPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, NotAuthorizedException;

    /*
     * MEMBERS
     */

    /**
     * Grant membership in a role to a user.
     * @summary Grant Membership(s)
     * @param organizationId The Organization ID.
     * @param bean Roles to grant, and the ID of the user.
     * @statuscode 204 If the membership(s) were successfully granted.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    public void grant(@PathParam("organizationId") String organizationId, GrantRolesBean bean)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException;

    /**
     * Revoke membership in a role.
     * @summary Revoke Single Membership
     * @param organizationId The organization ID.
     * @param roleId The role ID.
     * @param userId The user ID.
     * @statuscode 204 If the membership was successfully revoked.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/roles/{roleId}/{userId}")
    public void revoke(@PathParam("organizationId") String organizationId,
            @PathParam("roleId") String roleId, @PathParam("userId") String userId)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
            NotAuthorizedException;

    /**
     * Revoke all of a user's role memberships from the org.
     * @summary Revoke All Memberships
     * @param organizationId The organization ID.
     * @param userId The user ID.
     * @statuscode 204 If the user's memberships were successfully revoked.
     * @statuscode 404 If the user does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws RoleNotFoundException when a request is sent for a role that does not exist
     * @throws UserNotFoundException when a request is sent for a user who does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/members/{userId}")
    public void revokeAll(@PathParam("organizationId") String organizationId,
            @PathParam("userId") String userId) throws OrganizationNotFoundException, RoleNotFoundException,
            UserNotFoundException, NotAuthorizedException;

    /**
     * Lists all members of the organization.
     * @summary List Organization Members
     * @param organizationId The organization ID.
     * @statuscode 200 If the list of members is returned successfully.
     * @return List of members.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/members")
    @Produces(MediaType.APPLICATION_JSON)
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
     * @summary Get API Usage Metrics
     * @param organizationId The organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param interval A valid interval (month, week, day, hour, minute)
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/usage")
    @Produces(MediaType.APPLICATION_JSON)
    public UsageHistogramBean getUsage(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            @QueryParam("interval") HistogramIntervalType interval, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return request count data broken down by client.  It basically answers
     * the question "who is calling my API?".
     *
     * @summary Get API Usage Metrics (per Client)
     * @param organizationId The organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/clientUsage")
    @Produces(MediaType.APPLICATION_JSON)
    public UsagePerClientBean getUsagePerClient(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;


    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return request count data broken down by plan.  It basically answers
     * the question "which API plans are most used?".
     *
     * @summary Get API Usage Metrics (per Plan)
     * @param organizationId The organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/planUsage")
    @Produces(MediaType.APPLICATION_JSON)
    public UsagePerPlanBean getUsagePerPlan(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;


    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return a full histogram of response statistics data based on the provided date range
     * and interval.  Valid intervals are:  month, week, day, hour, minute
     *
     * The data returned includes total request counts, failure counts, and error counts
     * for each data point in the histogram.
     *
     * @summary Get API Response Statistics (Histogram)
     * @param organizationId The organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param interval A valid interval (month, week, day, hour, minute)
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Response statistics metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/responseStats")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseStatsHistogramBean getResponseStats(@PathParam("organizationId") String organizationId,
            @PathParam("apiId") String apiId, @PathParam("version") String version,
            @QueryParam("interval") HistogramIntervalType interval, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return total response type statistics over the given date range.  Basically
     * this will return three numbers: total request, # failed responses, # error
     * responses.
     *
     * @summary Get API Response Statistics (Summary)
     * @param organizationId The organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/summaryResponseStats")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseStatsSummaryBean getResponseStatsSummary(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return response type statistics broken down by client.
     *
     * @summary Get API Response Statistics (per Client)
     * @param organizationId The organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/clientResponseStats")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseStatsPerClientBean getResponseStatsPerClient(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;


    /**
     * Retrieves metrics/analytics information for a specific API.  This will
     * return response type statistics broken down by plan.
     *
     * @summary Get API Response Statistics (per Plan)
     * @param organizationId The organization ID.
     * @param apiId The API ID.
     * @param version The API version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/apis/{apiId}/versions/{version}/metrics/planResponseStats")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseStatsPerPlanBean getResponseStatsPerPlan(
            @PathParam("organizationId") String organizationId, @PathParam("apiId") String apiId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException, InvalidMetricCriteriaException;

}
