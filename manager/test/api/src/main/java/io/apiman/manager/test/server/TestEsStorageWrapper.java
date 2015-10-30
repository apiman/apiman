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

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.searchbox.client.JestClient;
import io.searchbox.indices.Refresh;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Wraps the ES storage impl so that it can "refresh" the indexes
 * when appropriate.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("javadoc")
public class TestEsStorageWrapper implements IStorage {

    private JestClient esClient;
    private IStorage delegate;

    /**
     * Constructor.
     * @param esClient
     * @param delegate
     */
    public TestEsStorageWrapper(JestClient esClient, IStorage delegate) {
        this.esClient = esClient;
        this.delegate = delegate;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#beginTx()
     */
    @Override
    public void beginTx() throws StorageException {
        this.delegate.beginTx();
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#commitTx()
     */
    @Override
    public void commitTx() throws StorageException {
        this.delegate.commitTx();
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#rollbackTx()
     */
    @Override
    public void rollbackTx() {
        this.delegate.rollbackTx();
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void createOrganization(OrganizationBean organization) throws StorageException {
        this.delegate.createOrganization(organization);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void createApplication(ApplicationBean application) throws StorageException {
        this.delegate.createApplication(application);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void createApplicationVersion(ApplicationVersionBean version) throws StorageException {
        this.delegate.createApplicationVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void createContract(ContractBean contract) throws StorageException {
        this.delegate.createContract(contract);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void createService(ServiceBean service) throws StorageException {
        this.delegate.createService(service);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void createServiceVersion(ServiceVersionBean version) throws StorageException {
        this.delegate.createServiceVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void createPlan(PlanBean plan) throws StorageException {
        this.delegate.createPlan(plan);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException {
        this.delegate.createPlanVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void createPolicy(PolicyBean policy) throws StorageException {
        this.delegate.createPolicy(policy);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void createGateway(GatewayBean gateway) throws StorageException {
        this.delegate.createGateway(gateway);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void createPlugin(PluginBean plugin) throws StorageException {
        this.delegate.createPlugin(plugin);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        this.delegate.createPolicyDefinition(policyDef);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createAuditEntry(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void createAuditEntry(AuditEntryBean entry) throws StorageException {
        this.delegate.createAuditEntry(entry);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void updateOrganization(OrganizationBean organization) throws StorageException {
        this.delegate.updateOrganization(organization);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void updateApplication(ApplicationBean application) throws StorageException {
        this.delegate.updateApplication(application);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void updateApplicationVersion(ApplicationVersionBean version) throws StorageException {
        this.delegate.updateApplicationVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void updateService(ServiceBean service) throws StorageException {
        this.delegate.updateService(service);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void updateServiceVersion(ServiceVersionBean version) throws StorageException {
        this.delegate.updateServiceVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void updatePlan(PlanBean plan) throws StorageException {
        this.delegate.updatePlan(plan);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException {
        this.delegate.updatePlanVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePolicy(PolicyBean policy) throws StorageException {
        this.delegate.updatePolicy(policy);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void updateGateway(GatewayBean gateway) throws StorageException {
        this.delegate.updateGateway(gateway);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        this.delegate.updatePolicyDefinition(policyDef);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void deleteOrganization(OrganizationBean organization) throws StorageException {
        this.delegate.deleteOrganization(organization);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void deleteApplication(ApplicationBean application) throws StorageException {
        this.delegate.deleteApplication(application);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void deleteApplicationVersion(ApplicationVersionBean version) throws StorageException {
        this.delegate.deleteApplicationVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void deleteContract(ContractBean contract) throws StorageException {
        this.delegate.deleteContract(contract);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void deleteService(ServiceBean service) throws StorageException {
        this.delegate.deleteService(service);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void deleteServiceVersion(ServiceVersionBean version) throws StorageException {
        this.delegate.deleteServiceVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void deletePlan(PlanBean plan) throws StorageException {
        this.delegate.deletePlan(plan);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void deletePlanVersion(PlanVersionBean version) throws StorageException {
        this.delegate.deletePlanVersion(version);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void deletePolicy(PolicyBean policy) throws StorageException {
        this.delegate.deletePolicy(policy);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void deleteGateway(GatewayBean gateway) throws StorageException {
        this.delegate.deleteGateway(gateway);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void deletePlugin(PluginBean plugin) throws StorageException {
        this.delegate.deletePlugin(plugin);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        this.delegate.deletePolicyDefinition(policyDef);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getOrganization(java.lang.String)
     */
    @Override
    public OrganizationBean getOrganization(String id) throws StorageException {
        return this.delegate.getOrganization(id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApplication(java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationBean getApplication(String organizationId, String id) throws StorageException {
        return this.delegate.getApplication(organizationId, id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApplicationVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationVersionBean getApplicationVersion(String organizationId, String applicationId,
            String version) throws StorageException {
        return this.delegate.getApplicationVersion(organizationId, applicationId, version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getContract(java.lang.Long)
     */
    @Override
    public ContractBean getContract(Long id) throws StorageException {
        return this.delegate.getContract(id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getService(java.lang.String, java.lang.String)
     */
    @Override
    public ServiceBean getService(String organizationId, String id) throws StorageException {
        return this.delegate.getService(organizationId, id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getServiceVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceVersionBean getServiceVersion(String organizationId, String serviceId, String version)
            throws StorageException {
        return this.delegate.getServiceVersion(organizationId, serviceId, version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlan(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String id) throws StorageException {
        return this.delegate.getPlan(organizationId, id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws StorageException {
        return this.delegate.getPlanVersion(organizationId, planId, version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicy(io.apiman.manager.api.beans.policies.PolicyType, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public PolicyBean getPolicy(PolicyType type, String organizationId, String entityId, String version,
            Long id) throws StorageException {
        return this.delegate.getPolicy(type, organizationId, entityId, version, id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getGateway(java.lang.String)
     */
    @Override
    public GatewayBean getGateway(String id) throws StorageException {
        return this.delegate.getGateway(id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(long)
     */
    @Override
    public PluginBean getPlugin(long id) throws StorageException {
        return this.delegate.getPlugin(id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(java.lang.String, java.lang.String)
     */
    @Override
    public PluginBean getPlugin(String groupId, String artifactId) throws StorageException {
        refresh();
        return this.delegate.getPlugin(groupId, artifactId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#reorderPolicies(io.apiman.manager.api.beans.policies.PolicyType, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public void reorderPolicies(PolicyType type, String organizationId, String entityId,
            String entityVersion, List<Long> newOrder) throws StorageException {
        this.delegate.reorderPolicies(type, organizationId, entityId, entityVersion, newOrder);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicyDefinition(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException {
        return this.delegate.getPolicyDefinition(id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteServiceDefinition(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void deleteServiceDefinition(ServiceVersionBean version) throws StorageException {
        delegate.deleteServiceDefinition(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getServiceDefinition(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public InputStream getServiceDefinition(ServiceVersionBean serviceVersion) throws StorageException {
        return delegate.getServiceDefinition(serviceVersion);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateServiceDefinition(io.apiman.manager.api.beans.services.ServiceVersionBean, java.io.InputStream)
     */
    @Override
    public void updateServiceDefinition(ServiceVersionBean version, InputStream definitionStream)
            throws StorageException {
        delegate.updateServiceDefinition(version, definitionStream);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void updatePlugin(PluginBean pluginBean) throws StorageException {
        delegate.updatePlugin(pluginBean);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        this.delegate.createUser(user);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        return this.delegate.getUser(userId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        this.delegate.updateUser(user);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        this.delegate.createRole(role);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String roleId) throws StorageException {
        return this.delegate.getRole(roleId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        this.delegate.updateRole(role);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        this.delegate.deleteRole(role);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createMembership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        this.delegate.createMembership(membership);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId) throws StorageException {
        return this.delegate.getMembership(userId, roleId, organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        this.delegate.deleteMembership(userId, roleId, organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        refresh();
        this.delegate.deleteMemberships(userId, organizationId);
    }

    @Override
    public Iterator<OrganizationBean> getAllOrganizations() throws StorageException {
        return this.delegate.getAllOrganizations();
    }

    @Override
    public Iterator<GatewayBean> getAllGateways() throws StorageException {
        return this.delegate.getAllGateways();
    }

    @Override
    public Iterator<AuditEntryBean> getAllAuditEntries(String orgId) throws StorageException {
        return this.delegate.getAllAuditEntries(orgId);
    }

    @Override
    public Iterator<PluginBean> getAllPlugins() throws StorageException {
        return this.delegate.getAllPlugins();
    }

    @Override
    public Iterator<RoleMembershipBean> getAllMemberships(String orgId) throws StorageException {
        return this.delegate.getAllMemberships(orgId);
    }

    @Override
    public Iterator<UserBean> getAllUsers() throws StorageException {
        return this.delegate.getAllUsers();
    }

    @Override
    public Iterator<RoleBean> getAllRoles() throws StorageException {
        return this.delegate.getAllRoles();
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPolicyDefinitions()
     */
    @Override
    public Iterator<PolicyDefinitionBean> getAllPolicyDefinitions() throws StorageException {
        return this.delegate.getAllPolicyDefinitions();
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPlans(java.lang.String)
     */
    @Override
    public Iterator<PlanBean> getAllPlans(String organizationId) throws StorageException {
        return delegate.getAllPlans(organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPlanVersions(java.lang.String, java.lang.String)
     */
    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(String organizationId, String planId)
            throws StorageException {
        return delegate.getAllPlanVersions(organizationId, planId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllServices(java.lang.String)
     */
    @Override
    public Iterator<ServiceBean> getAllServices(String organizationId) throws StorageException {
        return delegate.getAllServices(organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllServiceVersions(java.lang.String, java.lang.String)
     */
    @Override
    public Iterator<ServiceVersionBean> getAllServiceVersions(String organizationId, String serviceId)
            throws StorageException {
        return delegate.getAllServiceVersions(organizationId, serviceId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllApplications(java.lang.String)
     */
    @Override
    public Iterator<ApplicationBean> getAllApplications(String organizationId) throws StorageException {
        return delegate.getAllApplications(organizationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllApplicationVersions(java.lang.String, java.lang.String)
     */
    @Override
    public Iterator<ApplicationVersionBean> getAllApplicationVersions(String organizationId,
            String applicationId) throws StorageException {
        return delegate.getAllApplicationVersions(organizationId, applicationId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Iterator<ContractBean> getAllContracts(String organizationId, String applicationId, String version)
            throws StorageException {
        return delegate.getAllContracts(organizationId, applicationId, version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public Iterator<PolicyBean> getAllPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        return delegate.getAllPolicies(organizationId, entityId, version, type);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#createDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void createDownload(DownloadBean download) throws StorageException {
        delegate.createDownload(download);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#deleteDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void deleteDownload(DownloadBean download) throws StorageException {
        delegate.deleteDownload(download);
    }
    
    /**
     * @see io.apiman.manager.api.core.IStorage#getDownload(java.lang.String)
     */
    @Override
    public DownloadBean getDownload(String id) throws StorageException {
        return delegate.getDownload(id);
    }

    /**
     * Force a refresh in elasticsearch so that the result of any indexing operations
     * up to this point will be visible to searches.
     */
    private void refresh() {
        try {
            esClient.execute(new Refresh.Builder().addIndex("apiman_manager").build()); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
