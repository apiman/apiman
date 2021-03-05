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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Some simple common interrogation methods
 */
public interface Entry {

    /**
     * What type of entry is this (with respect to builder API, not underlying type).
     */
    @JsonIgnore
    String getEntryType();

    /**
     * @return true if entry is an Elasticsearch keyword type.
     */
    @JsonIgnore
    boolean isKeyword();

    /**
     * @return true if entry is an Elasticsearch multi-field with keyword.
     */
    @JsonIgnore
    boolean isKeywordMultiField();
}
