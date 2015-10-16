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
 * Used to listen to the reading of an apiman import file.  The import
 * reader will parse the import file and then fire events for each
 * entity it finds in the import.  This dispatcher is the callback for 
 * those events.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IImportReaderDispatcher {
    // Metadata
    void metadata(MetadataBean metadata);

    // Users
    void user(UserBean user);

    // Role definitions
    void role(RoleBean role);

    // Plugins
    void plugin(PluginBean pb);

    // Gateways
    void gateway(GatewayBean gb);

    // Policy Definitions
    void policyDef(PolicyDefinitionBean policyDef);

    // -----------------------------------
    // Orgs, apps, plans, services, etc...
    // -----------------------------------
    void organization(OrganizationBean org);

    void membership(RoleMembershipBean membership);

    void plan(PlanBean plan);
    void planVersion(PlanVersionBean pvb);
    void planPolicy(PolicyBean policy);

    void service(ServiceBean service);
    void serviceVersion(ServiceVersionBean svb);
    void servicePolicy(PolicyBean policy);

    void application(ApplicationBean application);
    void applicationVersion(ApplicationVersionBean avb);
    void applicationPolicy(PolicyBean policy);
    void applicationContract(ContractBean cb);

    void audit(AuditEntryBean ab);
}
