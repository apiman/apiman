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
package io.apiman.manager.api.exportimport;

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
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
import io.apiman.manager.api.exportimport.json.JsonGlobalStreamReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("nls")
public class IStorageTest implements IStorage {

    @Override
    public void beginTx() throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void commitTx() throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollbackTx() {
        // TODO Auto-generated method stub

    }

    @Override
    public void createOrganization(OrganizationBean organization) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createApplication(ApplicationBean application) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createApplicationVersion(ApplicationVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createContract(ContractBean contract) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createService(ServiceBean service) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createServiceVersion(ServiceVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createPlan(PlanBean plan) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createPolicy(PolicyBean policy) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createGateway(GatewayBean gateway) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createPlugin(PluginBean plugin) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createAuditEntry(AuditEntryBean entry) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateOrganization(OrganizationBean organization) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateApplication(ApplicationBean application) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateApplicationVersion(ApplicationVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateService(ServiceBean service) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateServiceVersion(ServiceVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateServiceDefinition(ServiceVersionBean version, InputStream definitionStream)
            throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePlan(PlanBean plan) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePolicy(PolicyBean policy) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateGateway(GatewayBean gateway) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteOrganization(OrganizationBean organization) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteApplication(ApplicationBean application) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteApplicationVersion(ApplicationVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteContract(ContractBean contract) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteService(ServiceBean service) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteServiceVersion(ServiceVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteServiceDefinition(ServiceVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePlan(PlanBean plan) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePlanVersion(PlanVersionBean version) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePolicy(PolicyBean policy) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteGateway(GatewayBean gateway) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePlugin(PluginBean plugin) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterator<OrganizationBean> getAllOrganizations() throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OrganizationBean getOrganization(String id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationBean getApplication(String organizationId, String id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationVersionBean getApplicationVersion(String organizationId, String applicationId,
            String version) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<ApplicationVersionBean> getAllApplicationVersions(String organizationId)
            throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ContractBean getContract(Long id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<ContractBean> getContracts(String organizationId) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceBean getService(String organizationId, String id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceVersionBean getServiceVersion(String organizationId, String serviceId, String version)
            throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getServiceDefinition(ServiceVersionBean serviceVersion) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlanBean getPlan(String organizationId, String id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PolicyBean getPolicy(PolicyType type, String organizationId, String entityId, String version,
            Long id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GatewayBean getGateway(String id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginBean getPlugin(long id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginBean getPlugin(String groupId, String artifactId) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void reorderPolicies(PolicyType type, String organizationId, String entityId, String entityVersion,
            List<Long> newOrder) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterator<ServiceVersionBean> getAllServiceVersions(String organizationId) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(String organizationId) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<GatewayBean> getAllGateways() throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    public static void main(String... args) throws Exception {
        File in = new File("/tmp/jsonout");
        FileInputStream is = new FileInputStream(in);
        JsonGlobalStreamReader sr = new JsonGlobalStreamReader(is, new IStorageTest());

        sr.parse();

        //JsonOrgStreamReader sr = new JsonOrgStreamReader(is, new IStorageTest(), new §);
        //sr.parse();
    }

    @Override
    public Iterator<PluginBean> getAllPlugins() throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<AuditEntryBean> getAllAuditEntries(String orgId) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<PolicyBean> getAllPolicies(String id) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updatePlugin(PluginBean pluginBean) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createUser(UserBean user) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public UserBean getUser(String userId) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateUser(UserBean user) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createRole(RoleBean role) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public RoleBean getRole(String roleId) throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateRole(RoleBean role) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId)
            throws StorageException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteMembership(String userId, String roleId, String organizationId)
            throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        // TODO Auto-generated method stub

    }

    @Override
    public Iterator<RoleMembershipBean> getAllMemberships(String orgId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<UserBean> getAllUsers(String orgId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<UserBean> getAllUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<RoleBean> getAllRoles(String orgId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<RoleBean> getAllRoles() {
        // TODO Auto-generated method stub
        return null;
    }

}
