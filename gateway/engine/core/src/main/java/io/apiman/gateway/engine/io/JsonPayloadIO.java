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

package io.apiman.gateway.engine.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Reads/writes JSON data, typically used for REST services.
 *
 * @author eric.wittmann@redhat.com
 */
public class JsonPayloadIO implements IPayloadIO<Map> {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Constructor.
     */
    public JsonPayloadIO() {
    }
    
    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#unmarshall(java.io.InputStream)
     */
    @Override
    public Map unmarshall(InputStream input) throws Exception {
        return mapper.readValue(input, Map.class);
    }
    
    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#unmarshall(byte[])
     */
    @Override
    public Map unmarshall(byte[] input) throws Exception {
        try (InputStream stream = new ByteArrayInputStream(input)) {
            return unmarshall(stream);
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.io.IPayloadIO#marshall(java.lang.Object)
     */
    @Override
    public byte[] marshall(Map data) throws Exception {
        String dataAsString = mapper.writeValueAsString(data);
        return dataAsString.getBytes("UTF8"); //$NON-NLS-1$
    }

}
