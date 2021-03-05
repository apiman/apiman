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
package io.apiman.gateway.engine.es;

import static io.apiman.common.es.util.builder.index.EsIndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.OBJECT_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.TEXT_AND_KEYWORD_PROP_256;

import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsIndexProperties;

/**
 * Elasticsearch index definitions for the gateway registry
 */
public class EsRegistryIndexes {
    static final EsIndexProperties GATEWAY_APIS = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_API_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_CONTENT_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_PROPERTIES, OBJECT_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_KEYS_STRIPPING_DISABLED, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PARSE_PAYLOAD, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_PUBLIC_API, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_API_POLICIES_POLICY_IMPL, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_API_POLICIES_POLICY_JSON_CONFIG, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties GATEWAY_CLIENTS = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_API_KEY, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_API_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_API_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_API_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_PLAN, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_IMPL, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_JSON_CONFIG, TEXT_AND_KEYWORD_PROP_256)
        .build();
}
