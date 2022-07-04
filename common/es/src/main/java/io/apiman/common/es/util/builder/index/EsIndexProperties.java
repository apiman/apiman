/*
 * Copyright 2021 Scheer PAS Schweiz AG
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
package io.apiman.common.es.util.builder.index;

import io.apiman.common.util.Preconditions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * An ES properties map
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EsIndexProperties implements AllowableIndexPropertyEntry, AllowableFieldEntry, AllowableRootEntry {

    @JsonProperty("properties")
    private final Map<String, AllowableIndexPropertyEntry> propertiesMap;

    @JsonProperty("dynamic_templates")
    @JsonSerialize(using = EsDynamicTemplatesSerializer.class)
    private final Map<String, EsDynamicTemplate> dynamicTemplatesMap;

    private EsIndexProperties(EsIndexPropertiesBuilder builder) {
        this.propertiesMap = builder.propertiesMap;
        this.dynamicTemplatesMap = builder.dynamicTemplatesMap;
    }

    /**
     * Get the properties
     */
    public Map<String, AllowableIndexPropertyEntry> getPropertiesMap() {
        return propertiesMap;
    }

    public Map<String, EsDynamicTemplate> getDynamicTemplatesMap() {
        return dynamicTemplatesMap;
    }

    /**
     * Create a new builder
     */
    public static EsIndexPropertiesBuilder builder() {
        return new EsIndexPropertiesBuilder();
    }

    @Override
    public String getEntryType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isKeyword() {
        return false;
    }

    @Override
    public boolean isKeywordMultiField() {
        return false;
    }

    @JsonIgnore
    public AllowableIndexPropertyEntry getProperty(String propertyName) {
        return propertiesMap.get(propertyName);
    }

    @JsonIgnore
    public boolean hasProperty(String propertyName) {
        return propertiesMap.containsKey(propertyName);
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static final class EsIndexPropertiesBuilder {
        private final Map<String, AllowableIndexPropertyEntry> propertiesMap = new HashMap<>();
        private final Map<String, EsDynamicTemplate> dynamicTemplatesMap = new HashMap<>();

        public EsIndexPropertiesBuilder() {
        }

        public EsIndexPropertiesBuilder addProperty(String propName, AllowableIndexPropertyEntry property) {
            propertiesMap.put(propName, property);
            return this;
        }

        public EsIndexPropertiesBuilder addTemplate(String propName, EsDynamicTemplate template) {
            dynamicTemplatesMap.put(propName, template);
            return this;
        }

        /**
         * Build and validate instance of {@link EsIndexProperties}
         * @return a new {@link EsIndexProperties} with the configuration provided in this builder
         */
        public EsIndexProperties build() {
            Preconditions.checkArgument(propertiesMap.size() > 0, "Must add at least one property");
            return new EsIndexProperties(this);
        }
    }

    public static class EsDynamicTemplatesSerializer extends JsonSerializer<Map<String, EsDynamicTemplate>> {

        public EsDynamicTemplatesSerializer() {
        }

        @Override
        public void serialize(Map<String, EsDynamicTemplate> templateMap, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartArray(templateMap.size());
            for (Map.Entry<String, EsDynamicTemplate> entry : templateMap.entrySet()) {
                gen.writeStartObject();
                gen.writeObjectField(entry.getKey(), entry.getValue());
                gen.writeEndObject();
            }
            gen.writeEndArray();
        }
    }
}
