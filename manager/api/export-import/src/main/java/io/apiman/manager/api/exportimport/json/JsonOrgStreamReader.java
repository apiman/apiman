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
package io.apiman.manager.api.exportimport.json;

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.exportimport.EntityHandler;
import io.apiman.manager.api.exportimport.OrgElementsEnum;
import io.apiman.manager.api.exportimport.read.IStreamReader;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * Read JSON in to recreate manager's state. FIFO.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class JsonOrgStreamReader extends AbstractJsonReader implements IStreamReader {

    private JsonParser jp;
    private JsonToken current;
    private OrganizationBean orgBean;
    private IStorage storage;

    public JsonOrgStreamReader(InputStream in,
            IStorage storage,
            JsonParser jp) throws JsonParseException, IOException {
        this.storage = storage;
        this.jp = jp;
    }

    public static final String ORG_BEAN_NAME = OrganizationBean.class.getSimpleName();

    @Override
    public void parse() throws Exception {
        current = jp.nextToken();

        while (jp.nextToken() != JsonToken.END_ARRAY) {
            // Traverse each domain definition
            while(jp.nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ORG_BEAN_NAME)) {
                    current = jp.nextToken();
                    orgBean = jp.readValueAs(OrganizationBean.class);
                    storage.createOrganization(orgBean);
                    System.out.println(orgBean);
                } else {
                    System.out.println(jp.getCurrentName());

                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = jp.nextToken();

                    switch (fieldName) {
                    case Memberships:
                        processEntities(RoleMembershipBean.class, new EntityHandler<RoleMembershipBean>() {
                            @Override
                            public void handleEntity(RoleMembershipBean membership) throws StorageException {
                                System.out.println("RoleMembershipBean is " + membership);
                                storage.createMembership(membership);
                            }
                        });
                        break;
                    case ApplicationVersions:
                        processEntities(ApplicationVersionBean.class, new EntityHandler<ApplicationVersionBean>() {
                           ApplicationBean app = null;

                           @Override
                           public void handleEntity(ApplicationVersionBean version) throws StorageException {
                               if (app == null) {
                                 app = version.getApplication();
                                 app.setOrganization(orgBean);
                                 storage.createApplication(app);
                               }
                               storage.createApplicationVersion(version);
                               System.out.println("ApplicationVersionBean is " + version);
                           }
                       });
                       break;
                    case ServiceVersions:
                        processEntities(ServiceVersionBean.class, new EntityHandler<ServiceVersionBean>() {
                            ServiceBean serviceBean = null;

                            @Override
                            public void handleEntity(ServiceVersionBean version) throws StorageException {
                                if (serviceBean == null) {
                                    serviceBean = version.getService();
                                    serviceBean.setOrganization(orgBean);
                                    storage.createService(serviceBean);
                                }
                                storage.createServiceVersion(version);
                                System.out.println("ServiceVersionBean is " + version);
                            }
                        });
                        break;
                    case PlanVersions:
                        processEntities(PlanVersionBean.class, new EntityHandler<PlanVersionBean>() {
                            private PlanBean plan = null;

                            @Override
                            public void handleEntity(PlanVersionBean version) throws StorageException {
                                if (plan == null) {
                                    plan = version.getPlan();
                                    plan.setOrganization(orgBean);
                                    storage.createPlan(plan);
                                }
                                storage.createPlanVersion(version);
                                System.out.println("PlanVersionBean " + plan);
                            }
                        });
                        break;
                    case Contracts:
                        processEntities(ContractBean.class, new EntityHandler<ContractBean>() {

                            @Override
                            public void handleEntity(ContractBean contract) throws StorageException {
                                System.out.println("ContractBean " + contract);
                                storage.createContract(contract);
                            }
                        });
                        break;
                    case Policies:
                        processEntities(PolicyBean.class, new EntityHandler<PolicyBean>() {
                            PolicyDefinitionBean polDef = null;

                            @Override
                            public void handleEntity(PolicyBean elem) throws StorageException {
                                if (polDef == null) {
                                    polDef = elem.getDefinition();
                                    storage.createPolicyDefinition(polDef);
                                }
                                storage.createPolicy(elem);
                                System.out.println("PolicyBean " + elem);
                            }
                        });
                        break;
                    case Audits:
                        processEntities(AuditEntryBean.class, new EntityHandler<AuditEntryBean>() {
                            @Override
                            public void handleEntity(AuditEntryBean member) throws StorageException {
                                System.out.println("AuditEntryBean is " + member);
                                storage.createAuditEntry(member);
                            }
                        });
                        break;
                    default:
                        throw new RuntimeException("Unhandled entity " + fieldName + " with token " + current);
                    }
                }
            }
        }
    }

    @Override
    protected JsonParser jsonParser() {
        return jp;
    }
 }
