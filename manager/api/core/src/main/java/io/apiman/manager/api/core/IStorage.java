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
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.core.exceptions.StorageException;

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
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException;
    public void createRole(RoleBean role) throws StorageException;
    public void createAuditEntry(AuditEntryBean entry) throws StorageException;

    /*
     * Various update methods.  These are called by the REST layer to update stuff.
     */

    public void updateOrganization(OrganizationBean organization) throws StorageException;
    public void updateApplication(ApplicationBean application) throws StorageException;
    public void updateApplicationVersion(ApplicationVersionBean version) throws StorageException;
    public void updateContract(ContractBean contract) throws StorageException;
    public void updateService(ServiceBean service) throws StorageException;
    public void updateServiceVersion(ServiceVersionBean version) throws StorageException;
    public void updatePlan(PlanBean plan) throws StorageException;
    public void updatePlanVersion(PlanVersionBean version) throws StorageException;
    public void updatePolicy(PolicyBean policy) throws StorageException;
    public void updateGateway(GatewayBean gateway) throws StorageException;
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException;
    public void updateRole(RoleBean role) throws StorageException;

    /*
     * Various delete methods.  These are called by the REST layer to delete stuff.
     */

    public void deleteOrganization(OrganizationBean organization) throws StorageException;
    public void deleteApplication(ApplicationBean application) throws StorageException;
    public void deleteApplicationVersion(ApplicationVersionBean version) throws StorageException;
    public void deleteContract(ContractBean contract) throws StorageException;
    public void deleteService(ServiceBean service) throws StorageException;
    public void deleteServiceVersion(ServiceVersionBean version) throws StorageException;
    public void deletePlan(PlanBean plan) throws StorageException;
    public void deletePlanVersion(PlanVersionBean version) throws StorageException;
    public void deletePolicy(PolicyBean policy) throws StorageException;
    public void deleteGateway(GatewayBean gateway) throws StorageException;
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException;
    public void deleteRole(RoleBean role) throws StorageException;

    /*
     * Various get methods.  These are called by the REST layer to get stuff.
     */

    public OrganizationBean getOrganization(String id) throws StorageException;
    public ApplicationBean getApplication(String organizationId, String id) throws StorageException;
    public ApplicationVersionBean getApplicationVersion(String organizationId, String applicationId, String version) throws StorageException;
    public ContractBean getContract(Long id) throws StorageException;
    public ServiceBean getService(String organizationId, String id) throws StorageException;
    public ServiceVersionBean getServiceVersion(String organizationId, String serviceId, String version) throws StorageException;
    public PlanBean getPlan(String organizationId, String id) throws StorageException;
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version) throws StorageException;
    public PolicyBean getPolicy(Long id) throws StorageException;
    public GatewayBean getGateway(String id) throws StorageException;
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException;
    public RoleBean getRole(String id) throws StorageException;

}
