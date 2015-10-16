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
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.read.IImportReaderDispatcher;

@SuppressWarnings("nls")
public class LoggingDispatcher implements IImportReaderDispatcher {

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#metadata(io.apiman.manager.api.exportimport.beans.MetadataBean)
     */
    @Override
    public void metadata(MetadataBean metadata) {
        System.out.println("metadata: " + metadata);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#user(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void user(UserBean user) {
        System.out.println("user: " + user);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#role(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void role(RoleBean role) {
        System.out.println("role: " + role);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#plugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void plugin(PluginBean pb) {
        System.out.println("plugin: " + pb);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#gateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void gateway(GatewayBean gb) {
        System.out.println("gateway: " + gb);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#policyDef(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void policyDef(PolicyDefinitionBean policyDef) {
        System.out.println("policyDef: " + policyDef);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#organization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void organization(OrganizationBean org) {
        System.out.println("org: " + org);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#membership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void membership(RoleMembershipBean membership) {
        System.out.println("\tmembership: " + membership);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#plan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void plan(PlanBean plan) {
        System.out.println("\tplan: " + plan);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#planVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void planVersion(PlanVersionBean pvb) {
        System.out.println("\t\tplanVersion: " + pvb);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#planPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void planPolicy(PolicyBean policy) {
        System.out.println("\t\t\tpolicy: " + policy);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#service(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void service(ServiceBean service) {
        System.out.println("\tservice: " + service);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#serviceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void serviceVersion(ServiceVersionBean svb) {
        System.out.println("\t\tserviceVersion: " + svb);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#servicePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void servicePolicy(PolicyBean policy) {
        System.out.println("\t\t\tpolicy: " + policy);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#application(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void application(ApplicationBean application) {
        System.out.println("\tapp: " + application);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void applicationVersion(ApplicationVersionBean avb) {
        System.out.println("\t\tappVersion: " + avb);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void applicationPolicy(PolicyBean policy) {
        System.out.println("\t\t\tpolicy: " + policy);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void applicationContract(ContractBean cb) {
        System.out.println("\t\t\tcontract: " + cb);
        
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#audit(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void audit(AuditEntryBean ab) {
        System.out.println("\taudit: " + ab);
        
    }

}
