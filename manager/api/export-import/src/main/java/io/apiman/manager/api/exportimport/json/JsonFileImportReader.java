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
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.exportimport.EntityHandler;
import io.apiman.manager.api.exportimport.GlobalElementsEnum;
import io.apiman.manager.api.exportimport.OrgElementsEnum;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.read.IImportReader;
import io.apiman.manager.api.exportimport.read.IImportReaderDispatcher;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Read JSON in to recreate manager's state. FIFO.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class JsonFileImportReader extends AbstractJsonReader implements IImportReader {
    
    private IImportReaderDispatcher dispatcher;
    private JsonParser jp;
    private JsonToken current;
    private InputStream in;
    
    /**
     * Constructor.
     * @param in
     * @throws JsonParseException
     * @throws IOException
     */
    public JsonFileImportReader(InputStream in) throws JsonParseException, IOException {
        this.in = in;
        jp = new JsonFactory().createJsonParser(in);
        jp.setCodec(new ObjectMapper());
    }
    
    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReader#setDispatcher(io.apiman.manager.api.exportimport.read.IImportReaderDispatcher)
     */
    @Override
    public void setDispatcher(IImportReaderDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void read() throws Exception {
        try {
            current = nextToken();
    
            if (current != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Expected start object at root");
            }
    
            while (nextToken() != JsonToken.END_OBJECT) {
                GlobalElementsEnum fieldName = GlobalElementsEnum.valueOf(jp.getCurrentName());
                current = nextToken();
    
                switch(fieldName) {
                case Metadata:
                    processEntity(MetadataBean.class, new EntityHandler<MetadataBean>() {
                        @Override
                        public void handleEntity(MetadataBean metadata) throws Exception {
                            dispatcher.metadata(metadata);
                        }
                    });
                    break;
                case Gateways:
                    processEntities(GatewayBean.class, new EntityHandler<GatewayBean>() {
                        @Override
                        public void handleEntity(GatewayBean gateway) throws Exception {
                            dispatcher.gateway(gateway);
                        }
                    });
                    break;
                case Plugins:
                    processEntities(PluginBean.class, new EntityHandler<PluginBean>() {
                        @Override
                        public void handleEntity(PluginBean plugin) throws Exception {
                            dispatcher.plugin(plugin);
                        }
                    });
                    break;
                case Roles:
                    processEntities(RoleBean.class, new EntityHandler<RoleBean>() {
                        @Override
                        public void handleEntity(RoleBean role) throws Exception {
                            dispatcher.role(role);
                        }
                    });
                    break;
                case PolicyDefinitions:
                    processEntities(PolicyDefinitionBean.class, new EntityHandler<PolicyDefinitionBean>() {
                        @Override
                        public void handleEntity(PolicyDefinitionBean policyDef) throws Exception {
                            dispatcher.policyDef(policyDef);
                        }
                    });
                    break;
                case Users:
                    processEntities(UserBean.class, new EntityHandler<UserBean>() {
                        @Override
                        public void handleEntity(UserBean user) throws Exception {
                            dispatcher.user(user);
                        }
                    });
                    break;
                case Orgs:
                    readOrgs();
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled field: " + fieldName);
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
    
    
    public void readOrgs() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each org definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(OrganizationBean.class.getSimpleName())) {
                    current = nextToken();
                    OrganizationBean orgBean = jp.readValueAs(OrganizationBean.class);
                    dispatcher.organization(orgBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();

                    switch (fieldName) {
                    case Memberships:
                        processEntities(RoleMembershipBean.class, new EntityHandler<RoleMembershipBean>() {
                            @Override
                            public void handleEntity(RoleMembershipBean membership) throws StorageException {
                                dispatcher.membership(membership);
                            }
                        });
                        break;
                    case Plans:
                        readPlans();
                        break;
                    case Services:
                        readServices();
                        break;
                    case Apps:
                        readApps();
                        break;
                    case Audits:
                        processEntities(AuditEntryBean.class, new EntityHandler<AuditEntryBean>() {
                            @Override
                            public void handleEntity(AuditEntryBean entry) throws StorageException {
                                dispatcher.audit(entry);
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
    
    public void readPlans() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each plan definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(PlanBean.class.getSimpleName())) {
                    current = nextToken();
                    PlanBean planBean = jp.readValueAs(PlanBean.class);
                    dispatcher.plan(planBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Versions:
                        readPlanVersions();
                        break;
                    default:
                        throw new RuntimeException("Unhandled entity " + fieldName + " with token " + current);
                    }
                }
            }
        }
    }

    public void readServices() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each service definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ServiceBean.class.getSimpleName())) {
                    current = nextToken();
                    ServiceBean serviceBean = jp.readValueAs(ServiceBean.class);
                    dispatcher.service(serviceBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Versions:
                        readServiceVersions();
                        break;
                    default:
                        throw new RuntimeException("Unhandled entity " + fieldName + " with token " + current);
                    }
                }
            }
        }
    }

    public void readApps() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each service definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ApplicationBean.class.getSimpleName())) {
                    current = nextToken();
                    ApplicationBean serviceBean = jp.readValueAs(ApplicationBean.class);
                    dispatcher.application(serviceBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Versions:
                        readApplicationVersions();
                        break;
                    default:
                        throw new RuntimeException("Unhandled entity " + fieldName + " with token " + current);
                    }
                }
            }
        }
    }

    public void readPlanVersions() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each plan definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(PlanVersionBean.class.getSimpleName())) {
                    current = nextToken();
                    PlanVersionBean planBean = jp.readValueAs(PlanVersionBean.class);
                    dispatcher.planVersion(planBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Policies:
                        processEntities(PolicyBean.class, new EntityHandler<PolicyBean>() {
                            @Override
                            public void handleEntity(PolicyBean policy) throws Exception {
                                dispatcher.planPolicy(policy);
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
    
    public void readServiceVersions() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each service definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ServiceVersionBean.class.getSimpleName())) {
                    current = nextToken();
                    ServiceVersionBean serviceBean = jp.readValueAs(ServiceVersionBean.class);
                    dispatcher.serviceVersion(serviceBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Policies:
                        processEntities(PolicyBean.class, new EntityHandler<PolicyBean>() {
                            @Override
                            public void handleEntity(PolicyBean policy) throws Exception {
                                dispatcher.servicePolicy(policy);
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

    public void readApplicationVersions() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each application definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ApplicationVersionBean.class.getSimpleName())) {
                    current = nextToken();
                    ApplicationVersionBean applicationBean = jp.readValueAs(ApplicationVersionBean.class);
                    dispatcher.applicationVersion(applicationBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Policies:
                        processEntities(PolicyBean.class, new EntityHandler<PolicyBean>() {
                            @Override
                            public void handleEntity(PolicyBean policy) throws Exception {
                                dispatcher.applicationPolicy(policy);
                            }
                        });
                        break;
                    case Contracts:
                        processEntities(ContractBean.class, new EntityHandler<ContractBean>() {
                            @Override
                            public void handleEntity(ContractBean contract) throws Exception {
                                dispatcher.applicationContract(contract);
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
