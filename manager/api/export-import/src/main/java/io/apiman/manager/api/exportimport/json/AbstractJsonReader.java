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

import io.apiman.manager.api.exportimport.EntityHandler;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 * Common JSON reader functionality
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public abstract class AbstractJsonReader {

    public AbstractJsonReader() {
        super();
    }

    protected abstract JsonParser jsonParser();

    protected <T> void processEntities(Class<T> klazz, EntityHandler<T> handler) throws Exception {
        while (nextToken() != JsonToken.END_ARRAY) {
            processEntity(klazz, handler);
        }
    }

    protected <T> void processEntity(Class<T> klazz, EntityHandler<T> handler) throws Exception {
        handler.handleEntity(jsonParser().readValueAs(klazz));
    }

    protected JsonToken nextToken() throws JsonParseException, IOException {
        JsonToken token = jsonParser().nextToken();
        return token;
    }

}