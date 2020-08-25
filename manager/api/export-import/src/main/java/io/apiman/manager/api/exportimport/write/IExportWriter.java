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

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
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
import io.apiman.manager.api.beans.system.MetadataBean;

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
    // Orgs, apps, plans, APIs, etc...
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

    IExportWriter startApis();
    IExportWriter startApi(ApiBean api);
    IExportWriter startApiVersions();
    IExportWriter startApiVersion(ApiVersionBean svb);
    IExportWriter startApiPolicies();
    IExportWriter writeApiPolicy(PolicyBean policy);
    IExportWriter endApiPolicies();
    IExportWriter endApiVersion();
    IExportWriter endApiVersions();
    IExportWriter endApi();
    IExportWriter endApis();

    IExportWriter startClients();
    IExportWriter startClient(ClientBean client);
    IExportWriter startClientVersions();
    IExportWriter startClientVersion(ClientVersionBean avb);
    IExportWriter startClientPolicies();
    IExportWriter writeClientPolicy(PolicyBean policy);
    IExportWriter endClientPolicies();
    IExportWriter startClientContracts();
    IExportWriter writeClientContract(ContractBean cb);
    IExportWriter endClientContracts();
    IExportWriter endClientVersion();
    IExportWriter endClientVersions();
    IExportWriter endClient();
    IExportWriter endClients();

    IExportWriter startAudits();
    IExportWriter writeAudit(AuditEntryBean ab);
    IExportWriter endAudits();

    IExportWriter endOrg();
    IExportWriter endOrgs();

    void close();
}
