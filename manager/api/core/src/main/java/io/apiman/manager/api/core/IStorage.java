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
import io.apiman.manager.api.core.exceptions.AlreadyExistsException;
import io.apiman.manager.api.core.exceptions.DoesNotExistException;
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
    
    public void createOrganization(OrganizationBean organization) throws StorageException, AlreadyExistsException;
    public void createApplication(ApplicationBean application) throws StorageException, AlreadyExistsException;
    public void createApplicationVersion(ApplicationVersionBean version) throws StorageException, AlreadyExistsException;
    public void createContract(ContractBean contract) throws StorageException, AlreadyExistsException;
    public void createService(ServiceBean service) throws StorageException, AlreadyExistsException;
    public void createServiceVersion(ServiceVersionBean version) throws StorageException, AlreadyExistsException;
    public void createPlan(PlanBean plan) throws StorageException, AlreadyExistsException;
    public void createPlanVersion(PlanVersionBean version) throws StorageException, AlreadyExistsException;
    public void createPolicy(PolicyBean policy) throws StorageException, AlreadyExistsException;
    public void createGateway(GatewayBean gateway) throws StorageException, AlreadyExistsException;
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException, AlreadyExistsException;
    public void createRole(RoleBean role) throws StorageException, AlreadyExistsException;
    public void createAuditEntry(AuditEntryBean entry) throws StorageException;

    /*
     * Various update methods.  These are called by the REST layer to update stuff.
     */

    public void updateOrganization(OrganizationBean organization) throws StorageException, DoesNotExistException;
    public void updateApplication(ApplicationBean application) throws StorageException, DoesNotExistException;
    public void updateApplicationVersion(ApplicationVersionBean version) throws StorageException, DoesNotExistException;
    public void updateContract(ContractBean contract) throws StorageException, DoesNotExistException;
    public void updateService(ServiceBean service) throws StorageException, DoesNotExistException;
    public void updateServiceVersion(ServiceVersionBean version) throws StorageException, DoesNotExistException;
    public void updatePlan(PlanBean plan) throws StorageException, DoesNotExistException;
    public void updatePlanVersion(PlanVersionBean version) throws StorageException, DoesNotExistException;
    public void updatePolicy(PolicyBean policy) throws StorageException, DoesNotExistException;
    public void updateGateway(GatewayBean gateway) throws StorageException, DoesNotExistException;
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException, DoesNotExistException;
    public void updateRole(RoleBean role) throws StorageException, DoesNotExistException;

    /*
     * Various delete methods.  These are called by the REST layer to delete stuff.
     */

    public void deleteOrganization(OrganizationBean organization) throws StorageException, DoesNotExistException;
    public void deleteApplication(ApplicationBean application) throws StorageException, DoesNotExistException;
    public void deleteApplicationVersion(ApplicationVersionBean version) throws StorageException, DoesNotExistException;
    public void deleteContract(ContractBean contract) throws StorageException, DoesNotExistException;
    public void deleteService(ServiceBean service) throws StorageException, DoesNotExistException;
    public void deleteServiceVersion(ServiceVersionBean version) throws StorageException, DoesNotExistException;
    public void deletePlan(PlanBean plan) throws StorageException, DoesNotExistException;
    public void deletePlanVersion(PlanVersionBean version) throws StorageException, DoesNotExistException;
    public void deletePolicy(PolicyBean policy) throws StorageException, DoesNotExistException;
    public void deleteGateway(GatewayBean gateway) throws StorageException, DoesNotExistException;
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException, DoesNotExistException;
    public void deleteRole(RoleBean role) throws StorageException, DoesNotExistException;

    /*
     * Various get methods.  These are called by the REST layer to get stuff.
     */

    public OrganizationBean getOrganization(String id) throws StorageException, DoesNotExistException;
    public ApplicationBean getApplication(String organizationId, String id) throws StorageException, DoesNotExistException;
    public ApplicationVersionBean getApplicationVersion(String organizationId, String applicationId, String version) throws StorageException, DoesNotExistException;
    public ContractBean getContract(Long id) throws StorageException, DoesNotExistException;
    public ServiceBean getService(String organizationId, String id) throws StorageException, DoesNotExistException;
    public ServiceVersionBean getServiceVersion(String organizationId, String applicationId, String version) throws StorageException, DoesNotExistException;
    public PlanBean getPlan(String organizationId, String id) throws StorageException, DoesNotExistException;
    public PlanVersionBean getPlanVersion(String organizationId, String applicationId, String version) throws StorageException, DoesNotExistException;
    public PolicyBean getPolicy(Long id) throws StorageException, DoesNotExistException;
    public GatewayBean getGateway(String id) throws StorageException, DoesNotExistException;
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException, DoesNotExistException;
    public RoleBean getRole(String id) throws StorageException, DoesNotExistException;

}
