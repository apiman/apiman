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

import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.json.ExportImportFactory;
import io.apiman.manager.api.exportimport.json.JsonExportImportFactory;
import io.apiman.manager.api.exportimport.read.IStreamReader;
import io.apiman.manager.api.exportimport.write.IGlobalStreamWriter;
import io.apiman.manager.api.exportimport.write.IOrgStreamWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.DateTime;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ExportImportManager {
    private static ExportImportConfigParser config = new ExportImportConfigParser();
    @Inject
    private IStorage iStorage;

    private Version version = new Version();
    private Map<ExportImportProviders, ExportImportFactory> eiFactories = new HashMap<>();
    private ExportImportProviders provider;

    // TODO We should have some kind of automated registration of these & factory pattern. This is interim.
    {
        eiFactories.put(ExportImportProviders.JSON, new JsonExportImportFactory());
    }

    public ExportImportManager() {
        provider = config.getProvider();
    }

    public static boolean isExportImport() {
        return config.getFunction() != ExportImportFunction.NONE;
    }

    public void doImportExport() {
        if(config.getFunction() == ExportImportFunction.IMPORT) {
            doImport();
        } else if (config.getFunction() == ExportImportFunction.EXPORT) {
            doExport();
        }
    }

    private void doImport() {
        IStreamReader reader = eiFactories.get(provider).getReader(config, iStorage);

        try {
            reader.parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doExport() {
        IGlobalStreamWriter writer = eiFactories.get(provider).getWriter(config, iStorage);
        new ExportWriter(writer, iStorage, null).write();
    }

    private class ExportWriter {
        private IGlobalStreamWriter writer;
        private IStorage iStorage;
        private String orgId;

        public ExportWriter(IGlobalStreamWriter writer,
                IStorage iStorage,
                String orgId) {
            this.writer = writer;
            this.iStorage = iStorage;
            this.orgId = orgId;
        }

        public void write() {
            writeUsers();
            writeMetadata();
            writeGateways();
            writePlugins();
            writeRoles();

            writeOrgs();

            writer.close();
        }

        private void writeOrgs() {
            try {
              writer.startOrgs();

              Iterator<OrganizationBean> iter = iStorage.getAllOrganizations();

              while (iter.hasNext()) {
                  OrganizationBean bean = iter.next();
                  IOrgStreamWriter orgWriter = writer.writeOrg(bean);

                  writeMemberships(orgWriter, bean.getId());
                  writeApplicationVersions(orgWriter, bean.getId());
                  writeServiceVersions(orgWriter, bean.getId());
                  writePlanVersions(orgWriter, bean.getId());
                  writeContracts(orgWriter, bean.getId());
                  writePolicies(orgWriter, bean.getId());
                  writeAudits(orgWriter, bean.getId());
              }

              writer.endOrgs();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writeAudits(IOrgStreamWriter orgWriter, String orgId) {
            try {
                Iterator<AuditEntryBean> iter = iStorage.getAllAuditEntries(orgId);

                orgWriter.startAudits();

                while(iter.hasNext()) {
                    AuditEntryBean bean = iter.next();
                    orgWriter.writeAudit(bean);
                }

                orgWriter.endAudits();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writePolicies(IOrgStreamWriter orgWriter, String orgId) {
            try {
                Iterator<PolicyBean> iter = iStorage.getAllPolicies(orgId);

                orgWriter.startPolicies();

                while(iter.hasNext()) {
                    PolicyBean bean = iter.next();
                    orgWriter.writePolicy(bean);
                }

                orgWriter.endPolicies();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writeContracts(IOrgStreamWriter orgWriter, String orgId) {
            try {
                Iterator<ContractBean> iter = iStorage.getContracts(orgId);

                orgWriter.startContracts();

                while(iter.hasNext()) {
                    ContractBean bean = iter.next();
                    orgWriter.writeContract(bean);
                }

                orgWriter.endContracts();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writePlanVersions(IOrgStreamWriter orgWriter, String orgId) {
            try {
                Iterator<PlanVersionBean> iter = iStorage.getAllPlanVersions(orgId);

                orgWriter.startPlanVersions();

                while(iter.hasNext()) {
                    PlanVersionBean bean = iter.next();
                    orgWriter.writePlanVersion(bean);
                }

                orgWriter.endPlanVersions();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writeServiceVersions(IOrgStreamWriter orgWriter, String orgId) {
            try {
                Iterator<ServiceVersionBean> iter = iStorage.getAllServiceVersions(orgId);

                orgWriter.startServiceVersions();

                while(iter.hasNext()) {
                    ServiceVersionBean bean = iter.next();
                    orgWriter.writeServiceVersion(bean);
                }

                orgWriter.endServiceVersions();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writeApplicationVersions(IOrgStreamWriter orgWriter, String orgId) {
            try {
                Iterator<ApplicationVersionBean> iter = iStorage.getAllApplicationVersions(orgId);

                orgWriter.startApplicationVersions();

                while(iter.hasNext()) {
                    ApplicationVersionBean bean = iter.next();
                    orgWriter.writeApplicationVersion(bean);
                }

                orgWriter.endApplicationVersions();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writeMemberships(IOrgStreamWriter orgWriter, String orgId) {
            try {
                Iterator<RoleMembershipBean> iter = iStorage.getAllMemberships(orgId);

                orgWriter.startMemberships();

                while(iter.hasNext()) {
                    RoleMembershipBean bean = iter.next();
                    orgWriter.writeMembership(bean);
                }

                orgWriter.endMemberships();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writeMetadata() {
            MetadataBean metadata = new MetadataBean();
            metadata.setApimanVersion(version.getVersionString());
            metadata.setExportedOn(new DateTime());
            writer.writeMetadata(metadata);
        }

        private void writeGateways() {
            try {
                Iterator<GatewayBean> iter = iStorage.getAllGateways();

                writer.startGateways();

                while(iter.hasNext()) {
                    GatewayBean bean = iter.next();
                    writer.writeGateways(bean);
                }

                writer.endGateways();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void writePlugins() {
            try {
                Iterator<PluginBean> iter = iStorage.getAllPlugins();

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


        private void writeUsers() {
            try {
                Iterator<UserBean> iter;

                if (orgId != null) {
                    iter = iStorage.getAllUsers(orgId);
                } else {
                    iter = iStorage.getAllUsers();
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

        private void writeRoles() {
            try {
                Iterator<RoleBean> iter;

                if (orgId != null) {
                    iter = iStorage.getAllRoles(orgId);
                } else {
                    iter = iStorage.getAllRoles();
                }

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

    }
}
