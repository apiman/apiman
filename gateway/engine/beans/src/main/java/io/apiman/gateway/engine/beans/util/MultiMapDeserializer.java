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
import java.util.ArrayDeque;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author eric.wittmann@gmail.com
 */
public class MultiMapDeserializer extends JsonDeserializer<HeaderMap> {

    /**
     * Constructor.
     */
    public MultiMapDeserializer() {
    }

    /**
     * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
     */
    @Override
    public HeaderMap deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        HeaderMap map = new HeaderMap();

        while (p.nextToken() != JsonToken.END_OBJECT) {
            String name = p.getCurrentName();

            p.nextToken();

            if (p.currentToken().isScalarValue()) {
                map.add(name, p.getValueAsString());
            } else {
                ArrayDeque<String> values = new ArrayDeque<>();
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    values.push(p.getValueAsString());
                }
                values.forEach(value -> map.add(name, value));
            }
        }
        return map;
    }

}
