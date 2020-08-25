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
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.GlobalElementsEnum;
import io.apiman.manager.api.exportimport.OrgElementsEnum;
import io.apiman.manager.api.beans.system.MetadataBean;
import io.apiman.manager.api.exportimport.write.IExportWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

/**
 * Stream global elements
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class JsonExportWriter extends AbstractJsonWriter<GlobalElementsEnum> implements IExportWriter {

    private JsonFactory jsonFactory = new JsonFactory();
    private JsonGenerator jg;
    private Map<Enum<GlobalElementsEnum>, Boolean> finished = new HashMap<>();
    private ObjectMapper om = new ObjectMapper();

    {
        om.setDateFormat(new ISO8601DateFormat());
        for (GlobalElementsEnum v : GlobalElementsEnum.values()) {
            finished.put(v, false);
        }
    }

    /**
     * Constructor.
     * @param targetStream
     * @param logger
     * @throws IOException
     */
    public JsonExportWriter(OutputStream targetStream, IApimanLogger logger) throws IOException {
        super(logger);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jg = jsonFactory.createGenerator(targetStream, JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        jg.setCodec(om);
        jg.writeStartObject(); // Set out the base/root object
    }

    @Override
    protected JsonGenerator jsonGenerator() {
        return jg;
    }

    @Override
    protected Map<Enum<GlobalElementsEnum>, Boolean> finished() {
        return finished;
    }

    @Override
    public IExportWriter writeMetadata(MetadataBean metadata) {
        validityCheckStart(GlobalElementsEnum.Metadata);
        writePojo(GlobalElementsEnum.Metadata, metadata);
        return this;
    }

    @Override
    public IExportWriter startGateways() {
        validityCheckStart(GlobalElementsEnum.Gateways);
        lock(GlobalElementsEnum.Gateways);
        writeStartArray(GlobalElementsEnum.Gateways);
        return this;
    }

    @Override
    public IExportWriter writeGateway(GatewayBean gb) {
        writeCheck(GlobalElementsEnum.Gateways);
        writePojo(gb);
        return this;
    }

    @Override
    public IExportWriter endGateways() {
        validityCheckEnd(GlobalElementsEnum.Gateways);
        writeEndArray(GlobalElementsEnum.Gateways);
        unlock(GlobalElementsEnum.Gateways);
        return this;
    }

    @Override
    public IExportWriter startPlugins() {
        validityCheckStart(GlobalElementsEnum.Plugins);
        lock(GlobalElementsEnum.Plugins);
        writeStartArray(GlobalElementsEnum.Plugins);
        return this;
    }

    @Override
    public IExportWriter writePlugin(PluginBean pb) {
        writeCheck(GlobalElementsEnum.Plugins);
        writePojo(pb);
        return this;
    }

    @Override
    public IExportWriter endPlugins() {
        validityCheckEnd(GlobalElementsEnum.Plugins);
        writeEndArray(GlobalElementsEnum.Plugins);
        unlock(GlobalElementsEnum.Plugins);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startPolicyDefs()
     */
    @Override
    public IExportWriter startPolicyDefs() {
        validityCheckStart(GlobalElementsEnum.PolicyDefinitions);
        lock(GlobalElementsEnum.PolicyDefinitions);
        writeStartArray(GlobalElementsEnum.PolicyDefinitions);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#writePolicyDef(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public IExportWriter writePolicyDef(PolicyDefinitionBean policyDef) {
        writeCheck(GlobalElementsEnum.PolicyDefinitions);
        writePojo(policyDef);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endPolicyDefs()
     */
    @Override
    public IExportWriter endPolicyDefs() {
        validityCheckEnd(GlobalElementsEnum.PolicyDefinitions);
        writeEndArray(GlobalElementsEnum.PolicyDefinitions);
        unlock(GlobalElementsEnum.PolicyDefinitions);
        return this;
    }

    @Override
    public IExportWriter startUsers() {
        validityCheckStart(GlobalElementsEnum.Users);
        lock(GlobalElementsEnum.Users);
        writeStartArray(GlobalElementsEnum.Users);
        return this;
    }

    @Override
    public IExportWriter writeUser(UserBean user) {
        writeCheck(GlobalElementsEnum.Users);
        writePojo(user);
        return this;
    }

    @Override
    public IExportWriter endUsers() {
        validityCheckEnd(GlobalElementsEnum.Users);
        writeEndArray(GlobalElementsEnum.Users);
        unlock(GlobalElementsEnum.Users);
        return this;
    }

    @Override
    public IExportWriter startRoles() {
        validityCheckStart(GlobalElementsEnum.Roles);
        lock(GlobalElementsEnum.Roles);
        writeStartArray(GlobalElementsEnum.Roles);
        return this;
    }

    @Override
    public IExportWriter writeRole(RoleBean role) {
        writeCheck(GlobalElementsEnum.Roles);
        writePojo(role);
        return this;
    }

    @Override
    public IExportWriter endRoles() {
        validityCheckEnd(GlobalElementsEnum.Roles);
        writeEndArray(GlobalElementsEnum.Roles);
        unlock(GlobalElementsEnum.Roles);
        return this;
    }

    @Override
    public IExportWriter startOrgs() {
        validityCheckStart(GlobalElementsEnum.Orgs);
        lock(GlobalElementsEnum.Orgs);
        writeStartArray(GlobalElementsEnum.Orgs);
        return this;
    }

    @Override
    public IExportWriter startOrg(OrganizationBean org) {
        writeStartObject();
        try {
            jg.writeObjectField(OrganizationBean.class.getSimpleName(), org);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public IExportWriter startMemberships() {
        writeStartArray(OrgElementsEnum.Memberships);
        return this;
    }

    @Override
    public IExportWriter writeMembership(RoleMembershipBean membership) {
        writePojo(membership);
        return this;
    }

    @Override
    public IExportWriter endMemberships() {
        writeEndArray(OrgElementsEnum.Memberships);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startPlans()
     */
    @Override
    public IExportWriter startPlans() {
        writeStartArray(OrgElementsEnum.Plans);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startPlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public IExportWriter startPlan(PlanBean plan) {
        writeStartObject();
        try {
            plan = (PlanBean) plan.clone();
            plan.setOrganization(null);
            jg.writeObjectField(PlanBean.class.getSimpleName(), plan);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startPlanVersions()
     */
    @Override
    public IExportWriter startPlanVersions() {
        writeStartArray(OrgElementsEnum.Versions);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startPlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public IExportWriter startPlanVersion(PlanVersionBean pvb) {
        writeStartObject();
        try {
            pvb = (PlanVersionBean) pvb.clone();
            pvb.setPlan(null);
            jg.writeObjectField(PlanVersionBean.class.getSimpleName(), pvb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startPlanPolicies()
     */
    @Override
    public IExportWriter startPlanPolicies() {
        writeStartArray(OrgElementsEnum.Policies);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#writePlanPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public IExportWriter writePlanPolicy(PolicyBean policy) {
        return writePolicy(policy);
    }

    /**
     * @param policy
     * @return
     */
    private IExportWriter writePolicy(PolicyBean policy) {
        try {
            policy = (PolicyBean) policy.clone();
            PolicyDefinitionBean definition = new PolicyDefinitionBean();
            definition.setId(policy.getDefinition().getId());
            policy.setDefinition(definition);
            writePojo(policy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endPlanPolicies()
     */
    @Override
    public IExportWriter endPlanPolicies() {
        writeEndArray(OrgElementsEnum.Policies);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endPlanVersion()
     */
    @Override
    public IExportWriter endPlanVersion() {
        writeEndObject();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endPlanVersions()
     */
    @Override
    public IExportWriter endPlanVersions() {
        writeEndArray();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endPlan()
     */
    @Override
    public IExportWriter endPlan() {
        writeEndObject();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endPlans()
     */
    @Override
    public IExportWriter endPlans() {
        writeEndArray(OrgElementsEnum.Plans);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startApis()
     */
    @Override
    public IExportWriter startApis() {
        writeStartArray(OrgElementsEnum.Apis);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public IExportWriter startApi(ApiBean api) {
        writeStartObject();
        try {
            api = (ApiBean) api.clone();
            api.setOrganization(null);
            jg.writeObjectField(ApiBean.class.getSimpleName(), api);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startApiVersions()
     */
    @Override
    public IExportWriter startApiVersions() {
        writeStartArray(OrgElementsEnum.Versions);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public IExportWriter startApiVersion(ApiVersionBean avb) {
        writeStartObject();
        try {
            avb = (ApiVersionBean) avb.clone();
            avb.setApi(null);
            jg.writeObjectField(ApiVersionBean.class.getSimpleName(), avb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startApiPolicies()
     */
    @Override
    public IExportWriter startApiPolicies() {
        writeStartArray(OrgElementsEnum.Policies);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#writeApiPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public IExportWriter writeApiPolicy(PolicyBean policy) {
        return writePolicy(policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endApiPolicies()
     */
    @Override
    public IExportWriter endApiPolicies() {
        writeEndArray(OrgElementsEnum.Policies);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endApiVersion()
     */
    @Override
    public IExportWriter endApiVersion() {
        writeEndObject();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endApiVersions()
     */
    @Override
    public IExportWriter endApiVersions() {
        writeEndArray();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endApi()
     */
    @Override
    public IExportWriter endApi() {
        writeEndObject();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endApis()
     */
    @Override
    public IExportWriter endApis() {
        writeEndArray(OrgElementsEnum.Apis);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startClients()
     */
    @Override
    public IExportWriter startClients() {
        writeStartArray(OrgElementsEnum.Clients);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startClient(io.apiman.manager.api.beans.ClientBean.ClientBean)
     */
    @Override
    public IExportWriter startClient(ClientBean client) {
        writeStartObject();
        try {
            client = (ClientBean) client.clone();
            client.setOrganization(null);
            jg.writeObjectField(ClientBean.class.getSimpleName(), client);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startClientVersions()
     */
    @Override
    public IExportWriter startClientVersions() {
        writeStartArray(OrgElementsEnum.Versions);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startClientVersion(io.apiman.manager.api.beans.ClientVersionBean.ClientVersionBean)
     */
    @Override
    public IExportWriter startClientVersion(ClientVersionBean cvb) {
        writeStartObject();
        try {
            cvb = (ClientVersionBean) cvb.clone();
            cvb.setClient(null);
            jg.writeObjectField(ClientVersionBean.class.getSimpleName(), cvb);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startClientPolicies()
     */
    @Override
    public IExportWriter startClientPolicies() {
        writeStartArray(OrgElementsEnum.Policies);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#writeClientPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public IExportWriter writeClientPolicy(PolicyBean policy) {
        return writePolicy(policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endClientPolicies()
     */
    @Override
    public IExportWriter endClientPolicies() {
        writeEndArray(OrgElementsEnum.Policies);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startClientContracts()
     */
    @Override
    public IExportWriter startClientContracts() {
        writeStartArray(OrgElementsEnum.Contracts);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#writeClientContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public IExportWriter writeClientContract(ContractBean cb) {
        writePojo(cb);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endClientContracts()
     */
    @Override
    public IExportWriter endClientContracts() {
        writeEndArray();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endClientVersion()
     */
    @Override
    public IExportWriter endClientVersion() {
        writeEndObject();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endClientVersions()
     */
    @Override
    public IExportWriter endClientVersions() {
        writeEndArray();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endClient()
     */
    @Override
    public IExportWriter endClient() {
        writeEndObject();
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endClients()
     */
    @Override
    public IExportWriter endClients() {
        writeEndArray(OrgElementsEnum.Clients);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#startAudits()
     */
    @Override
    public IExportWriter startAudits() {
        writeStartArray(OrgElementsEnum.Audits);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#writeAudit(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public IExportWriter writeAudit(AuditEntryBean ab) {
        writePojo(ab);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endAudits()
     */
    @Override
    public IExportWriter endAudits() {
        writeEndArray(OrgElementsEnum.Audits);
        return this;
    }

    /**
     * @see io.apiman.manager.api.exportimport.write.IExportWriter#endOrg()
     */
    @Override
    public IExportWriter endOrg() {
        writeEndObject();
        return this;
    }

    @Override
    public IExportWriter endOrgs() {
        validityCheckEnd(GlobalElementsEnum.Orgs);
        writeEndArray();
        unlock(GlobalElementsEnum.Orgs);
        return this;
    }

    @Override
    public void close() {
        try {
            writeEndObject();
            jg.flush();
            jg.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
