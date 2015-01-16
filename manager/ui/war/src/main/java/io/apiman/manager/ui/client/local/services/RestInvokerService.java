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
package io.apiman.manager.ui.client.local.services;

import io.apiman.manager.api.beans.actions.ActionBean;
import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.CurrentUserBean;
import io.apiman.manager.api.beans.idm.GrantRolesBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.members.MemberBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.ApplicationVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.GatewayTestResultBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.beans.summary.ServicePlanSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.api.rest.contract.IActionResource;
import io.apiman.manager.api.rest.contract.ICurrentUserResource;
import io.apiman.manager.api.rest.contract.IGatewayResource;
import io.apiman.manager.api.rest.contract.IOrganizationResource;
import io.apiman.manager.api.rest.contract.IPolicyDefinitionResource;
import io.apiman.manager.api.rest.contract.IRoleResource;
import io.apiman.manager.api.rest.contract.ISearchResource;
import io.apiman.manager.api.rest.contract.ISystemResource;
import io.apiman.manager.api.rest.contract.IUserResource;
import io.apiman.manager.ui.client.local.services.rest.CallbackAdapter;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;


/**
 * Used to invoke the APIMan DT REST interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class RestInvokerService {
    
    @Inject
    private Caller<ISystemResource> system;
    @Inject
    private Caller<ISearchResource> search;
    @Inject
    private Caller<ICurrentUserResource> currentUser;
    @Inject
    private Caller<IRoleResource> roles;
    @Inject
    private Caller<IUserResource> users;
    @Inject
    private Caller<IPolicyDefinitionResource> policyDefs;
    @Inject
    private Caller<IOrganizationResource> organizations;
    @Inject
    private Caller<IActionResource> actions;
    @Inject
    private Caller<IGatewayResource> gateways;
    
    /**
     * Constructor.
     */
    public RestInvokerService() {
    }

    /**
     * Gets the current system status.
     * @param callback
     */
    public void getSystemStatus(IRestInvokerCallback<String> callback) {
        CallbackAdapter<String> adapter = new CallbackAdapter<String>(callback);
        system.call(adapter, adapter).getStatus();
    }

    /**
     * Creates a role.
     * @param roleId
     * @param callback
     */
    public void createRole(RoleBean role, IRestInvokerCallback<RoleBean> callback) {
        CallbackAdapter<RoleBean> adapter = new CallbackAdapter<RoleBean>(callback);
        roles.call(adapter, adapter).create(role);
    }

    /**
     * Gets all roles that can be assigned to users.
     * @param callback
     */
    public void getRoles(IRestInvokerCallback<List<RoleBean>> callback) {
        CallbackAdapter<List<RoleBean>> adapter = new CallbackAdapter<List<RoleBean>>(callback);
        roles.call(adapter, adapter).list();
    }
    
    /**
     * Gets a single role by ID.
     * @param roleId
     * @param callback
     */
    public void getRole(String roleId, IRestInvokerCallback<RoleBean> callback) {
        CallbackAdapter<RoleBean> adapter = new CallbackAdapter<RoleBean>(callback);
        roles.call(adapter, adapter).get(roleId);
    }
    
    /**
     * Updates a role.
     * @param roleId
     * @param callback
     */
    public void updateRole(RoleBean role, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        roles.call(adapter, adapter).update(role.getId(), role);
    }

    /**
     * Deletes a role.  Use with caution!
     * @param roleId
     * @param callback
     */
    public void deleteRole(RoleBean role, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        roles.call(adapter, adapter).delete(role.getId());
    }

    /**
     * Gets info about the given user.
     * @param callback
     */
    public void getUser(String userId, IRestInvokerCallback<UserBean> callback) {
        CallbackAdapter<UserBean> adapter = new CallbackAdapter<UserBean>(callback);
        users.call(adapter, adapter).get(userId);
    }
    
    /**
     * Finds users using the given search criteria.
     * @param criteria
     * @param callback
     */
    public void findUsers(SearchCriteriaBean criteria, IRestInvokerCallback<SearchResultsBean<UserBean>> callback) {
        CallbackAdapter<SearchResultsBean<UserBean>> adapter = new CallbackAdapter<SearchResultsBean<UserBean>>(callback);
        users.call(adapter, adapter).search(criteria);
    }

    /**
     * Gets the organizations visible to the given user.
     * @param callback
     */
    public void getUserOrgs(String userId, IRestInvokerCallback<List<OrganizationSummaryBean>> callback) {
        CallbackAdapter<List<OrganizationSummaryBean>> adapter = new CallbackAdapter<List<OrganizationSummaryBean>>(callback);
        users.call(adapter, adapter).getOrganizations(userId);
    }

    /**
     * Gets the applications visible to the given user.
     * @param callback
     */
    public void getUserApps(String userId, IRestInvokerCallback<List<ApplicationSummaryBean>> callback) {
        CallbackAdapter<List<ApplicationSummaryBean>> adapter = new CallbackAdapter<List<ApplicationSummaryBean>>(callback);
        users.call(adapter, adapter).getApplications(userId);
    }

    /**
     * Gets the services visible to the given user.
     * @param callback
     */
    public void getUserServices(String userId, IRestInvokerCallback<List<ServiceSummaryBean>> callback) {
        CallbackAdapter<List<ServiceSummaryBean>> adapter = new CallbackAdapter<List<ServiceSummaryBean>>(callback);
        users.call(adapter, adapter).getServices(userId);
    }

    /**
     * Gets the user's recent activity.
     * @param userId
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getUserActivity(String userId, int page, int pageSize, IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        users.call(adapter, adapter).getActivity(userId, page, pageSize);
    }

    /**
     * Gets info about the current user.
     * @param callback
     */
    public void getCurrentUserInfo(IRestInvokerCallback<CurrentUserBean> callback) {
        CallbackAdapter<CurrentUserBean> adapter = new CallbackAdapter<CurrentUserBean>(callback);
        currentUser.call(adapter, adapter).getInfo();
    }
    
    /**
     * Updates the information for the current user.
     * @param user
     * @param callback
     */
    public void updateCurrentUserInfo(UserBean user, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        currentUser.call(adapter, adapter).updateInfo(user);
    }

    /**
     * Gets the organizations visible to the current user for app creation.
     * @param callback
     */
    public void getCurrentUserAppOrgs(IRestInvokerCallback<List<OrganizationSummaryBean>> callback) {
        CallbackAdapter<List<OrganizationSummaryBean>> adapter = new CallbackAdapter<List<OrganizationSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getAppOrganizations();
    }

    /**
     * Gets the organizations visible to the current user for service creation.
     * @param callback
     */
    public void getCurrentUserServiceOrgs(IRestInvokerCallback<List<OrganizationSummaryBean>> callback) {
        CallbackAdapter<List<OrganizationSummaryBean>> adapter = new CallbackAdapter<List<OrganizationSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getServiceOrganizations();
    }

    /**
     * Gets the organizations visible to the current user for plan creation.
     * @param callback
     */
    public void getCurrentUserPlanOrgs(IRestInvokerCallback<List<OrganizationSummaryBean>> callback) {
        CallbackAdapter<List<OrganizationSummaryBean>> adapter = new CallbackAdapter<List<OrganizationSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getPlanOrganizations();
    }

    /**
     * Gets all applications visible to the current user.
     * @param callback
     */
    public void getCurrentUserApps(IRestInvokerCallback<List<ApplicationSummaryBean>> callback) {
        CallbackAdapter<List<ApplicationSummaryBean>> adapter = new CallbackAdapter<List<ApplicationSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getApplications();
    }

    /**
     * Gets all services visible to the current user.
     * @param callback
     */
    public void getCurrentUserServices(IRestInvokerCallback<List<ServiceSummaryBean>> callback) {
        CallbackAdapter<List<ServiceSummaryBean>> adapter = new CallbackAdapter<List<ServiceSummaryBean>>(callback);
        currentUser.call(adapter, adapter).getServices();
    }

    /**
     * Gets an organization by ID.
     * @param orgId
     * @param callback
     */
    public void getOrganization(String orgId, IRestInvokerCallback<OrganizationBean> callback) {
        CallbackAdapter<OrganizationBean> adapter = new CallbackAdapter<OrganizationBean>(callback);
        organizations.call(adapter, adapter).get(orgId);
    }
    
    /**
     * Creates a new organization.
     * @param org
     * @param callback
     */
    public void createOrganization(OrganizationBean org, IRestInvokerCallback<OrganizationBean> callback) {
        CallbackAdapter<OrganizationBean> adapter = new CallbackAdapter<OrganizationBean>(callback);
        organizations.call(adapter, adapter).create(org);
    }
    
    /**
     * Updates an organization.
     * @param org
     * @param callback
     */
    public void updateOrganization(OrganizationBean org, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).update(org.getId(), org);
    }

    /**
     * Finds organizations using the given search criteria.
     * @param criteria
     * @param callback
     */
    public void findOrganizations(SearchCriteriaBean criteria, IRestInvokerCallback<SearchResultsBean<OrganizationSummaryBean>> callback) {
        CallbackAdapter<SearchResultsBean<OrganizationSummaryBean>> adapter = new CallbackAdapter<SearchResultsBean<OrganizationSummaryBean>>(callback);
        search.call(adapter, adapter).searchOrgs(criteria);
    }

    /**
     * Creates a new application.
     * @param organizationId
     * @param app
     * @param callback
     */
    public void createApplication(String organizationId, ApplicationBean app, IRestInvokerCallback<ApplicationBean> callback) {
        CallbackAdapter<ApplicationBean> adapter = new CallbackAdapter<ApplicationBean>(callback);
        organizations.call(adapter, adapter).createApp(organizationId, app);
    }
    
    /**
     * Updates an application.
     * @param app
     * @param callback
     */
    public void updateApplication(ApplicationBean app, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).updateApp(app.getOrganization().getId(), app.getId(), app);
    }
    
    /**
     * Creates a new version of an app.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param callback
     */
    public void createApplicationVersion(String organizationId, String applicationId, ApplicationVersionBean version,
            IRestInvokerCallback<ApplicationVersionBean> callback) {
        CallbackAdapter<ApplicationVersionBean> adapter = new CallbackAdapter<ApplicationVersionBean>(callback);
        organizations.call(adapter, adapter).createAppVersion(organizationId, applicationId, version);
    }
    
    /**
     * Creates a contract for an application.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param bean
     * @param callback
     */
    public void createContract(String organizationId, String applicationId, String version,
            NewContractBean bean, IRestInvokerCallback<ContractBean> callback) {
        CallbackAdapter<ContractBean> adapter = new CallbackAdapter<ContractBean>(callback);
        organizations.call(adapter, adapter).createContract(organizationId, applicationId, version, bean);
    }
    
    /**
     * Deletes a contract.  This is synonymous with "breaking" a contract.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param contractId
     * @param callback
     */
    public void deleteContract(String organizationId, String applicationId, String version,
            Long contractId, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).deleteContract(organizationId, applicationId, version, contractId);
    }

    /**
     * Gets an application.
     * @param organizationId
     * @param applicationId
     * @param callback
     */
    public void getApplication(String organizationId, String applicationId, IRestInvokerCallback<ApplicationBean> callback) {
        CallbackAdapter<ApplicationBean> adapter = new CallbackAdapter<ApplicationBean>(callback);
        organizations.call(adapter, adapter).getApp(organizationId, applicationId);
    }

    /**
     * Gets all versions of the application.
     * @param organizationId
     * @param applicationId
     * @param callback
     */
    public void getApplicationVersions(String organizationId, String applicationId, 
            IRestInvokerCallback<List<ApplicationVersionSummaryBean>> callback) {
        CallbackAdapter<List<ApplicationVersionSummaryBean>> adapter = new CallbackAdapter<List<ApplicationVersionSummaryBean>>(callback);
        organizations.call(adapter, adapter).listAppVersions(organizationId, applicationId);
    }

    /**
     * Get a single version of the application.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param callback
     */
    public void getApplicationVersion(String organizationId, String applicationId, String version, 
            IRestInvokerCallback<ApplicationVersionBean> callback) {
        CallbackAdapter<ApplicationVersionBean> adapter = new CallbackAdapter<ApplicationVersionBean>(callback);
        organizations.call(adapter, adapter).getAppVersion(organizationId, applicationId, version);
    }

    /**
     * Gets the application's contracts.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param callback
     */
    public void getApplicationContracts(String organizationId, String applicationId, String version, 
            IRestInvokerCallback<List<ContractSummaryBean>> callback) {
        CallbackAdapter<List<ContractSummaryBean>> adapter = new CallbackAdapter<List<ContractSummaryBean>>(callback);
        organizations.call(adapter, adapter).getApplicationVersionContracts(organizationId, applicationId, version);
    }
    
    /**
     * Gets the application's API registry.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param callback
     */
    public void getApiRegistry(String organizationId, String applicationId, String version, 
            IRestInvokerCallback<ApiRegistryBean> callback) {
        CallbackAdapter<ApiRegistryBean> adapter = new CallbackAdapter<ApiRegistryBean>(callback);
        organizations.call(adapter, adapter).getApiRegistry(organizationId, applicationId, version);
    }

    /**
     * Gets the application's policies.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param callback
     */
    public void getApplicationPolicies(String organizationId, String applicationId, String version, 
            IRestInvokerCallback<List<PolicySummaryBean>> callback) {
        CallbackAdapter<List<PolicySummaryBean>> adapter = new CallbackAdapter<List<PolicySummaryBean>>(callback);
        organizations.call(adapter, adapter).listAppPolicies(organizationId, applicationId, version);
    }

    /**
     * Reorders the application's policies.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param policyChain
     * @param callback
     */
    public void reorderApplicationPolicies(String organizationId, String applicationId, String version, 
            PolicyChainBean policyChain, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).reorderApplicationPolicies(organizationId, applicationId,
                version, policyChain);
    }
    
    /**
     * Gets the application activity.
     * @param organizationId
     * @param applicationId
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getApplicationActivity(String organizationId, String applicationId, int page, int pageSize,
            IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        organizations.call(adapter, adapter).getAppActivity(organizationId, applicationId, page, pageSize);
    }
    
    /**
     * Gets the application activity.
     * @param organizationId
     * @param applicationId
     * @param version
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getApplicationVersionActivity(String organizationId, String applicationId, String version, int page, int pageSize,
            IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        organizations.call(adapter, adapter).getAppVersionActivity(organizationId, applicationId, version, page, pageSize);
    }

    /**
     * Gets the activity for the organization.
     * @param organizationId
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getOrgActivity(String organizationId, int page, int pageSize,
            IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        organizations.call(adapter, adapter).activity(organizationId, page, pageSize);
    }
    
    /**
     * Gets all applications in the organization.
     * @param organizationId
     * @param applicationId
     * @param callback
     */
    public void getOrgApplications(String organizationId, IRestInvokerCallback<List<ApplicationSummaryBean>> callback) {
        CallbackAdapter<List<ApplicationSummaryBean>> adapter = new CallbackAdapter<List<ApplicationSummaryBean>>(callback);
        organizations.call(adapter, adapter).listApps(organizationId);
    }
    
    /**
     * Creates a new service.
     * @param organizationId
     * @param service
     * @param callback
     */
    public void createService(String organizationId, ServiceBean service, IRestInvokerCallback<ServiceBean> callback) {
        CallbackAdapter<ServiceBean> adapter = new CallbackAdapter<ServiceBean>(callback);
        organizations.call(adapter, adapter).createService(organizationId, service);
    }

    /**
     * Updates a service.
     * @param service
     * @param callback
     */
    public void updateService(ServiceBean service, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).updateService(service.getOrganization().getId(), service.getId(), service);
    }

    /**
     * Creates a new version of an service.
     * @param organizationId
     * @param serviceId
     * @param version
     * @param callback
     */
    public void createServiceVersion(String organizationId, String serviceId, ServiceVersionBean version,
            IRestInvokerCallback<ServiceVersionBean> callback) {
        CallbackAdapter<ServiceVersionBean> adapter = new CallbackAdapter<ServiceVersionBean>(callback);
        organizations.call(adapter, adapter).createServiceVersion(organizationId, serviceId, version);
    }

    /**
     * Gets an service.
     * @param organizationId
     * @param serviceId
     * @param callback
     */
    public void getService(String organizationId, String serviceId, IRestInvokerCallback<ServiceBean> callback) {
        CallbackAdapter<ServiceBean> adapter = new CallbackAdapter<ServiceBean>(callback);
        organizations.call(adapter, adapter).getService(organizationId, serviceId);
    }

    /**
     * Gets a single version of a service.
     * @param organizationId
     * @param serviceId
     * @param version
     * @param callback
     */
    public void getServiceVersion(String organizationId, String serviceId, String version,
            IRestInvokerCallback<ServiceVersionBean> callback) {
        CallbackAdapter<ServiceVersionBean> adapter = new CallbackAdapter<ServiceVersionBean>(callback);
        organizations.call(adapter, adapter).getServiceVersion(organizationId, serviceId, version);
    }

    /**
     * Gets the service version endpoint information.  This only works if the service has
     * been published!
     * @param organizationId
     * @param serviceId
     * @param version
     * @param callback
     */
    public void getServiceVersionEndpointInfo(String organizationId, String serviceId, String version,
            IRestInvokerCallback<ServiceVersionEndpointSummaryBean> callback) {
        CallbackAdapter<ServiceVersionEndpointSummaryBean> adapter = new CallbackAdapter<ServiceVersionEndpointSummaryBean>(callback);
        organizations.call(adapter, adapter).getServiceVersionEndpointInfo(organizationId, serviceId, version);
    }

    /**
     * Gets all versions of the service.
     * @param organizationId
     * @param serviceId
     * @param callback
     */
    public void getServiceVersions(String organizationId, String serviceId,
            IRestInvokerCallback<List<ServiceVersionSummaryBean>> callback) {
        CallbackAdapter<List<ServiceVersionSummaryBean>> adapter = new CallbackAdapter<List<ServiceVersionSummaryBean>>(callback);
        organizations.call(adapter, adapter).listServiceVersions(organizationId, serviceId);
    }

    /**
     * Gets the plans for a service version.
     * @param organizationId
     * @param serviceId
     * @param version
     * @param callback
     */
    public void getServiceVersionPlans(String organizationId, String serviceId, String version,
            IRestInvokerCallback<List<ServicePlanSummaryBean>> callback) {
        CallbackAdapter<List<ServicePlanSummaryBean>> adapter = new CallbackAdapter<List<ServicePlanSummaryBean>>(callback);
        organizations.call(adapter, adapter).getServiceVersionPlans(organizationId, serviceId, version);
    }
    
    /**
     * Updates a service version.
     * @param organizationId
     * @param serviceId
     * @param version
     * @param svb
     * @param callback
     */
    public void updateServiceVersion(String organizationId, String serviceId, String version,
            ServiceVersionBean svb, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).updateServiceVersion(organizationId, serviceId, version, svb);
    }

    /**
     * Gets the service's policies.
     * @param callback
     */
    public void getServicePolicies(String organizationId, String serviceId, String version, 
            IRestInvokerCallback<List<PolicySummaryBean>> callback) {
        CallbackAdapter<List<PolicySummaryBean>> adapter = new CallbackAdapter<List<PolicySummaryBean>>(callback);
        organizations.call(adapter, adapter).listServicePolicies(organizationId, serviceId, version);
    }

    /**
     * Reorders the service's policies.
     * @param callback
     */
    public void reorderServicePolicies(String organizationId, String serviceId, String version, 
            PolicyChainBean policyChain, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).reorderServicePolicies(organizationId, serviceId, version, policyChain);
    }
    
    /**
     * Gets the activity information for the service.
     * @param organizationId
     * @param serviceId
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getServiceActivity(String organizationId, String serviceId, int page, int pageSize,
            IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        organizations.call(adapter, adapter).getServiceActivity(organizationId, serviceId, page, pageSize);
    }

    /**
     * Gets the service's contracts.
     * @param organizationId
     * @param serviceId
     * @param version
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getServiceContracts(String organizationId, String serviceId, String version, int page, int pageSize, 
            IRestInvokerCallback<List<ContractSummaryBean>> callback) {
        CallbackAdapter<List<ContractSummaryBean>> adapter = new CallbackAdapter<List<ContractSummaryBean>>(callback);
        organizations.call(adapter, adapter).getServiceVersionContracts(organizationId, serviceId, version, page, pageSize);
    }

    /**
     * Gets the activity information for the service version.
     * @param organizationId
     * @param serviceId
     * @param version
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getServiceVersionActivity(String organizationId, String serviceId, String version, int page, int pageSize,
            IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        organizations.call(adapter, adapter).getServiceVersionActivity(organizationId, serviceId, version, page, pageSize);
    }
    
    /**
     * Gets the policy chain for a particular service + version + plan.  This method
     * can be used to answer the question:  "What policies will be applied if Service X
     * is invoked via Plan Y?"
     * @param organizationId
     * @param serviceId
     * @param version
     * @param planId
     */
    public void getServicePlanPolicyChain(String organizationId, String serviceId, String version,
            String planId, IRestInvokerCallback<PolicyChainBean> callback) {
        CallbackAdapter<PolicyChainBean> adapter = new CallbackAdapter<PolicyChainBean>(callback);
        organizations.call(adapter, adapter).getServicePolicyChain(organizationId, serviceId, version, planId);
    }

    /**
     * Gets all services in the organization.
     * @param organizationId
     * @param serviceId
     * @param callback
     */
    public void getOrgServices(String organizationId, IRestInvokerCallback<List<ServiceSummaryBean>> callback) {
        CallbackAdapter<List<ServiceSummaryBean>> adapter = new CallbackAdapter<List<ServiceSummaryBean>>(callback);
        organizations.call(adapter, adapter).listServices(organizationId);
    }

    /**
     * Gets all members of an org.
     * @param organizationId
     * @param callback
     */
    public void getOrgMembers(String organizationId, IRestInvokerCallback<List<MemberBean>> callback) {
        CallbackAdapter<List<MemberBean>> adapter = new CallbackAdapter<List<MemberBean>>(callback);
        organizations.call(adapter, adapter).listMembers(organizationId);
    }
    
    /**
     * Grants a role to a user.
     * @param organizationId
     * @param userId
     * @param roleIds
     * @param callback
     */
    public void grant(String organizationId, String userId, Set<String> roleIds, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        GrantRolesBean bean = new GrantRolesBean();
        bean.setUserId(userId);
        bean.setRoleIds(roleIds);
        organizations.call(adapter, adapter).grant(organizationId, bean);
    }

    /**
     * Revokes a role from the user.
     * @param organizationId
     * @param userId
     * @param roleId
     * @param callback
     */
    public void revoke(String organizationId, String userId, String roleId, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).revoke(organizationId, roleId, userId);
    }

    /**
     * Revokes a role from the user.
     * @param organizationId
     * @param userId
     * @param callback
     */
    public void revokeAll(String organizationId, String userId, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).revokeAll(organizationId, userId);
    }

    /**
     * Gets all plans in the organization.
     * @param organizationId
     * @param callback
     */
    public void getOrgPlans(String organizationId, IRestInvokerCallback<List<PlanSummaryBean>> callback) {
        CallbackAdapter<List<PlanSummaryBean>> adapter = new CallbackAdapter<List<PlanSummaryBean>>(callback);
        organizations.call(adapter, adapter).listPlans(organizationId);
    }

    /**
     * Gets a single version of a plan.
     * @param organizationId
     * @param planId
     * @param version
     * @param callback
     */
    public void getPlanVersion(String organizationId, String planId, String version,
            IRestInvokerCallback<PlanVersionBean> callback) {
        CallbackAdapter<PlanVersionBean> adapter = new CallbackAdapter<PlanVersionBean>(callback);
        organizations.call(adapter, adapter).getPlanVersion(organizationId, planId, version);
    }

    /**
     * Gets all versions of the plan.
     * @param organizationId
     * @param planId
     * @param callback
     */
    public void getPlanVersions(String organizationId, String planId, 
            IRestInvokerCallback<List<PlanVersionSummaryBean>> callback) {
        CallbackAdapter<List<PlanVersionSummaryBean>> adapter = new CallbackAdapter<List<PlanVersionSummaryBean>>(callback);
        organizations.call(adapter, adapter).listPlanVersions(organizationId, planId);
    }
    
    /**
     * Gets plan activity information.
     * @param organizationId
     * @param planId
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getPlanActivity(String organizationId, String planId, int page, int pageSize,
            IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        organizations.call(adapter, adapter).getPlanActivity(organizationId, planId, page, pageSize);
    }

    /**
     * Gets plan version activity information.
     * @param organizationId
     * @param planId
     * @param version
     * @param page
     * @param pageSize
     * @param callback
     */
    public void getPlanVersionActivity(String organizationId, String planId, String version, int page, int pageSize,
            IRestInvokerCallback<SearchResultsBean<AuditEntryBean>> callback) {
        CallbackAdapter<SearchResultsBean<AuditEntryBean>> adapter = new CallbackAdapter<SearchResultsBean<AuditEntryBean>>(callback);
        organizations.call(adapter, adapter).getPlanVersionActivity(organizationId, planId, version, page, pageSize);
    }

    /**
     * Gets the plan's policies.
     * @param callback
     */
    public void getPlanPolicies(String organizationId, String planId, String version, 
            IRestInvokerCallback<List<PolicySummaryBean>> callback) {
        CallbackAdapter<List<PolicySummaryBean>> adapter = new CallbackAdapter<List<PolicySummaryBean>>(callback);
        organizations.call(adapter, adapter).listPlanPolicies(organizationId, planId, version);
    }

    /**
     * Reorders the plan's policies.
     * @param callback
     */
    public void reorderPlanPolicies(String organizationId, String planId, String version, 
            PolicyChainBean policyChain, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).reorderPlanPolicies(organizationId, planId, version, policyChain);
    }
    
    /**
     * Creates a new plan.
     * @param organizationId
     * @param plan
     * @param callback
     */
    public void createPlan(String organizationId, PlanBean plan, IRestInvokerCallback<PlanBean> callback) {
        CallbackAdapter<PlanBean> adapter = new CallbackAdapter<PlanBean>(callback);
        organizations.call(adapter, adapter).createPlan(organizationId, plan);
    }

    /**
     * Updates a plan.
     * @param organizationId
     * @param plan
     * @param callback
     */
    public void updatePlan(PlanBean plan, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        organizations.call(adapter, adapter).updatePlan(plan.getOrganization().getId(), plan.getId(), plan);
    }

    /**
     * Creates a new version of an plan.
     * @param organizationId
     * @param planId
     * @param version
     * @param callback
     */
    public void createPlanVersion(String organizationId, String planId, PlanVersionBean version,
            IRestInvokerCallback<PlanVersionBean> callback) {
        CallbackAdapter<PlanVersionBean> adapter = new CallbackAdapter<PlanVersionBean>(callback);
        organizations.call(adapter, adapter).createPlanVersion(organizationId, planId, version);
    }
    
    /**
     * Finds applications using the given search criteria.
     * @param criteria
     * @param callback
     */
    public void findApplications(SearchCriteriaBean criteria, IRestInvokerCallback<SearchResultsBean<ApplicationBean>> callback) {
        CallbackAdapter<SearchResultsBean<ApplicationBean>> adapter = new CallbackAdapter<SearchResultsBean<ApplicationBean>>(callback);
        search.call(adapter, adapter).searchApps(criteria);
    }
    
    /**
     * Finds services using the given search criteria.
     * @param criteria
     * @param callback
     */
    public void findServices(SearchCriteriaBean criteria, IRestInvokerCallback<SearchResultsBean<ServiceSummaryBean>> callback) {
        CallbackAdapter<SearchResultsBean<ServiceSummaryBean>> adapter = new CallbackAdapter<SearchResultsBean<ServiceSummaryBean>>(callback);
        search.call(adapter, adapter).searchServices(criteria);
    }
    
    /**
     * Creates a policy for an application, service, or plan.
     * @param policyType
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param bean
     */
    public void createPolicy(PolicyType policyType, String organizationId, String entityId, String entityVersion,
            PolicyBean bean, IRestInvokerCallback<PolicyBean> callback) {
        CallbackAdapter<PolicyBean> adapter = new CallbackAdapter<PolicyBean>(callback);
        if (policyType == PolicyType.Application) {
            organizations.call(adapter, adapter).createAppPolicy(organizationId, entityId, entityVersion, bean);
        } else if (policyType == PolicyType.Service) {
            organizations.call(adapter, adapter).createServicePolicy(organizationId, entityId, entityVersion, bean);
        } else if (policyType == PolicyType.Plan) {
            organizations.call(adapter, adapter).createPlanPolicy(organizationId, entityId, entityVersion, bean);
        }
    }
    
    /**
     * Removes a policy from an application, service, or plan.
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param policyId
     * @param callback
     */
    public void removePolicy(PolicyType policyType, String organizationId, String entityId, String entityVersion,
            Long policyId, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        if (policyType == PolicyType.Application) {
            organizations.call(adapter, adapter).deleteAppPolicy(organizationId, entityId, entityVersion, policyId);
        } else if (policyType == PolicyType.Service) {
            organizations.call(adapter, adapter).deleteServicePolicy(organizationId, entityId, entityVersion, policyId);
        } else if (policyType == PolicyType.Plan) {
            organizations.call(adapter, adapter).deletePlanPolicy(organizationId, entityId, entityVersion, policyId);
        }
    }

    /**
     * Gets a policy for an application, service, or plan.
     * @param policyType
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param policyId
     * @param callback
     */
    public void getPolicy(PolicyType policyType, String organizationId, String entityId,
            String entityVersion, Long policyId, IRestInvokerCallback<PolicyBean> callback) {
        CallbackAdapter<PolicyBean> adapter = new CallbackAdapter<PolicyBean>(callback);
        if (policyType == PolicyType.Application) {
            organizations.call(adapter, adapter).getAppPolicy(organizationId, entityId, entityVersion, policyId);
        } else if (policyType == PolicyType.Service) {
            organizations.call(adapter, adapter).getServicePolicy(organizationId, entityId, entityVersion, policyId);
        } else if (policyType == PolicyType.Plan) {
            organizations.call(adapter, adapter).getPlanPolicy(organizationId, entityId, entityVersion, policyId);
        }
    }

    /**
     * Updates a policy.  Works for all three types: app, service, plan.
     * @param policyType
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param policyId
     * @param policy
     * @param callback
     */
    public void updatePolicy(PolicyType policyType, String organizationId, String entityId,
            String entityVersion, Long policyId, PolicyBean policy, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        if (policyType == PolicyType.Application) {
            organizations.call(adapter, adapter).updateAppPolicy(organizationId, entityId, entityVersion, policyId, policy);
        } else if (policyType == PolicyType.Service) {
            organizations.call(adapter, adapter).updateServicePolicy(organizationId, entityId, entityVersion, policyId, policy);
        } else if (policyType == PolicyType.Plan) {
            organizations.call(adapter, adapter).updatePlanPolicy(organizationId, entityId, entityVersion, policyId, policy);
        }
    }

    /**
     * Performs/executes the given action.
     * @param action
     * @param callback
     */
    public void performAction(ActionBean action, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        actions.call(adapter, adapter).performAction(action);
    }

    /**
     * Gets a list of all the policy definitions in the system.
     * @param callback
     */
    public void listPolicyDefinitions(IRestInvokerCallback<List<PolicyDefinitionSummaryBean>> callback) {
        CallbackAdapter<List<PolicyDefinitionSummaryBean>> adapter = new CallbackAdapter<List<PolicyDefinitionSummaryBean>>(callback);
        policyDefs.call(adapter, adapter).list();
    }
    
    /**
     * Creates a policy definition.
     * @param policyDef
     * @param callback
     */
    public void createPolicyDefinition(PolicyDefinitionBean policyDef, IRestInvokerCallback<PolicyDefinitionBean> callback) {
        CallbackAdapter<PolicyDefinitionBean> adapter = new CallbackAdapter<PolicyDefinitionBean>(callback);
        policyDefs.call(adapter, adapter).create(policyDef);
    }

    /**
     * Tests the gateway configuration to see if it's valid (is the gateway available?).
     * @param bean
     * @param callback
     */
    public void testGateway(GatewayBean bean, IRestInvokerCallback<GatewayTestResultBean> callback) {
        CallbackAdapter<GatewayTestResultBean> adapter = new CallbackAdapter<GatewayTestResultBean>(callback);
        gateways.call(adapter, adapter).test(bean);
    }
    
    /**
     * Gets a list of all the gateways in the system.
     * @param callback
     */
    public void listGateways(IRestInvokerCallback<List<GatewaySummaryBean>> callback) {
        CallbackAdapter<List<GatewaySummaryBean>> adapter = new CallbackAdapter<List<GatewaySummaryBean>>(callback);
        gateways.call(adapter, adapter).list();
    }
    
    /**
     * Creates a policy definition.
     * @param gateway
     * @param callback
     */
    public void createGateway(GatewayBean gateway, IRestInvokerCallback<GatewayBean> callback) {
        CallbackAdapter<GatewayBean> adapter = new CallbackAdapter<GatewayBean>(callback);
        gateways.call(adapter, adapter).create(gateway);
    }

    /**
     * Gets a single gateway by ID.
     * @param gatewayId
     * @param callback
     */
    public void getGateway(String gatewayId, IRestInvokerCallback<GatewayBean> callback) {
        CallbackAdapter<GatewayBean> adapter = new CallbackAdapter<GatewayBean>(callback);
        gateways.call(adapter, adapter).get(gatewayId);
    }
    
    /**
     * Updates a gateway.
     * @param gatewayId
     * @param callback
     */
    public void updateGateway(GatewayBean gateway, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        gateways.call(adapter, adapter).update(gateway.getId(), gateway);
    }

    /**
     * Deletes a gateway.  Use with caution!
     * @param gatewayId
     * @param callback
     */
    public void deleteGateway(GatewayBean gateway, IRestInvokerCallback<Void> callback) {
        CallbackAdapter<Void> adapter = new CallbackAdapter<Void>(callback);
        gateways.call(adapter, adapter).delete(gateway.getId());
    }

}
