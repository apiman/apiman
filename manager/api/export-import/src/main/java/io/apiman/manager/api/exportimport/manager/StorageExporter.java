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

package io.apiman.manager.api.exportimport.manager;

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
import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.write.IExportWriter;

import java.util.Iterator;

import org.joda.time.DateTime;

class StorageExporter {
    private Version version;
    private IExportWriter writer;
    private IStorage storage;
    private String orgId;

    /**
     * Constructor.
     * @param writer
     * @param storage
     * @param orgId
     */
    public StorageExporter(Version version, IExportWriter writer, IStorage storage, String orgId) {
        this.version = version;
        this.writer = writer;
        this.storage = storage;
        this.orgId = orgId;
    }

    /**
     * Called begin the export process.
     */
    public void export() {
        try {
            storage.beginTx();
            try {
                exportMetadata();
                exportUsers();
                exportGateways();
                exportPlugins();
                exportRoles();
                exportPolicyDefs();

                exportOrgs();
            } finally {
                storage.rollbackTx();
                writer.close();
            }
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    private void exportOrgs() {
        try {
          writer.startOrgs();

          Iterator<OrganizationBean> iter = storage.getAllOrganizations();

          while (iter.hasNext()) {
              OrganizationBean bean = iter.next();
              writer.startOrg(bean);

              writeMemberships(bean.getId());
              writePlans(bean.getId());
              writeServices(bean.getId());
              writeApplications(bean.getId());
              writeAudits(bean.getId());
              
              writer.endOrg();
          }

          writer.endOrgs();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writePlans(String orgId) {
        try {
            writer.startPlans();
            Iterator<PlanBean> planIter = storage.getAllPlans(orgId);
            while (planIter.hasNext()) {
                PlanBean planBean = planIter.next();
                writer.startPlan(planBean);
                writer.startPlanVersions();
                Iterator<PlanVersionBean> versionIter = storage.getAllPlanVersions(orgId, planBean.getId());
                while (versionIter.hasNext()) {
                    PlanVersionBean versionBean = versionIter.next();
                    writer.startPlanVersion(versionBean);
                    writer.startPlanPolicies();
                    Iterator<PolicyBean> policyIter = storage.getAllPolicies(orgId, planBean.getId(), versionBean.getVersion(), PolicyType.Plan);
                    while (policyIter.hasNext()) {
                        PolicyBean policyBean = policyIter.next();
                        writer.writePlanPolicy(policyBean);
                    }
                    writer.endPlanPolicies();
                    writer.endPlanVersion();
                }
                writer.endPlanVersions();
                writer.endPlan();
            }
            writer.endPlans();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeServices(String orgId) {
        try {
            writer.startServices();
            Iterator<ServiceBean> serviceIter = storage.getAllServices(orgId);
            while (serviceIter.hasNext()) {
                ServiceBean serviceBean = serviceIter.next();
                writer.startService(serviceBean);
                writer.startServiceVersions();
                Iterator<ServiceVersionBean> versionIter = storage.getAllServiceVersions(orgId, serviceBean.getId());
                while (versionIter.hasNext()) {
                    ServiceVersionBean versionBean = versionIter.next();
                    writer.startServiceVersion(versionBean);
                    writer.startServicePolicies();
                    Iterator<PolicyBean> policyIter = storage.getAllPolicies(orgId, serviceBean.getId(), versionBean.getVersion(), PolicyType.Service);
                    while (policyIter.hasNext()) {
                        PolicyBean policyBean = policyIter.next();
                        writer.writeServicePolicy(policyBean);
                    }
                    writer.endServicePolicies();
                    writer.endServiceVersion();
                }
                writer.endServiceVersions();
                writer.endService();
            }
            writer.endServices();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeApplications(String orgId) {
        try {
            writer.startApplications();
            Iterator<ApplicationBean> applicationIter = storage.getAllApplications(orgId);
            while (applicationIter.hasNext()) {
                ApplicationBean applicationBean = applicationIter.next();
                writer.startApplication(applicationBean);
                writer.startApplicationVersions();
                Iterator<ApplicationVersionBean> versionIter = storage.getAllApplicationVersions(orgId, applicationBean.getId());
                while (versionIter.hasNext()) {
                    ApplicationVersionBean versionBean = versionIter.next();
                    writer.startApplicationVersion(versionBean);
                    
                    // Policies
                    writer.startApplicationPolicies();
                    Iterator<PolicyBean> policyIter = storage.getAllPolicies(orgId, applicationBean.getId(), versionBean.getVersion(), PolicyType.Application);
                    while (policyIter.hasNext()) {
                        PolicyBean policyBean = policyIter.next();
                        writer.writeApplicationPolicy(policyBean);
                    }
                    writer.endApplicationPolicies();
                    
                    // Contracts
                    writer.startApplicationContracts();
                    Iterator<ContractBean> contractIter = storage.getAllContracts(orgId, applicationBean.getId(), versionBean.getVersion());
                    while (contractIter.hasNext()) {
                        ContractBean contractBean = contractIter.next();
                        writer.writeApplicationContract(contractBean);
                    }
                    writer.endApplicationContracts();
                    
                    writer.endApplicationVersion();
                }
                writer.endApplicationVersions();
                writer.endApplication();
            }
            writer.endApplications();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMemberships(String orgId) {
        try {
            Iterator<RoleMembershipBean> iter = storage.getAllMemberships(orgId);

            writer.startMemberships();

            while(iter.hasNext()) {
                RoleMembershipBean bean = iter.next();
                writer.writeMembership(bean);
            }

            writer.endMemberships();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeAudits(String orgId) {
        try {
            Iterator<AuditEntryBean> iter = storage.getAllAuditEntries(orgId);

            writer.startAudits();

            while(iter.hasNext()) {
                AuditEntryBean bean = iter.next();
                writer.writeAudit(bean);
            }

            writer.endAudits();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void exportMetadata() {
        MetadataBean metadata = new MetadataBean();
        metadata.setApimanVersion(version.getVersionString());
        metadata.setExportedOn(new DateTime());
        writer.writeMetadata(metadata);
    }

    private void exportGateways() {
        try {
            Iterator<GatewayBean> iter = storage.getAllGateways();

            writer.startGateways();

            while(iter.hasNext()) {
                GatewayBean bean = iter.next();
                writer.writeGateway(bean);
            }

            writer.endGateways();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void exportPlugins() {
        try {
            Iterator<PluginBean> iter = storage.getAllPlugins();

            writer.startPlugins();

            while(iter.hasNext()) {
                PluginBean bean = iter.next();
                writer.writePlugin(bean);
            }

            writer.endPlugins();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void exportUsers() {
        try {
            Iterator<UserBean> iter;

            if (orgId != null) {
                iter = storage.getAllUsers(orgId);
            } else {
                iter = storage.getAllUsers();
            }

            writer.startUsers();

            while (iter.hasNext()) {
                UserBean bean = iter.next();
                writer.writeUser(bean);
            }

            writer.endUsers();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void exportRoles() {
        try {
            Iterator<RoleBean> iter;
            
            iter = storage.getAllRoles();
            
            writer.startRoles();

            while(iter.hasNext()) {
                RoleBean bean = iter.next();
                writer.writeRole(bean);
            }

            writer.endRoles();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void exportPolicyDefs() {
        try {
            Iterator<PolicyDefinitionBean> iter;
            
            iter = storage.getAllPolicyDefinitions();
            
            writer.startPolicyDefs();

            while(iter.hasNext()) {
                PolicyDefinitionBean bean = iter.next();
                writer.writePolicyDef(bean);
            }

            writer.endPolicyDefs();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}