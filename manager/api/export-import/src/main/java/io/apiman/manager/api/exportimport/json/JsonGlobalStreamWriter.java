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
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.exportimport.GlobalElementsEnum;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.write.IGlobalStreamWriter;
import io.apiman.manager.api.exportimport.write.IOrgStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Stream global elements
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class JsonGlobalStreamWriter extends AbstractJsonWriter<GlobalElementsEnum> implements IGlobalStreamWriter {

    private JsonFactory jsonFactory = new JsonFactory();
    private JsonGenerator jg;
    private Map<Enum<GlobalElementsEnum>, Boolean> finished = new HashMap<>();
    private ObjectMapper om = new ObjectMapper();
    private JsonOrgStreamWriter orgWriter;

    {
        for (GlobalElementsEnum v : GlobalElementsEnum.values()) {
            finished.put(v, false);
        }
    }

    abstract class ExcludeOrganizationMixin {
        @JsonIgnore abstract OrganizationBean getOrganization();
    }

    private void setupIgnores() {
        om.getSerializationConfig().addMixInAnnotations(ApplicationBean.class, ExcludeOrganizationMixin.class);
        om.getSerializationConfig().addMixInAnnotations(ServiceBean.class, ExcludeOrganizationMixin.class);
        om.getSerializationConfig().addMixInAnnotations(PlanBean.class, ExcludeOrganizationMixin.class);
    }

    public JsonGlobalStreamWriter(OutputStream targetFile,
            IStorage istorage) throws IOException {
        jg = jsonFactory.createJsonGenerator(targetFile, JsonEncoding.UTF8);
        jg.setCodec(om);
        jg.writeStartObject(); // Set out the base array
        orgWriter = new JsonOrgStreamWriter(jg, this);
        setupIgnores();
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
    public IGlobalStreamWriter writeMetadata(MetadataBean metadata) {
        validityCheckStart(GlobalElementsEnum.Metadata);
        writePojo(GlobalElementsEnum.Metadata, metadata);
        return this;
    }

    @Override
    public IGlobalStreamWriter startGateways() {
        validityCheckStart(GlobalElementsEnum.Gateways);
        lock(GlobalElementsEnum.Gateways);
        writeStartArray(GlobalElementsEnum.Gateways);
        return this;
    }

    @Override
    public IGlobalStreamWriter writeGateways(GatewayBean gb) {
        writeCheck(GlobalElementsEnum.Gateways);
        writePojo(gb);
        return this;
    }

    @Override
    public IGlobalStreamWriter endGateways() {
        validityCheckEnd(GlobalElementsEnum.Gateways);
        writeEndArray(GlobalElementsEnum.Gateways);
        unlock(GlobalElementsEnum.Gateways);
        return this;
    }

    @Override
    public IGlobalStreamWriter startPlugins() {
        validityCheckStart(GlobalElementsEnum.Plugins);
        lock(GlobalElementsEnum.Plugins);
        writeStartArray(GlobalElementsEnum.Plugins);
        return this;
    }

    @Override
    public IGlobalStreamWriter writePlugin(PluginBean pb) {
        writeCheck(GlobalElementsEnum.Plugins);
        writePojo(pb);
        return this;
    }

    @Override
    public IGlobalStreamWriter endPlugins() {
        validityCheckEnd(GlobalElementsEnum.Plugins);
        writeEndArray(GlobalElementsEnum.Plugins);
        unlock(GlobalElementsEnum.Plugins);
        return this;
    }

    @Override
    public IGlobalStreamWriter startUsers() {
        validityCheckStart(GlobalElementsEnum.Users);
        lock(GlobalElementsEnum.Users);
        writeStartArray(GlobalElementsEnum.Users);
        return this;
    }

    @Override
    public IGlobalStreamWriter writeUser(UserBean user) {
        writeCheck(GlobalElementsEnum.Users);
        writePojo(user);
        return this;
    }

    @Override
    public IGlobalStreamWriter endUsers() {
        validityCheckEnd(GlobalElementsEnum.Users);
        writeEndArray(GlobalElementsEnum.Users);
        unlock(GlobalElementsEnum.Users);
        return this;
    }

    @Override
    public IGlobalStreamWriter startRoles() {
        validityCheckStart(GlobalElementsEnum.Roles);
        lock(GlobalElementsEnum.Roles);
        writeStartArray(GlobalElementsEnum.Roles);
        return this;
    }

    @Override
    public IGlobalStreamWriter writeRole(RoleBean role) {
        writeCheck(GlobalElementsEnum.Roles);
        writePojo(role);
        return this;
    }

    @Override
    public IGlobalStreamWriter endRoles() {
        validityCheckEnd(GlobalElementsEnum.Roles);
        writeEndArray(GlobalElementsEnum.Roles);
        unlock(GlobalElementsEnum.Roles);
        return this;
    }

    @Override
    public IGlobalStreamWriter startOrgs() {
        validityCheckStart(GlobalElementsEnum.Orgs);
        lock(GlobalElementsEnum.Orgs);
        writeStartArray(GlobalElementsEnum.Orgs);
        return this;
    }

    @Override
    public IOrgStreamWriter writeOrg(OrganizationBean org) {
        try {
            writeStartObject();
            return orgWriter.start(org);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IGlobalStreamWriter endOrgs() {
        orgWriter.close();
        validityCheckEnd(GlobalElementsEnum.Orgs);
        writeEndObject();
        unlock(GlobalElementsEnum.Orgs);
        return this;
    }

    @Override
    public void close() {
        try {
            writeEndArray();
            writeEndObject();
            jg.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
