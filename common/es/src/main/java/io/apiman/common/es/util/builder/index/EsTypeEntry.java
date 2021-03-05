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
import static io.apiman.common.es.util.EsConstants.ES_MAPPING_TYPE_TEXT;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Objects;

/**
 * An Elasticsearch type entry
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class EsTypeEntry implements AllowableFieldEntry {
    private final String type;

    private EsTypeEntry(EsPropertyBuilder builder) {
        this.type = builder.type;
    }

    public String getType() {
        return type;
    }

    public EsPropertyBuilder builder() {
        return new EsPropertyBuilder();
    }

    @Override
    public String getEntryType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean isKeyword() {
        return ES_MAPPING_TYPE_KEYWORD.equalsIgnoreCase(type);
    }

    @Override
    public boolean isKeywordMultiField() {
        return false;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static final class EsPropertyBuilder {
        private String type;

        public EsPropertyBuilder() {
        }

        public EsPropertyBuilder setType(String type) {
            this.type = type;
            return this;
        }

        public EsTypeEntry build() {
            Objects.requireNonNull(type, "Type must not be null");
            return new EsTypeEntry(this);
        }
    }
}
