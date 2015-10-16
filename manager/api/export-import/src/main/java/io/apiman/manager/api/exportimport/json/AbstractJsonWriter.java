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

import io.apiman.manager.api.core.logging.IApimanLogger;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;

@SuppressWarnings("nls")
public abstract class AbstractJsonWriter<T extends Enum<T>> {

    protected Enum<T> lock;
    protected boolean ended = false;

    private IApimanLogger logger;
    public int depth = 0;

    protected abstract JsonGenerator jsonGenerator();
    protected abstract Map<Enum<T>, Boolean> finished();

    /**
     * Constructor.
     */
    public AbstractJsonWriter(IApimanLogger logger) {
        this.logger = logger;
    }

    /**
     * @param msg
     */
    protected void debug(String msg) {
        String prefix = "";
        for (int i = 0 ; i < depth; i++) {
            prefix += "  ";
        }
        logger.info(prefix + msg);
    }

    protected void writeObjectFieldStart(Enum<?> globEnum) {
        debug("Start object field: " + globEnum.name());
        depth++;
        try {
            jsonGenerator().writeObjectFieldStart(globEnum.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeStartObject() {
        debug("Start object.");
        depth++;
        try {
            jsonGenerator().writeStartObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeStartObject(Enum<?> globEnum) {
        debug("Start object: " + globEnum.name());
        depth++;
        try {
            jsonGenerator().writeObjectFieldStart(globEnum.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeEndObject() {
        depth--;
        debug("End object.");
        try {
            jsonGenerator().writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeStartArray(Enum<?> globEnum) {
        debug("Start array: " + globEnum.name());
        depth++;
        try {
            jsonGenerator().writeArrayFieldStart(globEnum.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeEndArray(Enum<?> type) {
        depth--;
        debug("End array: " + type.name());
        try {
            jsonGenerator().writeEndArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writeEndArray() {
        depth--;
        debug("End array.");
        try {
            jsonGenerator().writeEndArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writePojo(Object pojo) {
        debug("POJO: " + pojo.getClass().getSimpleName());
        try {
            jsonGenerator().writeObject(pojo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writePojo(Enum<T> type, Object pojo) {
        debug("POJO: " + type.name());
        try {
            jsonGenerator().writeFieldName(type.name());
            jsonGenerator().writeObject(pojo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void unlock(Enum<T> type) {
        finished().put(type, true);
        lock = null;
    }

    protected void lock(Enum<T> type) {
        this.lock = type;
    }

    protected void validityCheckStart(Enum<T> type) {
        if (ended) {
            throw new IllegalStateException("Data streamer has already been closed");
        }

        if (lock != null) {
            throw new IllegalStateException("Must close " + lock + " section before trying new " + type);
        }

        if (finished().get(type)) {
            throw new IllegalStateException(type + " already closed");
        }
    }

    protected void writeCheck(Enum<T> app) {
        if (lock == null) {
            throw new IllegalStateException("Must call a start method first");
        }

        if (lock != app) {
            throw new IllegalStateException("Attempting to write wrong object type. Expected: " + lock + " got " + app);
        }
    }

    protected void validityCheckEnd(Enum<T> type) {
        if (lock == null) {
            throw new IllegalStateException("Nothing to close");
        }

        if (lock != type) {
            throw new IllegalStateException("Tried to close "  + type + " when open type is " + lock);
        }

        if (finished().get(type)) {
            throw new IllegalStateException(type + " is closed for writing");
        }
    }
}