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
package io.apiman.manager.api.es.util;

import java.io.IOException;

import org.apache.commons.io.output.StringBuilderWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Mimics the ES class of the same name.  For more details please see
 * {@link XContentFactory}.
 *
 * @author ewittman
 */
public class XContentBuilder implements AutoCloseable {

    private static final JsonFactory jsonFactory = new JsonFactory();
    public JsonGenerator json;
    private StringBuilderWriter writer;

    /**
     * Constructor.
     */
    public XContentBuilder() {
        try {
            writer = new StringBuilderWriter();
            json = jsonFactory.createGenerator(writer);
        } catch (IOException e) {
            // Will never happen!
            throw new RuntimeException(e);
        }
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        try {
            json.close();
        } catch (IOException e) {
            // Will never happen!
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the string value of the builder
     */
    public String string() {
        return writer.getBuilder().toString();
    }

    /**
     * @param fieldName
     * @throws IOException
     */
    public XContentBuilder startObject(String fieldName) throws IOException {
        json.writeObjectFieldStart(fieldName);
        return this;
    }

    /**
     * @throws IOException
     */
    public XContentBuilder startObject() throws IOException {
        json.writeStartObject();
        return this;
    }

    /**
     * @param fieldName
     * @throws IOException
     */
    public XContentBuilder startArray(String fieldName) throws IOException {
        json.writeArrayFieldStart(fieldName);
        return this;
    }

    /**
     * @param fieldName
     * @param fieldValue
     * @throws IOException
     */
    public XContentBuilder field(String fieldName, String fieldValue) throws IOException {
        if (fieldValue != null) {
            json.writeStringField(fieldName, fieldValue);
        }
        return this;
    }

    /**
     * @param fieldName
     * @param fieldValue
     * @throws IOException
     */
    public XContentBuilder field(String fieldName, Enum<?> fieldValue) throws IOException {
        if (fieldValue != null) {
            json.writeStringField(fieldName, fieldValue.name());
        }
        return this;
    }

    /**
     * @param fieldName
     * @param fieldValue
     * @throws IOException
     */
    public XContentBuilder field(String fieldName, Long fieldValue) throws IOException {
        if (fieldValue != null) {
            json.writeNumberField(fieldName, fieldValue);
        }
        return this;
    }

    /**
     * @param fieldName
     * @param fieldValue
     * @throws IOException
     */
    public XContentBuilder field(String fieldName, Integer fieldValue) throws IOException {
        if (fieldValue != null) {
            json.writeNumberField(fieldName, fieldValue);
        }
        return this;
    }

    /**
     * @param fieldName
     * @param fieldValue
     * @throws IOException
     */
    public XContentBuilder field(String fieldName, Boolean fieldValue) throws IOException {
        if (fieldValue != null) {
            json.writeBooleanField(fieldName, fieldValue);
        }
        return this;
    }

    /**
     * End the current object.
     * @throws IOException
     */
    public XContentBuilder endObject() throws IOException {
        json.writeEndObject();
        return this;
    }

    /**
     * End the current array.
     * @throws IOException
     */
    public XContentBuilder endArray() throws IOException {
        json.writeEndArray();
        return this;
    }

    /**
     * @param fieldName
     * @param array
     * @throws IOException
     */
    public XContentBuilder array(String fieldName, String[] array) throws IOException {
        json.writeArrayFieldStart(fieldName);
        for (String item : array) {
            json.writeString(item);
        }
        json.writeEndArray();
        return this;
    }

    /**
     * @param fieldName
     * @param array
     * @throws IOException
     */
    public XContentBuilder array(String fieldName, Enum<?>[] array) throws IOException {
        json.writeArrayFieldStart(fieldName);
        for (Enum<?> item : array) {
            json.writeString(item.name());
        }
        json.writeEndArray();
        return this;
    }

    /**
     * @param fieldName
     * @throws IOException
     */
    public XContentBuilder field(String fieldName) throws IOException {
        json.writeFieldName(fieldName);
        return this;
    }

    /**
     * Starts an array.
     * @throws IOException
     */
    public XContentBuilder startArray() throws IOException {
        json.writeStartArray();
        return this;
    }

}
