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

import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_KEYWORD;
import static io.apiman.common.util.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.apiman.common.es.util.EsConstants;
import java.util.HashMap;
import java.util.Map;

/**
 * An Elasticsearch property
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonInclude(Include.NON_EMPTY)
public class EsIndexProperty implements AllowableIndexPropertyEntry, AllowableFieldEntry {

    @JsonProperty("fields")
    private final Map<String, AllowableFieldEntry> fieldMap;
    private final String type;

    // Should use this for diagnostics only in most cases.
    @JsonProperty("fielddata")
    private final Boolean fieldData;

    private EsIndexProperty(EsIndexPropertyBuilder builder) {
        fieldMap = builder.fieldMap;
        type = builder.type;
        this.fieldData = builder.fieldData;
    }

    public Boolean getFieldData() {
        return fieldData;
    }
    
    /**
     * Get the field map
     */
    public Map<String, AllowableFieldEntry> getFieldMap() {
        return fieldMap;
    }

    /**
     * Get the ES data type, for example, text, boolean, keyword, etc.
     */
    public String getType() {
        return type;
    }

    /**
     * Create a builder
     */
    public static EsIndexPropertyBuilder builder() {
        return new EsIndexPropertyBuilder();
    }

    @Override
    public String getEntryType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isKeyword() {
        return ES_MAPPING_TYPE_KEYWORD.equalsIgnoreCase(type);
    }

    /**
     * This is current quite an implementation-specific mechanism, but we check: <p/>
     *  * If this top-level field is a keyword, in which case it's not a text-keyword multi-field
     *  * Otherwise, check to see if there's a "keyword" sub-entry with the type of keyword.
     */
    @Override
    public boolean isKeywordMultiField() {
        return !isKeyword() && fieldMap.containsKey(ES_MAPPING_TYPE_KEYWORD)
            && fieldMap.get(ES_MAPPING_TYPE_KEYWORD).isKeyword();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static final class EsIndexPropertyBuilder {
        private Map<String, AllowableFieldEntry> fieldMap = new HashMap<>();
        private String type;
        private Boolean fieldData;


        public EsIndexPropertyBuilder() {
        }

        /**
         * Set the ES data type, for example, text, boolean, keyword, etc.
         */
        public EsIndexPropertyBuilder setType(String type) {
            this.type = type;
            return this;
        }

        /**
         * Add a field, this can be of several concrete types due to nesting and multi-fields.
         */
        public EsIndexPropertyBuilder addField(String fieldName, AllowableFieldEntry field) {
            this.fieldMap.put(fieldName, field);
            return this;
        }

        /**
         * Set the fieldmap explicitly. Most use-cases will not need to use this.
         */
        public EsIndexPropertyBuilder setFieldMap(Map<String, AllowableFieldEntry> fieldMap) {
            this.fieldMap = fieldMap;
            return this;
        }

        /**
         * Likely should only use this for diagnostics. Sets the fielddata field to true, which makes the
         * entity fully indexed and searchable, but it has serious caveats.
         * @see <a href='https://www.elastic.co/guide/en/elasticsearch/reference/7.11/text.html#fielddata-mapping-param'>ES fielddata mapping params documentation</a>
         */
        public EsIndexPropertyBuilder setFieldData(Boolean value) {
            this.fieldData = value;
            return this;
        }

        /**
         * Build and validate instance of {@link EsIndexPropertyBuilder}
         * @return a new {@link EsIndexPropertyBuilder} with the configuration provided in this builder
         */
        public EsIndexProperty build() {
            checkArgument(fieldMap != null || type != null, "Both fields and type can not both be null null");
            return new EsIndexProperty(this);
        }

    }
}
