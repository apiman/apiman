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
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.orgs.UpdateOrganizationBean;
import io.apiman.manager.api.beans.plans.NewPlanBean;
import io.apiman.manager.api.beans.plans.NewPlanVersionBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plans.UpdatePlanBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
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

/**
 * The Organization API.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("organizations")
public interface IOrganizationResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationBean create(NewOrganizationBean bean) throws OrganizationAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public OrganizationBean get(@PathParam("organizationId") String organizationId) throws OrganizationNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(@PathParam("organizationId") String organizationId, UpdateOrganizationBean bean)
            throws OrganizationNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> activity(
            @PathParam("organizationId") String organizationId, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws OrganizationNotFoundException, NotAuthorizedException;

    /*
     * APPLICATIONS
     */

    @POST
    @Path("{organizationId}/applications")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationBean createApp(@PathParam("organizationId") String organizationId, NewApplicationBean bean)
            throws OrganizationNotFoundException, ApplicationAlreadyExistsException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationBean getApp(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId) throws ApplicationNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getAppActivity(
            @PathParam("organizationId") String organizationId, @PathParam("applicationId") String applicationId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws ApplicationNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApplicationSummaryBean> listApps(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/applications/{applicationId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateApp(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, UpdateApplicationBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/applications/{applicationId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationVersionBean createAppVersion(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, NewApplicationVersionBean bean)
            throws ApplicationNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApplicationVersionSummaryBean> listAppVersions(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId) throws ApplicationNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApplicationVersionBean getAppVersion(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getAppVersionActivity(
            @PathParam("organizationId") String organizationId, @PathParam("applicationId") String applicationId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ApplicationVersionNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ContractBean createContract(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            NewContractBean bean) throws OrganizationNotFoundException, ApplicationNotFoundException,
            ServiceNotFoundException, PlanNotFoundException, ContractAlreadyExistsException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts/{contractId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ContractBean getContract(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("contractId") Long contractId) throws ApplicationNotFoundException,
            ContractNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContractSummaryBean> getApplicationVersionContracts(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/apiregistry/json")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiRegistryBean getApiRegistryJSON(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/apiregistry/xml")
    @Produces(MediaType.APPLICATION_XML)
    public ApiRegistryBean getApiRegistryXML(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws ApplicationNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/contracts/{contractId}")
    public void deleteContract(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("contractId") Long contractId) throws ApplicationNotFoundException,
            ContractNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            PolicyBean bean) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            ApplicationVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies/{policyId}")
    public void deleteAppPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/applications/{applicationId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listAppPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("applicationId") String applicationId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ApplicationVersionNotFoundException,
            NotAuthorizedException;

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

    @POST
    @Path("{organizationId}/services")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceBean createService(@PathParam("organizationId") String organizationId, NewServiceBean bean)
            throws OrganizationNotFoundException, ServiceAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{organizationId}/services/{serviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceBean getService(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId) throws ServiceNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/services")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceSummaryBean> listServices(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/services/{serviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateService(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, UpdateServiceBean bean)
            throws ServiceNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getServiceActivity(
            @PathParam("organizationId") String organizationId, @PathParam("serviceId") String serviceId,
            @QueryParam("page") int page, @QueryParam("count") int pageSize) throws ServiceNotFoundException,
            NotAuthorizedException;

    @POST
    @Path("{organizationId}/services/{serviceId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionBean createServiceVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, NewServiceVersionBean bean)
            throws ServiceNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServiceVersionSummaryBean> listServiceVersions(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId) throws ServiceNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionBean getServiceVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/endpoint")
    @Produces(MediaType.APPLICATION_JSON)
    public ServiceVersionEndpointSummaryBean getServiceVersionEndpointInfo(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, InvalidServiceStatusException, GatewayNotFoundException;
    
    @PUT
    @Path("{organizationId}/services/{serviceId}/versions/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateServiceVersion(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            UpdateServiceVersionBean bean) throws ServiceVersionNotFoundException, NotAuthorizedException,
            InvalidServiceStatusException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getServiceVersionActivity(
            @PathParam("organizationId") String organizationId, @PathParam("serviceId") String serviceId,
            @PathParam("version") String version, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws ServiceVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ServicePlanSummaryBean> getServiceVersionPlans(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws ServiceVersionNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            PolicyBean bean) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            ServiceVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies/{policyId}")
    public void deleteServicePolicy(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listServicePolicies(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version)
            throws OrganizationNotFoundException, ServiceVersionNotFoundException,
            NotAuthorizedException;

    @POST
    @Path("{organizationId}/services/{serviceId}/versions/{version}/reorderPolicies")
    @Consumes(MediaType.APPLICATION_JSON)
    public void reorderServicePolicies(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            PolicyChainBean policyChain) throws OrganizationNotFoundException,
            ServiceVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/services/{serviceId}/versions/{version}/plans/{planId}/policyChain")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyChainBean getServicePolicyChain(@PathParam("organizationId") String organizationId,
            @PathParam("serviceId") String serviceId, @PathParam("version") String version,
            @PathParam("planId") String planId) throws ServiceVersionNotFoundException,
            NotAuthorizedException;

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

    @POST
    @Path("{organizationId}/plans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanBean createPlan(@PathParam("organizationId") String organizationId, NewPlanBean bean)
            throws OrganizationNotFoundException, PlanAlreadyExistsException, NotAuthorizedException;
    
    @GET
    @Path("{organizationId}/plans/{planId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PlanBean getPlan(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId) throws PlanNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getPlanActivity(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @QueryParam("page") int page,
            @QueryParam("count") int pageSize) throws PlanNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlanSummaryBean> listPlans(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/plans/{planId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updatePlan(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, UpdatePlanBean bean)
            throws PlanNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/plans/{planId}/versions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PlanVersionBean createPlanVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, NewPlanVersionBean bean)
            throws PlanNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlanVersionSummaryBean> listPlanVersions(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId) throws PlanNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}")
    @Produces(MediaType.APPLICATION_JSON)
    public PlanVersionBean getPlanVersion(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version)
            throws PlanVersionNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/activity")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultsBean<AuditEntryBean> getPlanVersionActivity(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @QueryParam("page") int page, @QueryParam("count") int pageSize)
            throws PlanVersionNotFoundException, NotAuthorizedException;

    @POST
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean createPlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            PolicyBean bean) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PolicyBean getPlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @PUT
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updatePlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @PathParam("policyId") long policyId, PolicyBean bean) throws OrganizationNotFoundException,
            PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException;

    @DELETE
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies/{policyId}")
    public void deletePlanPolicy(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version,
            @PathParam("policyId") long policyId) throws OrganizationNotFoundException, PlanVersionNotFoundException,
            PolicyNotFoundException, NotAuthorizedException;

    @GET
    @Path("{organizationId}/plans/{planId}/versions/{version}/policies")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PolicySummaryBean> listPlanPolicies(@PathParam("organizationId") String organizationId,
            @PathParam("planId") String planId, @PathParam("version") String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException,
            NotAuthorizedException;

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
     * @param organizationId
     * @param bean
     * @throws OrganizationNotFoundException
     * @throws RoleNotFoundException
     * @throws UserNotFoundException
     * @throws NotAuthorizedException
     */
    @POST
    @Path("{organizationId}/roles")
    @Consumes(MediaType.APPLICATION_JSON)
    public void grant(@PathParam("organizationId") String organizationId, GrantRolesBean bean)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException, NotAuthorizedException;

    /**
     * Revoke membership in a role.
     * @summary Revoke Single Role Membership
     * @param organizationId The organization ID.
     * @param roleId The role ID.
     * @param userId The user ID.
     * @throws OrganizationNotFoundException
     * @throws RoleNotFoundException
     * @throws UserNotFoundException
     * @throws NotAuthorizedException
     */
    @DELETE
    @Path("{organizationId}/roles/{roleId}/{userId}")
    public void revoke(@PathParam("organizationId") String organizationId,
            @PathParam("roleId") String roleId, @PathParam("userId") String userId)
            throws OrganizationNotFoundException, RoleNotFoundException, UserNotFoundException,
            NotAuthorizedException;

    /**
     * Revoke all of a user's role memberships from the org.
     * @summary Revoke All Role Memberships
     * @param organizationId The organization ID.
     * @param userId The user ID.
     * @throws OrganizationNotFoundException
     * @throws RoleNotFoundException
     * @throws UserNotFoundException
     * @throws NotAuthorizedException
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
     * @throws OrganizationNotFoundException
     * @throws NotAuthorizedException
     */
    @GET
    @Path("{organizationId}/members")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MemberBean> listMembers(@PathParam("organizationId") String organizationId)
            throws OrganizationNotFoundException, NotAuthorizedException;
}
