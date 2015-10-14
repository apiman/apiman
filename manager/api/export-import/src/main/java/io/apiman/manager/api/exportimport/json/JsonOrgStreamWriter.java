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

import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.exportimport.OrgElementsEnum;
import io.apiman.manager.api.exportimport.write.IOrgStreamWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;

/**
 * Stream out to a JSON file
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class JsonOrgStreamWriter extends AbstractJsonWriter<OrgElementsEnum> implements IOrgStreamWriter {

    private JsonGenerator jg;
    private Map<Enum<OrgElementsEnum>, Boolean> finished = new HashMap<>();
    boolean ended = false;

    {
        for (OrgElementsEnum v : OrgElementsEnum.values()) {
            finished.put(v, false);
        }
    }

    public JsonOrgStreamWriter(JsonGenerator jg) throws IOException {
        this.jg = jg;
    }

    IOrgStreamWriter start(OrganizationBean org) throws JsonGenerationException, IOException {
        jg.writeObjectField(OrganizationBean.class.getSimpleName(), org);
        return this;
    }

    @Override
    public IOrgStreamWriter startMemberships() {
        validityCheckStart(OrgElementsEnum.Memberships);
        lock(OrgElementsEnum.Memberships);
        writeStartArray(OrgElementsEnum.Memberships);
        return this;
    }

    @Override
    public IOrgStreamWriter writeMembership(RoleMembershipBean membership) {
        writeCheck(OrgElementsEnum.Memberships);
        writePojo(membership);
        return this;
    }

    @Override
    public IOrgStreamWriter endMemberships() {
        validityCheckEnd(OrgElementsEnum.Memberships);
        writeEndArray(OrgElementsEnum.Memberships);
        unlock(OrgElementsEnum.Memberships);
        return this;
    }

    @Override
    public IOrgStreamWriter startServiceVersions() {
        validityCheckStart(OrgElementsEnum.ServiceVersions);
        lock(OrgElementsEnum.ServiceVersions);
        writeStartArray(OrgElementsEnum.ServiceVersions);
        return this;
    }

    @Override
    public IOrgStreamWriter writeServiceVersion(ServiceVersionBean svb) {
        writeCheck(OrgElementsEnum.ServiceVersions);
        writePojo(svb);
        return this;
    }

    @Override
    public IOrgStreamWriter endServiceVersions() {
        validityCheckEnd(OrgElementsEnum.ServiceVersions);
        writeEndArray(OrgElementsEnum.ServiceVersions);
        unlock(OrgElementsEnum.ServiceVersions);
        return this;
    }

    @Override
    public IOrgStreamWriter startApplicationVersions() {
        validityCheckStart(OrgElementsEnum.ApplicationVersions);
        lock(OrgElementsEnum.ApplicationVersions);
        writeStartArray(OrgElementsEnum.ApplicationVersions);
        return this;
    }

    @Override
    public IOrgStreamWriter writeApplicationVersion(ApplicationVersionBean avb) {
        writeCheck(OrgElementsEnum.ApplicationVersions);
        writePojo(avb);
        return this;
    }

    @Override
    public IOrgStreamWriter endApplicationVersions() {
        validityCheckEnd(OrgElementsEnum.ApplicationVersions);
        writeEndArray(OrgElementsEnum.ApplicationVersions);
        unlock(OrgElementsEnum.ApplicationVersions);
        return this;
    }

    @Override
    public IOrgStreamWriter startPlanVersions() {
        validityCheckStart(OrgElementsEnum.PlanVersions);
        writeStartArray(OrgElementsEnum.PlanVersions);
        lock(OrgElementsEnum.PlanVersions);
        return this;
    }

    @Override
    public IOrgStreamWriter writePlanVersion(PlanVersionBean pvb) {
        writeCheck(OrgElementsEnum.PlanVersions);
        writePojo(pvb);
        return this;
    }

    @Override
    public IOrgStreamWriter endPlanVersions() {
        validityCheckEnd(OrgElementsEnum.PlanVersions);
        writeEndArray(OrgElementsEnum.PlanVersions);
        unlock(OrgElementsEnum.PlanVersions);
        return this;
    }

    @Override
    public IOrgStreamWriter startContracts() {
        validityCheckStart(OrgElementsEnum.Contracts);
        lock(OrgElementsEnum.Contracts);
        writeStartArray(OrgElementsEnum.Contracts);
        return this;
    }

    @Override
    public IOrgStreamWriter writeContract(ContractBean cb) {
        writeCheck(OrgElementsEnum.Contracts);
        writePojo(cb);
        return this;
    }

    @Override
    public IOrgStreamWriter endContracts() {
        validityCheckEnd(OrgElementsEnum.Contracts);
        writeEndArray(OrgElementsEnum.Contracts);
        unlock(OrgElementsEnum.Contracts);
        return this;
    }

    @Override
    public IOrgStreamWriter startPolicies() {
        validityCheckStart(OrgElementsEnum.Policies);
        lock(OrgElementsEnum.Policies);
        writeStartArray(OrgElementsEnum.Policies);
        return this;
    }

    @Override
    public IOrgStreamWriter writePolicy(PolicyBean pb) {
        writeCheck(OrgElementsEnum.Policies);
        writePojo(pb);
        return this;
    }

    @Override
    public IOrgStreamWriter endPolicies() {
        validityCheckEnd(OrgElementsEnum.Policies);
        writeEndArray(OrgElementsEnum.Policies);
        unlock(OrgElementsEnum.Policies);
        return this;
    }

    @Override
    public IOrgStreamWriter startAudits() {
        validityCheckStart(OrgElementsEnum.Audits);
        lock(OrgElementsEnum.Audits);
        writeStartArray(OrgElementsEnum.Audits);
        return this;
    }

    @Override
    public IOrgStreamWriter writeAudit(AuditEntryBean ab) {
        writeCheck(OrgElementsEnum.Audits);
        writePojo(ab);
        return this;
    }

    @Override
    public IOrgStreamWriter endAudits() {
        validityCheckEnd(OrgElementsEnum.Audits);
        writeEndArray(OrgElementsEnum.Audits);
        unlock(OrgElementsEnum.Audits);
        return this;
    }

    void close() {
        ended = true;
    }

    @Override
    protected JsonGenerator jsonGenerator() {
        return jg;
    }

    @Override
    protected Map<Enum<OrgElementsEnum>, Boolean> finished() {
        return finished;
    }

}
