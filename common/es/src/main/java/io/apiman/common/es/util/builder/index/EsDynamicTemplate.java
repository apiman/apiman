/*
 * Copyright 2022 Black Parrot Labs Ltd
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/7.17/dynamic-templates.html">Elasticsearch dynamic template</a> builder.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@JsonInclude(Include.NON_EMPTY)
public class EsDynamicTemplate {
    private final String match;
    @JsonProperty("match_pattern")
    private final String matchPattern;
    @JsonProperty("match_mapping_type")
    private final String matchMappingType;
    @JsonProperty("path_match")
    private final String pathMatch;
    private final EsIndexProperty mapping;

    EsDynamicTemplate(EsDynamicTemplateBuilder builder) {
        this.match = builder.match;
        this.matchPattern = builder.matchPattern;
        this.matchMappingType = builder.matchMappingType;
        this.pathMatch = builder.pathMatch;
        this.mapping = builder.mapping;
    }

    public String getMatch() {
        return match;
    }

    public String getMatchPattern() {
        return matchPattern;
    }

    public String getMatchMappingType() {
        return matchMappingType;
    }

    public String getPathMatch() {
        return pathMatch;
    }

    public EsIndexProperty getMapping() {
        return mapping;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static final class EsDynamicTemplateBuilder {
        private String match;
        private String matchPattern;
        private String pathMatch;
        private String matchMappingType;
        private EsIndexProperty mapping;

        public EsDynamicTemplateBuilder() {
        }

        public EsDynamicTemplateBuilder setMatch(String match) {
            this.match = match;
            return this;
        }

        /**
         * Match as full Java regex for {@link #setMatch(String)} instead of simple wildcard as default
         */
        public EsDynamicTemplateBuilder matcherAsRegex() {
            this.matchPattern = "regex";
            return this;
        }

        public EsDynamicTemplateBuilder setPathMatch(String pathMatch) {
            this.pathMatch = pathMatch;
            return this;
        }

        public EsDynamicTemplateBuilder setMatchMappingType(String matchMappingType) {
            this.matchMappingType = matchMappingType;
            return this;
        }

        public EsDynamicTemplateBuilder setMapping(EsIndexProperty mapping) {
            this.mapping = mapping;
            return this;
        }

        public EsDynamicTemplate build() {
            return new EsDynamicTemplate(this);
        }
    }

}

