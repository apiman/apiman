/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.manager.api.migrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author eric.wittmann@gmail.com
 */
public class JsonDataMigratorWriter implements IDataMigratorWriter {

    private final OutputStream os;
    private final JsonFactory jsonFactory = new JsonFactory();
    private final JsonGenerator jg;
    private final ObjectMapper om = new ObjectMapper();
    private final Set<String> sections = new HashSet<>();

    /**
     * Constructor.
     * @param toDest
     * @throws IOException
     */
    public JsonDataMigratorWriter(File toDest) throws IOException {
        this(new FileOutputStream(toDest));
    }
    
    /**
     * Constructor.
     * @param output
     */
    public JsonDataMigratorWriter(OutputStream output) throws IOException {
        this.os = output;
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jg = jsonFactory.createGenerator(output, JsonEncoding.UTF8);
        jg.useDefaultPrettyPrinter();
        jg.setCodec(om);
        jg.writeStartObject(); // Set out the base/root object
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        jg.writeEndArray();
        jg.writeEndObject();
        jg.flush();
        IOUtils.closeQuietly(os);
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorWriter#writeMetaData(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void writeMetaData(ObjectNode node) throws IOException {
        jg.writeObjectField("Metadata", node); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorWriter#writeUser(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void writeUser(ObjectNode node) throws IOException {
        if (!sections.contains("Users")) { //$NON-NLS-1$
            jg.writeArrayFieldStart("Users"); //$NON-NLS-1$
            sections.add("Users"); //$NON-NLS-1$
        }
        jg.writeObject(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorWriter#writeGateway(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void writeGateway(ObjectNode node) throws IOException {
        if (!sections.contains("Gateways")) { //$NON-NLS-1$
            jg.writeEndArray();
            jg.writeArrayFieldStart("Gateways"); //$NON-NLS-1$
            sections.add("Gateways"); //$NON-NLS-1$
        }
        jg.writeObject(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorWriter#writePlugin(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void writePlugin(ObjectNode node) throws IOException {
        if (!sections.contains("Plugins")) { //$NON-NLS-1$
            jg.writeEndArray();
            jg.writeArrayFieldStart("Plugins"); //$NON-NLS-1$
            sections.add("Plugins"); //$NON-NLS-1$
        }
        jg.writeObject(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorWriter#writeRole(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void writeRole(ObjectNode node) throws IOException {
        if (!sections.contains("Roles")) { //$NON-NLS-1$
            jg.writeEndArray();
            jg.writeArrayFieldStart("Roles"); //$NON-NLS-1$
            sections.add("Roles"); //$NON-NLS-1$
        }
        jg.writeObject(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorWriter#writePolicyDefinition(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void writePolicyDefinition(ObjectNode node) throws IOException {
        if (!sections.contains("PolicyDefinitions")) { //$NON-NLS-1$
            jg.writeEndArray();
            jg.writeArrayFieldStart("PolicyDefinitions"); //$NON-NLS-1$
            sections.add("PolicyDefinitions"); //$NON-NLS-1$
        }
        jg.writeObject(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorWriter#writeOrg(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void writeOrg(ObjectNode node) throws IOException {
        if (!sections.contains("Orgs")) { //$NON-NLS-1$
            jg.writeEndArray();
            jg.writeArrayFieldStart("Orgs"); //$NON-NLS-1$
            sections.add("Orgs"); //$NON-NLS-1$
        }
        jg.writeObject(node);
    }

}
