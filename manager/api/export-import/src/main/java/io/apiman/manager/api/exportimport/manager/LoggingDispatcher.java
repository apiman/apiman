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

import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.developers.DeveloperBean;
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
import io.apiman.manager.api.exportimport.read.IImportReaderDispatcher;

@SuppressWarnings("nls")
public class LoggingDispatcher implements IImportReaderDispatcher {

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#metadata(MetadataBean)
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
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#api(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void api(ApiBean api) {
        System.out.println("\tapi: " + api);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#apiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void apiVersion(ApiVersionBean svb) {
        System.out.println("\t\tapiVersion: " + svb);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#apiPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void apiPolicy(PolicyBean policy) {
        System.out.println("\t\t\tpolicy: " + policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#client(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void client(ClientBean client) {
        System.out.println("\tapp: " + client);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#clientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void clientVersion(ClientVersionBean avb) {
        System.out.println("\t\tappVersion: " + avb);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#clientPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void clientPolicy(PolicyBean policy) {
        System.out.println("\t\t\tpolicy: " + policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#clientContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void clientContract(ContractBean cb) {
        System.out.println("\t\t\tcontract: " + cb);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#audit(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void audit(AuditEntryBean ab) {
        System.out.println("\taudit: " + ab);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#developer(DeveloperBean)
     */
    @Override
    public void developer(DeveloperBean developer) {
        System.out.println("developer:" + developer);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#close()
     */
    @Override
    public void close() {
        System.out.println("close");
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#cancel()
     */
    @Override
    public void cancel() {
        System.out.println("cancel");
    }

}
