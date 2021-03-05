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

import static io.apiman.common.util.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Top level elasticsearch configuration object.
 *
 * @see #builder() to build and validate an instance.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EsIndex {

    @JsonIgnore
    private String indexName;

    /**
     * Uses a {@link JsonAnyGetter} to return a map with a selected indexName.
     *
     * See {@link #getRootEntries()} for more information.
     */
    @JsonIgnore
    private final Map<String, AllowableRootEntry> rootEntryMap;

    private EsIndex(EsIndexBuilder esIndexBuilder) {
        this.indexName = esIndexBuilder.indexName;
        this.rootEntryMap = esIndexBuilder.rootEntryMap;
    }

    /**
     * Get the Elasticsearch index name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Override index name
     */
    public EsIndex setIndexName(String indexName) {
        this.indexName = indexName;
        return this;
    }

    /**
     * Get the root entries.
     */
    @JsonAnyGetter
    public Map<String, Object> getRootEntries() {
        return Collections.singletonMap(indexName, rootEntryMap);
    }

    public Object getMappings() {
        return rootEntryMap.keySet().stream().findFirst();
    }

    /**
     * Build an {@link EsIndex}
     */
    public static EsIndexBuilder builder() {
        return new EsIndexBuilder();
    }

    /**
     * Builder for {@link EsIndex}
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static final class EsIndexBuilder {
        private String indexName;
        private Map<String, AllowableRootEntry> rootEntryMap = new HashMap<>();

        /**
         * Set the Elasticsearch index name
         */
        public EsIndexBuilder setIndexName(String indexName) {
            this.indexName = indexName;
            return this;
        }

        /**
         * Add a 'mappings' entry to the root.
         */
        public EsIndexBuilder addPropertyMappings(
            EsIndexProperties properties
        ) {
            this.rootEntryMap.put("mappings", properties);
            return this;
        }

        /**
         * Set the whole root entry map (unless you are doing something custom, you probably don't want this)
         */
        public EsIndexBuilder setRootEntryMap(
            Map<String, AllowableRootEntry> rootEntryMap) {
            this.rootEntryMap = rootEntryMap;
            return this;
        }


        /**
         * Build and validate instance of {@link EsIndex}
         * @return a new EsIndex with the configuration provided in this builder
         */
        public EsIndex build() {
            requireNonNull(indexName, "Index name must not be null");
            checkArgument(rootEntryMap.size() > 0, "Must have some mapping entries");
            return new EsIndex(this);
        }
    }
}
