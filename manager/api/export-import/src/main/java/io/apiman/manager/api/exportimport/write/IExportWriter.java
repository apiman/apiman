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
package io.apiman.manager.api.exportimport.write;

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
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.exportimport.beans.MetadataBean;

/**
 * 
 * @author msavy
 */
public interface IExportWriter {
    // Metadata
    IExportWriter writeMetadata(MetadataBean metadata);

    // Users
    IExportWriter startUsers();
    IExportWriter writeUser(UserBean user);
    IExportWriter endUsers();

    // Role definitions
    IExportWriter startRoles();
    IExportWriter writeRole(RoleBean role);
    IExportWriter endRoles();

    // Plugins
    IExportWriter startPlugins();
    IExportWriter writePlugin(PluginBean pb);
    IExportWriter endPlugins();

    // Gateways
    IExportWriter startGateways();
    IExportWriter writeGateway(GatewayBean gb);
    IExportWriter endGateways();

    // Policy Definitions
    IExportWriter startPolicyDefs();
    IExportWriter writePolicyDef(PolicyDefinitionBean policyDef);
    IExportWriter endPolicyDefs();

    // -----------------------------------
    // Orgs, apps, plans, services, etc...
    // -----------------------------------
    IExportWriter startOrgs();
    IExportWriter startOrg(OrganizationBean org);

    IExportWriter startMemberships();
    IExportWriter writeMembership(RoleMembershipBean membership);
    IExportWriter endMemberships();

    IExportWriter startPlans();
    IExportWriter startPlan(PlanBean plan);
    IExportWriter startPlanVersions();
    IExportWriter startPlanVersion(PlanVersionBean pvb);
    IExportWriter startPlanPolicies();
    IExportWriter writePlanPolicy(PolicyBean policy);
    IExportWriter endPlanPolicies();
    IExportWriter endPlanVersion();
    IExportWriter endPlanVersions();
    IExportWriter endPlan();
    IExportWriter endPlans();

    IExportWriter startServices();
    IExportWriter startService(ServiceBean service);
    IExportWriter startServiceVersions();
    IExportWriter startServiceVersion(ServiceVersionBean svb);
    IExportWriter startServicePolicies();
    IExportWriter writeServicePolicy(PolicyBean policy);
    IExportWriter endServicePolicies();
    IExportWriter endServiceVersion();
    IExportWriter endServiceVersions();
    IExportWriter endService();
    IExportWriter endServices();

    IExportWriter startApplications();
    IExportWriter startApplication(ApplicationBean application);
    IExportWriter startApplicationVersions();
    IExportWriter startApplicationVersion(ApplicationVersionBean avb);
    IExportWriter startApplicationPolicies();
    IExportWriter writeApplicationPolicy(PolicyBean policy);
    IExportWriter endApplicationPolicies();
    IExportWriter startApplicationContracts();
    IExportWriter writeApplicationContract(ContractBean cb);
    IExportWriter endApplicationContracts();
    IExportWriter endApplicationVersion();
    IExportWriter endApplicationVersions();
    IExportWriter endApplication();
    IExportWriter endApplications();

    IExportWriter startAudits();
    IExportWriter writeAudit(AuditEntryBean ab);
    IExportWriter endAudits();

    IExportWriter endOrg();
    IExportWriter endOrgs();

    void close();
}
