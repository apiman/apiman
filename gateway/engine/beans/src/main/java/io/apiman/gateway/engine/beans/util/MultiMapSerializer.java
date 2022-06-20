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

package io.apiman.gateway.engine.beans.util;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author eric.wittmann@gmail.com
 */
public class MultiMapSerializer extends JsonSerializer<CaseInsensitiveStringMultiMap> {

    /**
     * Constructor.
     */
    public MultiMapSerializer() {
    }

    /**
     * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(CaseInsensitiveStringMultiMap map, JsonGenerator gen, SerializerProvider serializers)
            throws IOException, JsonProcessingException {
        gen.writeStartObject(); // {

        for (String key : map.keySet()) {
            List<String> values = map.getAll(key);

            if (values.size() <= 1) {
                gen.writeStringField(key, values.get(0)); // "key": "value"
            } else {
                gen.writeFieldName(key); // "key":
                gen.writeStartArray(values.size()); // [
                for (String val : values) {
                    gen.writeString(val); // "value", ...
                }
                gen.writeEndArray();// ]
            }
        }

        gen.writeEndObject(); // }
    }
}
