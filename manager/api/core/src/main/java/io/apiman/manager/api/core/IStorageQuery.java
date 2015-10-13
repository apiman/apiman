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
package io.apiman.manager.api.core;

import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.ApplicationVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.beans.summary.ServicePlanSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.util.List;
import java.util.Set;


/**
 * Specific querying of the storage layer.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IStorageQuery {

    /**
     * Lists all of the Plugins.
     * @return list of plugins
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<PluginSummaryBean> listPlugins() throws StorageException;

    /**
     * Lists all of the Gateways.
     * @return list of gateways
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<GatewaySummaryBean> listGateways() throws StorageException;

    /**
     * Finds organizations by the provided criteria.
     * @param criteria search criteria search criteria
     * @return found orgs
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public SearchResultsBean<OrganizationSummaryBean> findOrganizations(SearchCriteriaBean criteria) throws StorageException;
    
    /**
     * Finds applications by the provided criteria.
     * @param criteria search criteria
     * @return found applications
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public SearchResultsBean<ApplicationSummaryBean> findApplications(SearchCriteriaBean criteria) throws StorageException;

    /**
     * Finds services by the provided criteria.
     * @param criteria search criteria
     * @return found services
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public SearchResultsBean<ServiceSummaryBean> findServices(SearchCriteriaBean criteria) throws StorageException;
    
    /**
     * Finds plans (within an organization) with the given criteria.
     * @param organizationId the organization id
     * @param criteria search criteria
     * @return found plans 
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public SearchResultsBean<PlanSummaryBean> findPlans(String organizationId, SearchCriteriaBean criteria) throws StorageException;
    
    /**
     * Gets the audit log for an entity.
     * @param organizationId the organization id
     * @param entityId the entity id
     * @param entityVersion the entity version
     * @param type the type
     * @param paging the paging specification
     * @return audit entity
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public <T> SearchResultsBean<AuditEntryBean> auditEntity(String organizationId, String entityId,
            String entityVersion, Class<T> type, PagingBean paging) throws StorageException;

    /**
     * Gets the audit log for a user.
     * @param userId the user id
     * @param paging the paging specification
     * @return audit user
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public <T> SearchResultsBean<AuditEntryBean> auditUser(String userId, PagingBean paging) throws StorageException;
    
    /**
     * Returns summary info for all organizations in the given set.
     * @param organizationIds the organization ids
     * @return list of orgs
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<OrganizationSummaryBean> getOrgs(Set<String> organizationIds) throws StorageException;

    /**
     * Returns summary info for all applications in all organizations in the given set.
     * @param organizationIds the organization ids
     * @return list of applications in orgs
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<ApplicationSummaryBean> getApplicationsInOrgs(Set<String> organizationIds) throws StorageException;

    /**
     * Returns summary info for all applications in the given organization.
     * @param organizationId the organization id
     * @return list of applications
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<ApplicationSummaryBean> getApplicationsInOrg(String organizationId) throws StorageException;

    /**
     * Returns all application versions for a given app.
     * @param organizationId the organization id
     * @param applicationId the application id
     * @return list of application versions
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public List<ApplicationVersionSummaryBean> getApplicationVersions(String organizationId, String applicationId)
            throws StorageException;

    /**
     * Returns all Contracts for the application.
     * @param organizationId the organization id
     * @param applicationId the application id
     * @param version the version
     * @return list of application contracts
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public List<ContractSummaryBean> getApplicationContracts(String organizationId, String applicationId, String version)
            throws StorageException;


    /**
     * Returns the api registry for the given application.
     * @param organizationId the organization id
     * @param applicationId the application id
     * @param version the version
     * @return the registry bean
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public ApiRegistryBean getApiRegistry(String organizationId, String applicationId, String version)
            throws StorageException;

    /**
     * Returns summary info for all services in all organizations in the given set.
     * @param organizationIds the organization ids
     * @return services in orgs
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<ServiceSummaryBean> getServicesInOrgs(Set<String> organizationIds) throws StorageException;

    /**
     * Returns summary info for all services in the given organization.
     * @param organizationId the organization id
     * @return list of services in org
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<ServiceSummaryBean> getServicesInOrg(String organizationId) throws StorageException;
    
    /**
     * Returns all service versions for a given service.
     * @param organizationId the organization id
     * @param serviceId the service id
     * @return list of service versions
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<ServiceVersionSummaryBean> getServiceVersions(String organizationId, String serviceId) throws StorageException;

    /**
     * Returns the service plans configured for the given service version.
     * @param organizationId the organization id
     * @param serviceId the service id
     * @param version the version
     * @return list of service plans
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws StorageException;

    /**
     * Returns summary info for all plans in all organizations in the given set.
     * @param organizationIds the organization ids
     * @return list of plans in orgs
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> organizationIds) throws StorageException;

    /**
     * Returns summary info for all plans in the given organization.
     * @param organizationId the organization id
     * @return list of plans in org
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<PlanSummaryBean> getPlansInOrg(String organizationId) throws StorageException;

    /**
     * Returns all plan versions for a given plan.
     * @param organizationId the organization id
     * @param planId the plan id
     * @return list of plan versions
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<PlanVersionSummaryBean> getPlanVersions(String organizationId, String planId)
            throws StorageException;

    /**
     * Returns all policies of the given type for the given entity/version.  This could be
     * any of Application, Plan, Service.
     * @param organizationId the organization id
     * @param entityId the entity id
     * @param version the version
     * @param type the type
     * @return list of policies
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public List<PolicySummaryBean> getPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException;

    /**
     * Lists the policy definitions in the system.
     * @return list of policy definitions
     * @throws StorageException if a storage problem occurs while storing a bean.
     */
    public List<PolicyDefinitionSummaryBean> listPolicyDefinitions() throws StorageException;
    
    /**
     * Gets a list of contracts for the given service.  This is paged.
     * @param organizationId the organization id
     * @param serviceId the service id
     * @param version the version
     * @param page the page
     * @param pageSize the paging size
     * @return list of service contracts
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public List<ContractSummaryBean> getServiceContracts(String organizationId,
            String serviceId, String version, int page, int pageSize) throws StorageException;

    /**
     * Returns the largest order index value for the policies assigned to the
     * given entity.
     * @param organizationId the organization id
     * @param entityId the entity id
     * @param entityVersion the entity version
     * @param type the type
     * @return largest order index
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public int getMaxPolicyOrderIndex(String organizationId, String entityId, String entityVersion,
            PolicyType type) throws StorageException;

    /**
     * Lists all of the policy definitions contributed via a particular plugin.
     * @param pluginId the plugin id
     * @return list of plugin policy defs
     * @throws StorageException if a storage problem occurs while storing a bean. 
     */
    public List<PolicyDefinitionSummaryBean> listPluginPolicyDefs(Long pluginId) throws StorageException;

    /**
     * Returns a list of users that match the given search criteria.
     * @param criteria search criteria
     * @return found users
     * @throws StorageException if an exception occurs during storage attempt
     */
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException;

    /**
     * Returns a list of users that match the given search criteria.
     * @param criteria search criteria
     * @return the found roles
     * @throws StorageException if an exception occurs during storage attempt
     */
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException;

    /**
     * Gets all the user's memberships.
     * @param userId the user's id
     * @return set of memberships
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException;

    /**
     * Gets all the user's memberships for the given organization.
     * @param userId the user's id
     * @param organizationId the organization's id
     * @return set of memberships
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId) throws StorageException;

    /**
     * Gets all the memberships configured for a particular organization.
     * @param organizationId the organization's id
     * @return set of memberships
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException;

    /**
     * Returns a set of permissions granted to the user due to their role
     * memberships.
     * @param userId the user's id
     * @return set of permissions
     * @throws StorageException if an exception occurs during storage attempt
     */
    public Set<PermissionBean> getPermissions(String userId) throws StorageException;

}
