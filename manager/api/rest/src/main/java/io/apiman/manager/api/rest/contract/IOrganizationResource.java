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

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.apps.NewApplicationBean;
import io.apiman.manager.api.beans.apps.NewApplicationVersionBean;
import io.apiman.manager.api.beans.apps.UpdateApplicationBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.idm.GrantRolesBean;
import io.apiman.manager.api.beans.members.MemberBean;
import io.apiman.manager.api.beans.metrics.UsageHistogramBean;
import io.apiman.manager.api.beans.metrics.UsagePerAppBean;
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
import io.apiman.manager.api.beans.services.NewServiceBean;
import io.apiman.manager.api.beans.services.NewServiceVersionBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.services.UpdateServiceBean;
import io.apiman.manager.api.beans.services.UpdateServiceVersionBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.ApplicationVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.beans.summary.ServicePlanSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ContractAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ContractNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.InvalidServiceStatusException;
import io.apiman.manager.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.PlanNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.RoleNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceAlreadyExistsException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.UserNotFoundException;

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
public interface IOrganizationResource {

    /**
     * Use this endpoint to create a new Organization.
     * @summary Create Organization
     * @param bean Information about the new Organization.
     * @statuscode 200 If the Organization was successfully created.
     * @return Full details about the Organization that was created.
     * @throws OrganizationAlreadyExistsException when trying to create an Organization that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationBean create(NewOrganizationBean bean) throws OrganizationAlreadyExistsException, NotAuthorizedException;

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
     * Use this endpoint to create a new Application.  Note that it is important to also
     * create an initial version of the Application (e.g. 1.0).  This can either be done
     * by including the 'initialVersion' property in the request, or by immediately following
     * up with a call to "Create Application Version".  If the former is done, then a first
     * Application version will be created automatically by this endpoint.
     * @summary Create Application
     * @param organizationId The Organization ID.
     * @param bean Information about the new Application.
     * @statuscode 200 If the Application is successfully created.
     * @statuscode 404 If the Organization does not exist.
     * @return Full details about the newly created Application.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationAlreadyExistsException when trying to create an Application that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/applications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationBean createApp(@PathParam("organizationId") String organizationId, NewApplicationBean bean)
            throws OrganizationNotFoundException, ApplicationAlreadyExistsException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve information about a single Application by ID.  Note
     * that this only returns information about the Application, not about any particular
     * *version* of the Application.
     * @summary Get Application By ID
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @statuscode 200 If the Application is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Application does not exist.
     * @return An Application.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist when trying to get, update, or delete an application that does not exist.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationBean getApp(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId) throws ApplicationNotFoundException,
            NotAuthorizedException;

    /**
     * This endpoint returns audit activity information about the Application.
     * @summary Get Application Activity
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param page Which page of activity should be returned.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit information is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Application does not exist.
     * @return A list of audit activity entries.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getAppActivity(
            @PathParam("organizationId") String organizationId, @PathParam("applicationId") String applicationId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws ApplicationNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Applications in the Organization.
     * @summary List Applications
     * @param organizationId The Organization ID.
     * @statuscode 200 If the list of Applications is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @return A list of Applications.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApplicationSummaryBean> listApps(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about an Application.
     * @summary Update Application
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param bean Updated Application information.
     * @statuscode 204 If the Application is updated successfully.
     * @statuscode 404 If the Application does not exist.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/applications/{applicationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateApp(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, UpdateApplicationBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the Application.
     * @summary Create Application Version
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param bean Initial information about the new Application version.
     * @statuscode 200 If the Application version is created successfully.
     * @statuscode 404 If the Application does not exist.
     * @return Full details about the newly created Application version.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/applications/{applicationId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationVersionBean createAppVersion(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, NewApplicationVersionBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all of the versions of an Application.
     * @summary List Application Versions
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @statuscode 200 If the list of Application versions is successfully returned.
     * @return A list of Applications.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApplicationVersionSummaryBean> listAppVersions(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId) throws ApplicationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * an Application.
     * @summary Get Application Version
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @statuscode 200 If the Application version is successfully returned.
     * @statuscode 404 If the Application version does not exist.
     * @return An Application version.
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationVersionBean getAppVersion(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * Application.
     * @summary Get Application Version Activity
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param page Which page of activity data to return.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit activity entries are successfully returned.
     * @statuscode 404 If the Application version does not exist.
     * @return A list of audit entries.
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getAppVersionActivity(
            @PathParam("organizationId") String organizationId, @PathParam("applicationId") String applicationId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ApplicationVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to create a Contract between the Application and a Service.  In order
     * to create a Contract, the caller must specify the Organization, ID, and Version of the
     * Service.  Additionally the caller must specify the ID of the Plan it wished to use for
     * the Contract with the Service.
     * @summary Create a Service Contract
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param bean Required information about the new Contract.
     * @statuscode 200 If the Contract is successfully created.
     * @statuscode 404 If the Application version does not exist.
     * @return Full details about the newly created Contract.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws ServiceNotFoundException when trying to get, update, or delete an service that does not exist
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws ContractAlreadyExistsException when trying to create an Contract that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ContractBean createContract(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            NewContractBean bean) throws OrganizationNotFoundException, ApplicationNotFoundException,
            ServiceNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException;

    /**
     * Use this endpoint to retrieve detailed information about a single Service Contract
     * for an Application.
     * @summary Get Service Contract
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param contractId The ID of the Contract.
     * @statuscode 200 If the Contract is successfully returned.
     * @statuscode 404 If the Application version does not exist.
     * @statuscode 404 If the Contract is not found.
     * @return Details about a single Contract.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws ContractNotFoundException when trying to get, update, or delete a contract that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts/{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ContractBean getContract(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("contractId") Long contractId) throws ApplicationNotFoundException,
            ContractNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Contracts for an Application.
     * @summary List All Contracts for an Application
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @statuscode 200 If the list of Contracts is successfully returned.
     * @statuscode 404 If the Application is not found.
     * @return A list of Contracts.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContractSummaryBean> getApplicationVersionContracts(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get registry style information about all Services that this
     * Application consumes.  This is a useful endpoint to invoke in order to retrieve
     * a summary of every Service consumed by the application.  The information returned
     * by this endpoint could potentially be included directly in a client application
     * as a way to lookup endpoint information for the APIs it wishes to consume.  This
     * variant of the API Registry is formatted as JSON data.
     * @summary Get API Registry (JSON)
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @statuscode 200 If the API Registry information is successfully returned.
     * @statuscode 404 If the Application does not exist.
     * @return API Registry information.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/apiregistry/json")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiRegistryBean getApiRegistryJSON(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get registry style information about all Services that this
     * Application consumes.  This is a useful endpoint to invoke in order to retrieve
     * a summary of every Service consumed by the application.  The information returned
     * by this endpoint could potentially be included directly in a client application
     * as a way to lookup endpoint information for the APIs it wishes to consume.  This
     * variant of the API Registry is formatted as XML data.
     * @summary Get API Registry (XML)
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @statuscode 200 If the API Registry information is successfully returned.
     * @statuscode 404 If the Application does not exist.
     * @return API Registry information.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/apiregistry/xml")
    @Produces(MediaType.APPLICATION_XML)
    public ApiRegistryBean getApiRegistryXML(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to break all contracts between this application and its services.
     * @summary Break All Contracts
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @statuscode 200 If the operation is successful.
     * @statuscode 404 If the Application does not exist.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts")
    public void deleteAllContracts(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to break a Contract with a Service.
     * @summary Break Contract
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param contractId The Contract ID.
     * @statuscode 200 If the Contract is successfully broken.
     * @statuscode 404 If the Application does not exist.
     * @statuscode 404 If the Contract does not exist.
     * @throws ApplicationNotFoundException when trying to get, update, or delete an application that does not exist
     * @throws ContractNotFoundException when trying to get, update, or delete a contract that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts/{contractId}")
    public void deleteContract(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("contractId") Long contractId) throws ApplicationNotFoundException,
            ContractNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to add a new Policy to the Application version.
     * @summary Add Application Policy
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param bean Information about the new Policy.
     * @statuscode 200 If the Policy is successfully added.
     * @statuscode 404 If the Application does not exist.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the Application version.
     * @summary Get Application Policy
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param policyId The Policy ID.
     * @statuscode 200 If the Policy is successfully returned.
     * @statuscode 404 If the Application does not exist.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single Application Policy.
     * @summary Update Application Policy
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param policyId The Policy ID.
     * @param bean New meta-data and/or configuration for the Policy.
     * @statuscode 204 If the Policy was successfully updated.
     * @statuscode 404 If the Application does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ApplicationVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the Application.
     * @summary Remove Application Policy
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param policyId The Policy ID.
     * @statuscode 204 If the Policy was successfully deleted.
     * @statuscode 404 If the Application does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies/{policyId}")
    public void deleteAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all of the Policies configured for the Application.
     * @summary List All Application Policies
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @statuscode 200 If the list of Policies is successfully returned.
     * @statuscode 404 If the Application does not exist.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listAppPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to change the order of Policies for an Application.  When a
     * Policy is added to the Application, it is added as the last Policy in the list
     * of Application Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all of the Policies for the Application, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @summary Re-Order Application Policies
     * @param organizationId The Organization ID.
     * @param applicationId The Application ID.
     * @param version The Application version.
     * @param policyChain The Policies in the desired order.
     * @statuscode 204 If the re-ordering of Policies was successful.
     * @statuscode 404 If the Application does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ApplicationVersionNotFoundException when trying to get, update, or delete a application version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    public void reorderApplicationPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ApplicationVersionNotFoundException, NotAuthorizedException;

    /*
     * SERVICES
     */

    /**
     * Use this endpoint to create a new Service.  Note that it is important to also
     * create an initial version of the Service (e.g. 1.0).  This can either be done
     * by including the 'initialVersion' property in the request, or by immediately following
     * up with a call to "Create Service Version".  If the former is done, then a first
     * Service version will be created automatically by this endpoint.
     * @summary Create Service
     * @param organizationId The Organization ID.
     * @param bean Information about the new Service.
     * @statuscode 200 If the Service is successfully created.
     * @statuscode 404 If the Organization does not exist.
     * @return Full details about the newly created Service.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceAlreadyExistsException when trying to create an Service that already exists
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/services")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceBean createService(@PathParam("organizationId") String organizationId, NewServiceBean bean)
            throws OrganizationNotFoundException, ServiceAlreadyExistsException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve information about a single Service by ID.  Note
     * that this only returns information about the Service, not about any particular
     * *version* of the Service.
     * @summary Get Service By ID
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @statuscode 200 If the Service is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Service does not exist.
     * @return A Service.
     * @throws ServiceNotFoundException when trying to get, update, or delete an service that does not exist when trying to get, update, or delete an service that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceBean getService(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId) throws ServiceNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Services in the Organization.
     * @summary List Services
     * @param organizationId The Organization ID.
     * @statuscode 200 If the list of Services is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @return A list of Services.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceSummaryBean> listServices(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update information about a Service.
     * @summary Update Service
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param bean Updated Service information.
     * @statuscode 204 If the Service is updated successfully.
     * @statuscode 404 If the Service does not exist.
     * @throws ServiceNotFoundException when trying to get, update, or delete an service that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/services/{serviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateService(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, UpdateServiceBean bean)
            throws ServiceNotFoundException, NotAuthorizedException;

    /**
     * This endpoint returns audit activity information about the Service.
     * @summary Get Service Activity
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param page Which page of activity should be returned.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit information is successfully returned.
     * @statuscode 404 If the Organization does not exist.
     * @statuscode 404 If the Service does not exist.
     * @return A list of audit activity entries.
     * @throws ServiceNotFoundException when trying to get, update, or delete an service that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getServiceActivity(
            @PathParam("organizationId") String organizationId, @PathParam("serviceId") String serviceId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws ServiceNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to create a new version of the Service.
     * @summary Create Service Version
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param bean Initial information about the new Service version.
     * @statuscode 200 If the Service version is created successfully.
     * @statuscode 404 If the Service does not exist.
     * @return Full details about the newly created Service version.
     * @throws ServiceNotFoundException when trying to get, update, or delete an service that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/services/{serviceId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionBean createServiceVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, NewServiceVersionBean bean)
            throws ServiceNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list all of the versions of a Service.
     * @summary List Service Versions
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @statuscode 200 If the list of Service versions is successfully returned.
     * @return A list of Services.
     * @throws ServiceNotFoundException when trying to get, update, or delete an service that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceVersionSummaryBean> listServiceVersions(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId) throws ServiceNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get detailed information about a single version of
     * a Service.
     * @summary Get Service Version
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @statuscode 200 If the Service version is successfully returned.
     * @statuscode 404 If the Service version does not exist.
     * @return A Service version.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionBean getServiceVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to retrieve the Service's definition document.  A service
     * definition document can be several different types, depending on the Service
     * type and technology used to define the service.  For example, this endpoint
     * might return a WSDL document, or a Swagger JSON document.
     * @summary Get Service Definition
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @statuscode 200 If the Service definition is successfully returned.
     * @statuscode 404 If the Service version does not exist.
     * @return The Service Definition document (e.g. a Swagger JSON file).
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/definition")
    @Produces({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    public Response getServiceDefinition(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get information about the Managed Service's gateway
     * endpoint.  In other words, this returns the actual live endpoint on the
     * API Gateway - the endpoint that a client should use when invoking the Service.
     * @summary Get Service Endpoint
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @statuscode 200 If the endpoint information is successfully returned.
     * @statuscode 404 If the Service does not exist.
     * @return The live Service endpoint information.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws InvalidServiceStatusException when the user attempts some action on the service when it is not in an appropriate state/status
     * @throws GatewayNotFoundException when trying to get, update, or delete a gateay that does not exist
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionEndpointSummaryBean getServiceVersionEndpointInfo(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, InvalidServiceStatusException, GatewayNotFoundException;

    /**
     * Use this endpoint to update information about a single version of a Service.
     * @summary Update Service Version
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param bean Updated information about the Service version.
     * @return The updated Service Version.
     * @statuscode 204 If the Service version information was successfully updated.
     * @statuscode 404 If the Service does not exist.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidServiceStatusException when the user attempts some action on the service when it is not in an appropriate state/status
     */
    @PUT
    @Path("{organizationId}/services/{serviceId}/versions/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionBean updateServiceVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            UpdateServiceVersionBean bean) throws ServiceVersionNotFoundException, NotAuthorizedException,
            InvalidServiceStatusException;

    /**
     * Use this endpoint to update the Service's definition document.  A service
     * definition will vary depending on the type of service, and the type of
     * definition used.  For example, it might be a Swagger document or a WSDL file.
     * To use this endpoint, simply PUT the updated Service Definition document
     * in its entirety, making sure to set the Content-Type appropriately for the
     * type of definition document.  The content will be stored and the Service's
     * "Definition Type" field will be updated.
     * <br />
     * Whenever a service's definition is updated, the "definitionType" property of
     * that service is automatically set based on the request Content-Type.  There
     * is no other way to set the service's definition type property.  The following
     * is a map of Content-Type to service definition type.
     *
     * <table>
     *   <thead>
     *     <tr><th>Content Type</th><th>Service Definition Type</th></tr>
     *   </thead>
     *   <tbody>
     *     <tr><td>application/json</td><td>SwaggerJSON</td></tr>
     *     <tr><td>application/x-yaml</td><td>SwaggerYAML</td></tr>
     *     <tr><td>application/wsdl+xml</td><td>WSDL</td></tr>
     *   </tbody>
     * </table>
     * @summary Update Service Definition
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @statuscode 204 If the Service definition was successfully updated.
     * @statuscode 404 If the Service does not exist.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     * @throws InvalidServiceStatusException when the user attempts some action on the service when it is not in an appropriate state/status
     */
    @PUT
    @Path("{organizationId}/services/{serviceId}/versions/{version}/definition")
    @Consumes({ MediaType.APPLICATION_JSON, "application/wsdl+xml", "application/x-yaml" })
    public void updateServiceDefinition(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException, InvalidServiceStatusException;

    /**
     * Use this endpoint to get audit activity information for a single version of the
     * Service.
     * @summary Get Service Version Activity
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param page Which page of activity data to return.
     * @param pageSize The number of entries per page to return.
     * @statuscode 200 If the audit activity entries are successfully returned.
     * @statuscode 404 If the Service version does not exist.
     * @return A list of audit entries.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getServiceVersionActivity(
            @PathParam("organizationId") String organizationId, @PathParam("serviceId") String serviceId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ServiceVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to list the Plans configured for the given Service version.
     * @summary List Service Plans
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @statuscode 200 If the Service plans are successfully returned.
     * @statuscode 404 If the Service cannot be found.
     * @return A list of Service plans.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServicePlanSummaryBean> getServiceVersionPlans(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to add a new Policy to the Service version.
     * @summary Add Service Policy
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param bean Information about the new Policy.
     * @statuscode 200 If the Policy is successfully added.
     * @statuscode 404 If the Service does not exist.
     * @return Full details about the newly added Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            NewPolicyBean bean) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get information about a single Policy in the Service version.
     * @summary Get Service Policy
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param policyId The Policy ID.
     * @statuscode 200 If the Policy is successfully returned.
     * @statuscode 404 If the Service does not exist.
     * @return Full information about the Policy.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to update the meta-data or configuration of a single Service Policy.
     * @summary Update Service Policy
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param policyId The Policy ID.
     * @param bean New meta-data and/or configuration for the Policy.
     * @statuscode 204 If the Policy was successfully updated.
     * @statuscode 404 If the Service does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @PUT
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, UpdatePolicyBean bean) throws OrganizationNotFoundException,
            ServiceVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to remove a Policy from the Service.
     * @summary Remove Service Policy
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param policyId The Policy ID.
     * @statuscode 204 If the Policy was successfully deleted.
     * @statuscode 404 If the Service does not exist.
     * @statuscode 404 If the Policy does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws PolicyNotFoundException when trying to get, update, or delete a policy that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    public void deleteServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to delete a Service's definition document.  When this
     * is done, the "definitionType" field on the Service will be set to None.
     * @summary Remove Service Definition
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @statuscode 204 If the Service definition was successfully deleted.
     * @statuscode 404 If the Service does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @DELETE
    @Path("{organizationId}/services/{serviceId}/versions/{version}/definition")
    public void deleteServiceDefinition(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to list all of the Policies configured for the Service.
     * @summary List All Service Policies
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @statuscode 200 If the list of Policies is successfully returned.
     * @statuscode 404 If the Service does not exist.
     * @return A List of Policies.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listServicePolicies(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to change the order of Policies for a Service.  When a
     * Policy is added to the Service, it is added as the last Policy in the list
     * of Service Policies.  Sometimes the order of Policies is important, so it
     * is often useful to re-order the Policies by invoking this endpoint.  The body
     * of the request should include all of the Policies for the Service, in the
     * new desired order.  Note that only the IDs of each of the Policies is actually
     * required in the request, at a minimum.
     * @summary Re-Order Service Policies
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param policyChain The Policies in the desired order.
     * @statuscode 204 If the re-ordering of Policies was successful.
     * @statuscode 404 If the Service does not exist.
     * @throws OrganizationNotFoundException when trying to get, update, or delete an organization that does not exist
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/services/{serviceId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    public void reorderServicePolicies(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ServiceVersionNotFoundException, NotAuthorizedException;

    /**
     * Use this endpoint to get a Policy Chain for the specific Service version.  A
     * Policy Chain is a useful summary to better understand which Policies would be
     * executed for a request to this Service through a particular Plan offered by the
     * Service.  Often this information is interesting prior to create a Contract with
     * the Service.
     * @summary Get Service Policy Chain
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param planId The Plan ID.
     * @statuscode 200 If the Policy Chain is successfully returned.
     * @statuscode 404 If the Service does not exist.
     * @return A Policy Chain.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/plans/{planId}/policyChain")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyChainBean getServicePolicyChain(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("planId") String planId) throws ServiceVersionNotFoundException,
            NotAuthorizedException;

    /**
     * Use this endpoint to get a list of all Contracts created with this Service.  This
     * will return Contracts created by between any Application and through any Plan.
     * @summary List Service Contracts
     * @param organizationId The Organization ID.
     * @param serviceId The Service ID.
     * @param version The Service version.
     * @param page Which page of Contracts to return.
     * @param pageSize The number of Contracts per page to return.
     * @statuscode 200 If the list of Contracts is successfully returned.
     * @statuscode 404 If the Service does not exist.
     * @return A list of Contracts.
     * @throws ServiceVersionNotFoundException when trying to get, update, or delete a service version that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContractSummaryBean> getServiceVersionContracts(
            @PathParam("organizationId") String organizationId, @PathParam("serviceId") String serviceId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ServiceVersionNotFoundException, NotAuthorizedException;

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
     */
    @POST
    @Path("{organizationId}/plans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanBean createPlan(@PathParam("organizationId") String organizationId, NewPlanBean bean)
            throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException;

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
     * @return Full details about the newly created Plan version.
     * when trying to get, update, or delete an plan that does not exist
     * @throws PlanNotFoundException when trying to get, update, or delete an plan that does not exist
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @POST
    @Path("{organizationId}/plans/{planId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanVersionBean createPlanVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, NewPlanVersionBean bean)
            throws PlanNotFoundException, NotAuthorizedException;

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
     * Retrieves metrics/analytics information for a specific service.  This will
     * return a full histogram of request count data based on the provided date range
     * and interval.  Valid intervals are:  month, week, day, hour, minute
     *
     * @summary Get Service Usage Metrics
     * @param organizationId The organization ID.
     * @param serviceId The service ID.
     * @param version The service version.
     * @param interval A valid interval (month, week, day, hour, minute)
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/metrics/usage")
    @Produces(MediaType.APPLICATION_JSON)
    public UsageHistogramBean getUsage(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @QueryParam("interval") String interval, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException;

    /**
     * Retrieves metrics/analytics information for a specific service.  This will
     * return request count data broken down by application.  It basically answers
     * the question "who is calling my service?".
     *
     * @summary Get Service Usage Metrics (per App)
     * @param organizationId The organization ID.
     * @param serviceId The service ID.
     * @param version The service version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/metrics/appUsage")
    @Produces(MediaType.APPLICATION_JSON)
    public UsagePerAppBean getUsagePerApp(
            @PathParam("organizationId") String organizationId, @PathParam("serviceId") String serviceId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException;


    /**
     * Retrieves metrics/analytics information for a specific service.  This will
     * return request count data broken down by plan.  It basically answers
     * the question "which service plans are most used?".
     *
     * @summary Get Service Usage Metrics (per Plan)
     * @param organizationId The organization ID.
     * @param serviceId The service ID.
     * @param version The service version.
     * @param fromDate The start of a valid date range.
     * @param toDate The end of a valid date range.
     * @statuscode 200 If the metrics data is successfully returned.
     * @return Usage metrics information.
     * @throws NotAuthorizedException when the user attempts to do or see something that they are not authorized (do not have permission) to
     */
    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/metrics/planUsage")
    @Produces(MediaType.APPLICATION_JSON)
    public UsagePerPlanBean getUsagePerPlan(
            @PathParam("organizationId") String organizationId, @PathParam("serviceId") String serviceId,
            @PathParam("version") String version, @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) throws NotAuthorizedException;
}
