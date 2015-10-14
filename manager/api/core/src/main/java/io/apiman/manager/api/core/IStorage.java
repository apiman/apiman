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
import io.apiman.manager.api.core.exceptions.StorageException;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the persistent storage interface for Apiman DT.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IStorage {

    /*
     * Transaction related methods
     */

    public void beginTx() throws StorageException;
    public void commitTx() throws StorageException;
    public void rollbackTx();

    /*
     * Various creation methods.  These are called by the REST layer to create stuff.
     */

    public void createOrganization(OrganizationBean organization) throws StorageException;
    public void createApplication(ApplicationBean application) throws StorageException;
    public void createApplicationVersion(ApplicationVersionBean version) throws StorageException;
    public void createContract(ContractBean contract) throws StorageException;
    public void createService(ServiceBean service) throws StorageException;
    public void createServiceVersion(ServiceVersionBean version) throws StorageException;
    public void createPlan(PlanBean plan) throws StorageException;
    public void createPlanVersion(PlanVersionBean version) throws StorageException;
    public void createPolicy(PolicyBean policy) throws StorageException;
    public void createGateway(GatewayBean gateway) throws StorageException;
    public void createPlugin(PluginBean plugin) throws StorageException;
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException;
    public void createAuditEntry(AuditEntryBean entry) throws StorageException;

    public Iterator<AuditEntryBean> getAllAuditEntries(String orgId) throws StorageException;

    /*
     * Various update methods.  These are called by the REST layer to update stuff.
     */

    public void updateOrganization(OrganizationBean organization) throws StorageException;
    public void updateApplication(ApplicationBean application) throws StorageException;
    public void updateApplicationVersion(ApplicationVersionBean version) throws StorageException;
    public void updateService(ServiceBean service) throws StorageException;
    public void updateServiceVersion(ServiceVersionBean version) throws StorageException;
    public void updateServiceDefinition(ServiceVersionBean version, InputStream definitionStream) throws StorageException;
    public void updatePlan(PlanBean plan) throws StorageException;
    public void updatePlanVersion(PlanVersionBean version) throws StorageException;
    public void updatePolicy(PolicyBean policy) throws StorageException;
    public void updateGateway(GatewayBean gateway) throws StorageException;
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException;
    public void updatePlugin(PluginBean pluginBean) throws StorageException;

    /*
     * Various delete methods.  These are called by the REST layer to delete stuff.
     */

    public void deleteOrganization(OrganizationBean organization) throws StorageException;
    public void deleteApplication(ApplicationBean application) throws StorageException;
    public void deleteApplicationVersion(ApplicationVersionBean version) throws StorageException;
    public void deleteContract(ContractBean contract) throws StorageException;
    public void deleteService(ServiceBean service) throws StorageException;
    public void deleteServiceVersion(ServiceVersionBean version) throws StorageException;
    public void deleteServiceDefinition(ServiceVersionBean version) throws StorageException;
    public void deletePlan(PlanBean plan) throws StorageException;
    public void deletePlanVersion(PlanVersionBean version) throws StorageException;
    public void deletePolicy(PolicyBean policy) throws StorageException;
    public void deleteGateway(GatewayBean gateway) throws StorageException;
    public void deletePlugin(PluginBean plugin) throws StorageException;
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException;
    public Iterator<PolicyDefinitionBean> getAllPolicyDefinitions() throws StorageException;

    /*
     * Various get methods.  These are called by the REST layer to get stuff.
     */

    public Iterator<OrganizationBean> getAllOrganizations() throws StorageException;
    public OrganizationBean getOrganization(String id) throws StorageException;

    public ApplicationBean getApplication(String organizationId, String id) throws StorageException;
    public ApplicationVersionBean getApplicationVersion(String organizationId, String applicationId, String version) throws StorageException;
    public Iterator<ApplicationVersionBean> getAllApplicationVersions(String organizationId) throws StorageException;


    public ContractBean getContract(Long id) throws StorageException;
    public Iterator<ContractBean> getAllContracts(String organizationId) throws StorageException;

    public ServiceBean getService(String organizationId, String id) throws StorageException;
    public ServiceVersionBean getServiceVersion(String organizationId, String serviceId, String version) throws StorageException;
    public Iterator<ServiceVersionBean> getAllServiceVersions(String organizationId) throws StorageException;

    public InputStream getServiceDefinition(ServiceVersionBean serviceVersion) throws StorageException;
    public PlanBean getPlan(String organizationId, String id) throws StorageException;
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version) throws StorageException;
    public Iterator<PlanVersionBean> getAllPlanVersions(String organizationId) throws StorageException;

    public PolicyBean getPolicy(PolicyType type, String organizationId, String entityId, String version, Long id) throws StorageException;
    public GatewayBean getGateway(String id) throws StorageException;
    public Iterator<GatewayBean> getAllGateways() throws StorageException;


    public PluginBean getPlugin(long id) throws StorageException;
    public PluginBean getPlugin(String groupId, String artifactId) throws StorageException;
    public Iterator<PluginBean> getAllPlugins() throws StorageException;

    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException;
    public Iterator<PolicyBean> getAllPolicies(String id) throws StorageException;

    /*
     * Anything that doesn't fall into the above categories!
     */
    public void reorderPolicies(PolicyType type, String organizationId, String entityId,
            String entityVersion, List<Long> newOrder) throws StorageException;

    /*
     * Here are some IDM related storage methods.
     */
    public void createUser(UserBean user) throws StorageException;
    public UserBean getUser(String userId) throws StorageException;
    public void updateUser(UserBean user) throws StorageException;
    public void createRole(RoleBean role) throws StorageException;
    public RoleBean getRole(String roleId) throws StorageException;
    public void updateRole(RoleBean role) throws StorageException;
    public void deleteRole(RoleBean role) throws StorageException;
    public void createMembership(RoleMembershipBean membership) throws StorageException;
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId) throws StorageException;
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException;
    public void deleteMemberships(String userId, String organizationId) throws StorageException;
    public Iterator<RoleMembershipBean> getAllMemberships(String orgId) throws StorageException;
    public Iterator<UserBean> getAllUsers(String orgId) throws StorageException;
    public Iterator<UserBean> getAllUsers() throws StorageException;
    public Iterator<RoleBean> getAllRoles() throws StorageException;
}
