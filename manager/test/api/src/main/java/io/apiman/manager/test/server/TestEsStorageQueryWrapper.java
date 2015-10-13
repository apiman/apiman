/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.test.server;

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
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.searchbox.client.JestClient;
import io.searchbox.indices.Refresh;

import java.util.List;
import java.util.Set;

/**
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("javadoc")
public class TestEsStorageQueryWrapper implements IStorageQuery {

    private JestClient esClient;
    private IStorageQuery delegate;

    /**
     * Constructor.
     * @param esClient
     * @param delegate
     */
    public TestEsStorageQueryWrapper(JestClient esClient, IStorageQuery delegate) {
        this.esClient = esClient;
        this.delegate = delegate;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPlugins()
     */
    @Override
    public List<PluginSummaryBean> listPlugins() throws StorageException {
        refresh();
        return this.delegate.listPlugins();
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listGateways()
     */
    @Override
    public List<GatewaySummaryBean> listGateways() throws StorageException {
        refresh();
        return this.delegate.listGateways();
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findOrganizations(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<OrganizationSummaryBean> findOrganizations(SearchCriteriaBean criteria)
            throws StorageException {
        refresh();
        return this.delegate.findOrganizations(criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findApplications(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApplicationSummaryBean> findApplications(SearchCriteriaBean criteria)
            throws StorageException {
        refresh();
        return this.delegate.findApplications(criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findServices(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ServiceSummaryBean> findServices(SearchCriteriaBean criteria)
            throws StorageException {
        refresh();
        return this.delegate.findServices(criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findPlans(java.lang.String, io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanSummaryBean> findPlans(String organizationId, SearchCriteriaBean criteria)
            throws StorageException {
        refresh();
        return this.delegate.findPlans(organizationId, criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.Class, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditEntity(String organizationId, String entityId,
            String entityVersion, Class<T> type, PagingBean paging) throws StorageException {
        refresh();
        return this.delegate.auditEntity(organizationId, entityId, entityVersion, type, paging);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditUser(java.lang.String, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditUser(String userId, PagingBean paging)
            throws StorageException {
        refresh();
        return this.delegate.auditUser(userId, paging);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgs(java.util.Set)
     */
    @Override
    public List<OrganizationSummaryBean> getOrgs(Set<String> orgIds) throws StorageException {
        refresh();
        return this.delegate.getOrgs(orgIds);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationsInOrgs(java.util.Set)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrgs(Set<String> orgIds) throws StorageException {
        refresh();
        return this.delegate.getApplicationsInOrgs(orgIds);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationsInOrg(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrg(String orgId) throws StorageException {
        refresh();
        return this.delegate.getApplicationsInOrg(orgId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ApplicationVersionSummaryBean> getApplicationVersions(String organizationId,
            String applicationId) throws StorageException {
        refresh();
        return this.delegate.getApplicationVersions(organizationId, applicationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> getApplicationContracts(String organizationId, String applicationId,
            String version) throws StorageException {
        refresh();
        return this.delegate.getApplicationContracts(organizationId, applicationId, version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiRegistry(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiRegistryBean getApiRegistry(String organizationId, String applicationId, String version)
            throws StorageException {
        refresh();
        return this.delegate.getApiRegistry(organizationId, applicationId, version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServicesInOrgs(java.util.Set)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrgs(Set<String> orgIds) throws StorageException {
        refresh();
        return this.delegate.getServicesInOrgs(orgIds);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServicesInOrg(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrg(String orgId) throws StorageException {
        refresh();
        return this.delegate.getServicesInOrg(orgId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ServiceVersionSummaryBean> getServiceVersions(String orgId, String serviceId)
            throws StorageException {
        refresh();
        return this.delegate.getServiceVersions(orgId, serviceId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws StorageException {
        refresh();
        return this.delegate.getServiceVersionPlans(organizationId, serviceId, version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrgs(java.util.Set)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> orgIds) throws StorageException {
        refresh();
        return this.delegate.getPlansInOrgs(orgIds);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrg(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrg(String orgId) throws StorageException {
        refresh();
        return this.delegate.getPlansInOrg(orgId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlanVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionSummaryBean> getPlanVersions(String organizationId, String planId)
            throws StorageException {
        refresh();
        return this.delegate.getPlanVersions(organizationId, planId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public List<PolicySummaryBean> getPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        refresh();
        return this.delegate.getPolicies(organizationId, entityId, version, type);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPolicyDefinitions()
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPolicyDefinitions() throws StorageException {
        refresh();
        return this.delegate.listPolicyDefinitions();
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceContracts(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public List<ContractSummaryBean> getServiceContracts(String organizationId, String serviceId,
            String version, int page, int pageSize) throws StorageException {
        refresh();
        return this.delegate.getServiceContracts(organizationId, serviceId, version, page, pageSize);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getMaxPolicyOrderIndex(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public int getMaxPolicyOrderIndex(String organizationId, String entityId, String entityVersion,
            PolicyType type) throws StorageException {
        refresh();
        return this.delegate.getMaxPolicyOrderIndex(organizationId, entityId, entityVersion, type);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPluginPolicyDefs(java.lang.Long)
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPluginPolicyDefs(Long pluginId) throws StorageException {
        refresh();
        return this.delegate.listPluginPolicyDefs(pluginId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        refresh();
        return this.delegate.findUsers(criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findRoles(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException {
        refresh();
        return this.delegate.findRoles(criteria);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getUserMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        refresh();
        return this.delegate.getUserMemberships(userId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getUserMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId)
            throws StorageException {
        refresh();
        return this.delegate.getUserMemberships(userId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        refresh();
        return this.delegate.getOrgMemberships(organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPermissions(java.lang.String)
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        refresh();
        return this.delegate.getPermissions(userId);
    }

    /**
     * Force a refresh in elasticsearch so that the result of any indexing operations
     * up to this point will be visible to searches.
     */
    private void refresh() {
        try {
        	esClient.execute(new Refresh.Builder().refresh(true).addIndex("apiman_manager").build()); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
