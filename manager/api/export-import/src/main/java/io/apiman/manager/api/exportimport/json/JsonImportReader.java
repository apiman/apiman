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
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.EntityHandler;
import io.apiman.manager.api.exportimport.GlobalElementsEnum;
import io.apiman.manager.api.exportimport.OrgElementsEnum;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.read.IImportReader;
import io.apiman.manager.api.exportimport.read.IImportReaderDispatcher;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Read JSON in to recreate manager's state. FIFO.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class JsonImportReader extends AbstractJsonReader implements IImportReader {

    private IApimanLogger logger;
    private IImportReaderDispatcher dispatcher;
    private JsonParser jp;
    private JsonToken current;
    private InputStream in;

    /**
     * Constructor.
     * @param logger the apiman logger
     * @param in the input stream
     * @throws JsonParseException
     * @throws IOException
     */
    public JsonImportReader(IApimanLogger logger, InputStream in) throws IOException {
        this.logger = logger;
        this.in = in;
        jp = new JsonFactory().createParser(in);
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
                    processEntity(MetadataBean.class, dispatcher::metadata);
                    break;
                case Gateways:
                    processEntities(GatewayBean.class, dispatcher::gateway);
                    break;
                case Plugins:
                    processEntities(PluginBean.class, dispatcher::plugin);
                    break;
                case Roles:
                    processEntities(RoleBean.class, dispatcher::role);
                    break;
                case PolicyDefinitions:
                    processEntities(PolicyDefinitionBean.class, dispatcher::policyDef);
                    break;
                case Users:
                    processEntities(UserBean.class, dispatcher::user);
                    break;
                case Orgs:
                    readOrgs();
                    break;
                default:
                    throw new IllegalArgumentException("Unhandled field: " + fieldName);
                }
            }

            dispatcher.close();
        } catch (Throwable t) {
            logger.error(t);
            dispatcher.cancel();
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
                        processEntities(RoleMembershipBean.class, dispatcher::membership);
                        break;
                    case Plans:
                        readPlans();
                        break;
                    case Apis:
                        readApis();
                        break;
                    case Clients:
                        readClients();
                        break;
                    case Audits:
                        processEntities(AuditEntryBean.class, dispatcher::audit);
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

    public void readApis() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each api definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ApiBean.class.getSimpleName())) {
                    current = nextToken();
                    ApiBean apiBean = jp.readValueAs(ApiBean.class);
                    dispatcher.api(apiBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Versions:
                        readApiVersions();
                        break;
                    default:
                        throw new RuntimeException("Unhandled entity " + fieldName + " with token " + current);
                    }
                }
            }
        }
    }

    public void readClients() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each api definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ClientBean.class.getSimpleName())) {
                    current = nextToken();
                    ClientBean apiBean = jp.readValueAs(ClientBean.class);
                    dispatcher.client(apiBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Versions:
                        readClientVersions();
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

    public void readApiVersions() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each api definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ApiVersionBean.class.getSimpleName())) {
                    current = nextToken();
                    ApiVersionBean apiBean = jp.readValueAs(ApiVersionBean.class);
                    dispatcher.apiVersion(apiBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Policies:
                        processEntities(PolicyBean.class, dispatcher::apiPolicy);
                        break;
                    default:
                        throw new RuntimeException("Unhandled entity " + fieldName + " with token " + current);
                    }
                }
            }
        }
    }

    public void readClientVersions() throws Exception {
        current = nextToken();
        if (current == JsonToken.END_ARRAY) {
            return;
        }
        while (nextToken() != JsonToken.END_ARRAY) {
            // Traverse each client definition
            while(nextToken() != JsonToken.END_OBJECT) {
                if (jp.getCurrentName().equals(ClientVersionBean.class.getSimpleName())) {
                    current = nextToken();
                    ClientVersionBean clientBean = jp.readValueAs(ClientVersionBean.class);
                    dispatcher.clientVersion(clientBean);
                } else {
                    OrgElementsEnum fieldName = OrgElementsEnum.valueOf(jp.getCurrentName());
                    current = nextToken();
                    switch (fieldName) {
                    case Policies:
                        processEntities(PolicyBean.class, dispatcher::clientPolicy);
                        break;
                    case Contracts:
                        processEntities(ContractBean.class, dispatcher::clientContract);
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
