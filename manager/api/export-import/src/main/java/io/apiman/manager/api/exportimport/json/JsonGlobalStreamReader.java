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

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.exportimport.EntityHandler;
import io.apiman.manager.api.exportimport.GlobalElementsEnum;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.read.IStreamReader;

import java.io.IOException;
import java.io.InputStream;

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
public class JsonGlobalStreamReader extends AbstractJsonReader implements IStreamReader {

    private JsonParser jp;
    private JsonToken current;
    private IStorage storage;
    private InputStream in;

    public JsonGlobalStreamReader(InputStream in,
            IStorage storage) throws JsonParseException, IOException {
        this.storage = storage;
        this.in = in;
        jp = new JsonFactory().createJsonParser(in);
        jp.setCodec(new ObjectMapper());
    }

    @Override
    public void parse() throws Exception {
        current = jp.nextToken();

        if (current != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected start object at root");
        }

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            GlobalElementsEnum fieldName = GlobalElementsEnum.valueOf(jp.getCurrentName());
            current = jp.nextToken();

            switch(fieldName) {
            case Metadata:
                processEntity(MetadataBean.class, new EntityHandler<MetadataBean>() {
                    @Override
                    public void handleEntity(MetadataBean metadata) throws Exception {
                        System.out.println("Metadata " + metadata);
                    }
                });
                break;
            case Gateways:
                processEntities(GatewayBean.class, new EntityHandler<GatewayBean>() {
                    @Override
                    public void handleEntity(GatewayBean gateway) throws Exception {
                        System.out.println("GatewayBean " + gateway);
                        storage.createGateway(gateway);
                    }
                });
                break;
            case Plugins:
                processEntities(PluginBean.class, new EntityHandler<PluginBean>() {
                    @Override
                    public void handleEntity(PluginBean plugin) throws Exception {
                        System.out.println("PluginBean " + plugin);
                        storage.createPlugin(plugin);
                    }
                });
                break;
            case Roles:
                processEntities(RoleBean.class, new EntityHandler<RoleBean>() {
                    @Override
                    public void handleEntity(RoleBean role) throws Exception {
                        System.out.println("RoleBean " + role);
                        storage.createRole(role);
                    }
                });
                break;
            case Users:
                processEntities(UserBean.class, new EntityHandler<UserBean>() {
                    @Override
                    public void handleEntity(UserBean user) throws Exception {
                        System.out.println("UserBean " + user);
                        storage.createUser(user);
                    }
                });
                break;
            case Orgs:
                new JsonOrgStreamReader(in, storage, jp).parse();
                break;
            default:
                throw new IllegalArgumentException("Unhandled field: " + fieldName);
            }
        }
    }

    @Override
    protected JsonParser jsonParser() {
        return jp;
    }

}
