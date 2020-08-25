/*
 * Copyright 2013 JBoss Inc
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

package io.apiman.manager.api.exportimport.read;

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
import io.apiman.manager.api.exportimport.exceptions.ImportNotNeededException;

/**
 * Used to listen to the reading of an apiman import file.  The import
 * reader will parse the import file and then fire events for each
 * entity it finds in the import.  This dispatcher is the callback for
 * those events.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IImportReaderDispatcher {
    // Metadata
    void metadata(MetadataBean metadata) throws ImportNotNeededException;

    // Users
    void user(UserBean user);

    // Role definitions
    void role(RoleBean role);

    // Plugins
    void plugin(PluginBean plugin);

    // Gateways
    void gateway(GatewayBean gateway);

    // Policy Definitions
    void policyDef(PolicyDefinitionBean policyDef);

    // -----------------------------------
    // Orgs, clients, plans, apis, etc...
    // -----------------------------------
    void organization(OrganizationBean org);

    void membership(RoleMembershipBean membership);

    void plan(PlanBean plan);
    void planVersion(PlanVersionBean planVersion);
    void planPolicy(PolicyBean policy);

    void api(ApiBean api);
    void apiVersion(ApiVersionBean apiVersion);
    void apiPolicy(PolicyBean policy);

    void client(ClientBean client);
    void clientVersion(ClientVersionBean clientVersion);
    void clientPolicy(PolicyBean policy);
    void clientContract(ContractBean contract);

    void audit(AuditEntryBean auditEntry);

    // Called when the import is complete
    void close();

    // Called to perform a rollback of the storage
    void cancel();
}
