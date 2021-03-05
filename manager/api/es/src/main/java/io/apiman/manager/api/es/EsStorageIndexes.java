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
package io.apiman.manager.api.es;

import static io.apiman.common.es.util.builder.index.EsIndexUtils.BIN_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.BOOL_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.DATE_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.KEYWORD_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.LONG_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.OBJECT_PROP;
import static io.apiman.common.es.util.builder.index.EsIndexUtils.TEXT_AND_KEYWORD_PROP_256;

import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.builder.index.EsIndexProperties;

/**
 * Elasticsearch index definitions for the Manager API
 **/
public class EsStorageIndexes {
    static final EsIndexProperties MANAGER_GATEWAY = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CONFIGURATION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_TYPE, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_DOWNLOAD = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_EXPIRES, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PATH, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_TYPE, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_POLICY_DEF= EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_FORM_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ICON, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PLUGIN_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_FORM, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_TEMPLATES_TEMPLATE, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_DELETED, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_POLICY_IMPL, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties MANAGER_PLUGIN = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_ARTIFACT_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DELETED, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_GROUP_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_ROLE = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_AUTO_GRANT, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_PERMISSIONS, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties MANAGER_USER = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_EMAIL, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_FULL_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_JOINED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_USERNAME, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties MANAGER_MEMBERSHIP = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ROLE_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_USER_ID, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_ORGANIZATION = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties MANAGER_AUDIT_ENTRY = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DATA, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ENTITY_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENTITY_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENTITY_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_WHAT, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_WHO, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties MANAGER_PLAN = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties MANAGER_PLAN_VERSION = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_LOCKED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_PLAN_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_PLAN_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PLAN_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_STATUS, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_PLAN_POLICIES = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_ENTITY_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENTITY_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CONFIGURATION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_DEFINITION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_ORDER_INDEX, LONG_PROP)
        .build();

    static final EsIndexProperties MANAGER_API = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_NUM_PUBLISHED, LONG_PROP)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .build();

    static final EsIndexProperties MANAGER_API_DEFINITION = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_DATA, BIN_PROP)
        .build();

    static final EsIndexProperties MANAGER_API_VERSION = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_API_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_API_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_API_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DEFINITION_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_DEFINITION_URL, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_DISABLE_KEYS_STRIP, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_CONTENT_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_PROPERTIES, OBJECT_PROP)
        .addProperty(EsConstants.ES_FIELD_ENDPOINT_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_PARSE_PAYLOAD, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_PUBLIC_API, BOOL_PROP)
        .addProperty(EsConstants.ES_FIELD_PUBLISHED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_RETIRED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_STATUS, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_GATEWAYS_GATEWAY_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_PLANS_PLAN_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_PLANS_VERSION, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_API_POLICIES = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_DEFINITION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENTITY_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CONFIGURATION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ENTITY_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_ORDER_INDEX, LONG_PROP)
        .build();

    static final EsIndexProperties MANAGER_CLIENT = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .build();

    static final EsIndexProperties MANAGER_CLIENT_VERSION = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_API_KEY, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CLIENT_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CLIENT_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_PUBLISHED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_RETIRED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_STATUS, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_VERSION, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_CLIENT_POLICIES = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_DEFINITION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENTITY_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ENTITY_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_TYPE, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CONFIGURATION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_NESTED_FIELD_POLICIES_ORDER_INDEX, LONG_PROP)
        .build();

    static final EsIndexProperties MANAGER_CONTRACT = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_API_DESCRIPTION, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_API_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_API_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_API_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_API_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_API_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CLIENT_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CLIENT_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CLIENT_ORGANIZATION_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CLIENT_ORGANIZATION_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CLIENT_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_CREATED_BY, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_CREATED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PLAN_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_PLAN_NAME, TEXT_AND_KEYWORD_PROP_256)
        .addProperty(EsConstants.ES_FIELD_PLAN_VERSION, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_DEVELOPER = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CLIENTS_CLIENT_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_NESTED_FIELD_CLIENTS_ORGANIZATION_ID, KEYWORD_PROP)
        .build();

    static final EsIndexProperties MANAGER_METADATA = EsIndexProperties.builder()
        .addProperty(EsConstants.ES_FIELD_APIMAN_VERSION, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_APIMAN_VERSION_AT_IMPORT, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_EXPORTED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_ID, KEYWORD_PROP)
        .addProperty(EsConstants.ES_FIELD_IMPORTED_ON, DATE_PROP)
        .addProperty(EsConstants.ES_FIELD_SUCCESS, BOOL_PROP)
        .build();

}
