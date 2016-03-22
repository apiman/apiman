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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author eric.wittmann@gmail.com
 */
public class JsonDataMigratorReader implements IDataMigratorReader {

    private final JsonParser jp;
    private final InputStream in;

    /**
     * Constructor.
     * @param fromSource
     * @throws IOException
     */
    public JsonDataMigratorReader(File fromSource) throws IOException {
        this(new FileInputStream(fromSource));
    }

    /**
     * Constructor.
     * @param fromSource
     * @throws IOException
     */
    public JsonDataMigratorReader(InputStream fromSource) throws IOException {
        this.in = fromSource;
        jp = new JsonFactory().createParser(in);
        jp.setCodec(new ObjectMapper());
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        IOUtils.closeQuietly(in);
    }

    /**
     * @see io.apiman.manager.api.migrator.IDataMigratorReader#read(io.apiman.manager.api.migrator.IReaderHandler)
     */
    @Override
    public void read(IReaderHandler readerHandler) throws IOException {
        try {
            JsonToken current = jp.nextToken();

            if (current != JsonToken.START_OBJECT) {
                throw new IOException("Expected start object at root."); //$NON-NLS-1$
            }

            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String name = jp.getCurrentName();
                current = jp.nextToken();
                
                if (name.equals("Metadata")) { //$NON-NLS-1$
                    ObjectNode obj = readObjectNode();
                    readerHandler.onMetaData(obj);
                } else if (name.equals("Gateways")) { //$NON-NLS-1$
                    readArrayStart();
                    while (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                        ObjectNode obj = readObjectNode();
                        readerHandler.onGateway(obj);
                        jp.nextToken();
                    }
                } else if (name.equals("Plugins")) { //$NON-NLS-1$
                    readArrayStart();
                    while (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                        ObjectNode obj = readObjectNode();
                        readerHandler.onPlugin(obj);
                        jp.nextToken();
                    }
                } else if (name.equals("Roles")) { //$NON-NLS-1$
                    readArrayStart();
                    while (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                        ObjectNode obj = readObjectNode();
                        readerHandler.onRole(obj);
                        jp.nextToken();
                    }
                } else if (name.equals("PolicyDefinitions")) { //$NON-NLS-1$
                    readArrayStart();
                    while (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                        ObjectNode obj = readObjectNode();
                        readerHandler.onPolicyDefinition(obj);
                        jp.nextToken();
                    }
                } else if (name.equals("Users")) { //$NON-NLS-1$
                    readArrayStart();
                    while (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                        ObjectNode obj = readObjectNode();
                        readerHandler.onUser(obj);
                        jp.nextToken();
                    }
                } else if (name.equals("Orgs")) { //$NON-NLS-1$
                    readArrayStart();
                    while (jp.getCurrentToken() == JsonToken.START_OBJECT) {
                        ObjectNode obj = readObjectNode();
                        readerHandler.onOrg(obj);
                        jp.nextToken();
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void readArrayStart() throws IOException, JsonParseException {
        JsonToken token = jp.getCurrentToken();
        if (token != JsonToken.START_ARRAY) {
            throw new IOException("Unexpected token (array start expected)."); //$NON-NLS-1$
        }
        jp.nextToken();
    }
    
    /**
     * @return reads an object from the input
     * @throws IOException 
     */
    private ObjectNode readObjectNode() throws IOException {
        return (ObjectNode) jp.readValueAsTree();
    }

}
