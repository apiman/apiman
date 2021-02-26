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
package io.apiman.common.es.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Some useful elasticsearch impl constants.
 *
 * @author eric.wittmann@redhat.com
 */
public class EsConstants {

    public static final String GATEWAY_INDEX_NAME = "apiman_gateway"; //$NON-NLS-1$
    public static final String METRICS_INDEX_NAME = "apiman_metrics"; //$NON-NLS-1$
    public static final String CACHE_INDEX_NAME = "apiman_cache"; //$NON-NLS-1$
    public static final String MANAGER_INDEX_NAME = "apiman_manager"; //$NON-NLS-1$

    //cache indices
    public static final String INDEX_CACHE_CACHE_ENTRY = "cacheEntry";

    public static final String[] CACHE_INDEX_POSTFIXES = {
        INDEX_CACHE_CACHE_ENTRY
    };

    //gateway indices (indices have to be lowercase)
    public static final String INDEX_APIS = "apis"; //$NON-NLS-1$
    public static final String INDEX_CLIENTS = "clients"; //$NON-NLS-1$
    public static final String INDEX_RATE_BUCKET = "ratebucket"; //$NON-NLS-1$
    public static final String INDEX_SHARED_STATE_PROPERTY = "sharedstateproperty"; //$NON-NLS-1$
    public static final String INDEX_DATA_VERSION = "dataversion"; //$NON-NLS-1$

    public static final String[] GATEWAY_INDEX_POSTFIXES = {
        INDEX_APIS,
        INDEX_CLIENTS,
        INDEX_RATE_BUCKET,
        INDEX_SHARED_STATE_PROPERTY,
        INDEX_DATA_VERSION
    };

    // manager indices (indices have to be lowercase)
    public static final String INDEX_MANAGER_POSTFIX_GATEWAY = "gateway";
    public static final String INDEX_MANAGER_POSTFIX_DOWNLOAD = "download";
    public static final String INDEX_MANAGER_POSTFIX_POLICY_DEF = "policydef";
    public static final String INDEX_MANAGER_POSTFIX_PLUGIN = "plugin";
    public static final String INDEX_MANAGER_POSTFIX_ROLE = "role";
    public static final String INDEX_MANAGER_POSTFIX_USER = "user";
    public static final String INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP = "rolemembership";
    public static final String INDEX_MANAGER_POSTFIX_ORGANIZATION = "organization";
    public static final String INDEX_MANAGER_POSTFIX_AUDIT_ENTRY = "auditentry";
    public static final String INDEX_MANAGER_POSTFIX_PLAN = "plan";
    public static final String INDEX_MANAGER_POSTFIX_PLAN_VERSION = "planversion";
    public static final String INDEX_MANAGER_POSTFIX_PLAN_POLICIES = "planpolicies";
    public static final String INDEX_MANAGER_POSTFIX_API = "api";
    public static final String INDEX_MANAGER_POSTFIX_API_DEFINITION = "apidefinition";
    public static final String INDEX_MANAGER_POSTFIX_API_VERSION = "apiversion";
    public static final String INDEX_MANAGER_POSTFIX_API_POLICIES = "apipolicies";
    public static final String INDEX_MANAGER_POSTFIX_CLIENT = "client";
    public static final String INDEX_MANAGER_POSTFIX_CLIENT_VERSION = "clientversion";
    public static final String INDEX_MANAGER_POSTFIX_CLIENT_POLICIES = "clientpolicies";
    public static final String INDEX_MANAGER_POSTFIX_CONTRACT = "contract";
    public static final String INDEX_MANAGER_POSTFIX_DEVELOPER = "developer";
    public static final String INDEX_MANAGER_POSTFIX_METADATA = "metadata";


    public static final String[] MANAGER_INDEX_POSTFIXES = {
        INDEX_MANAGER_POSTFIX_GATEWAY,
        INDEX_MANAGER_POSTFIX_DOWNLOAD,
        INDEX_MANAGER_POSTFIX_POLICY_DEF,
        INDEX_MANAGER_POSTFIX_PLUGIN,
        INDEX_MANAGER_POSTFIX_ROLE,
        INDEX_MANAGER_POSTFIX_USER,
        INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP,
        INDEX_MANAGER_POSTFIX_ORGANIZATION,
        INDEX_MANAGER_POSTFIX_AUDIT_ENTRY,
        INDEX_MANAGER_POSTFIX_PLAN,
        INDEX_MANAGER_POSTFIX_PLAN_VERSION,
        INDEX_MANAGER_POSTFIX_PLAN_POLICIES,
        INDEX_MANAGER_POSTFIX_API,
        INDEX_MANAGER_POSTFIX_API_DEFINITION,
        INDEX_MANAGER_POSTFIX_API_VERSION,
        INDEX_MANAGER_POSTFIX_API_POLICIES,
        INDEX_MANAGER_POSTFIX_CLIENT,
        INDEX_MANAGER_POSTFIX_CLIENT_VERSION,
        INDEX_MANAGER_POSTFIX_CLIENT_POLICIES,
        INDEX_MANAGER_POSTFIX_CONTRACT,
        INDEX_MANAGER_POSTFIX_DEVELOPER,
        INDEX_MANAGER_POSTFIX_METADATA
    };

    // es fields  (field names could be camelcase)
    public static final String ES_FIELD_APIMAN_VERSION = "apimanVersion";
    public static final String ES_FIELD_APIMAN_VERSION_AT_IMPORT = "apimanVersionAtImport";
    public static final String ES_FIELD_API_DESCRIPTION = "apiDescription";
    public static final String ES_FIELD_API_DURATION = "apiDuration";
    public static final String ES_FIELD_API_END = "apiEnd";
    public static final String ES_FIELD_API_ID = "apiId";
    public static final String ES_FIELD_API_KEY = "apiKey";
    public static final String ES_FIELD_API_NAME = "apiName";
    public static final String ES_FIELD_API_ORGANIZATION_ID = "apiOrganizationId";
    public static final String ES_FIELD_API_ORGANIZATION_NAME = "apiOrganizationName";
    public static final String ES_FIELD_API_ORG_ID = "apiOrgId";
    public static final String ES_FIELD_API_START = "apiStart";
    public static final String ES_FIELD_API_VERSION = "apiVersion";
    public static final String ES_FIELD_ARTIFACT_ID = "artifactId";
    public static final String ES_FIELD_AUTO_GRANT = "autoGrant";
    public static final String ES_FIELD_BYTES_DOWNLOADED = "bytesDownloaded";
    public static final String ES_FIELD_BYTES_UPLOADED = "bytesUploaded";
    public static final String ES_FIELD_CLIENT_DESCRIPTION = "clientDescription";
    public static final String ES_FIELD_CLIENT_ID = "clientId";
    public static final String ES_FIELD_CLIENT_NAME = "clientName";
    public static final String ES_FIELD_CLIENT_ORGANIZATION_ID = "clientOrganizationId";
    public static final String ES_FIELD_CLIENT_ORGANIZATION_NAME = "clientOrganizationName";
    public static final String ES_FIELD_CLIENT_ORG_ID = "clientOrgId";
    public static final String ES_FIELD_CLIENT_VERSION = "clientVersion";
    public static final String ES_FIELD_CONFIGURATION = "configuration";
    public static final String ES_FIELD_CONTRACT_ID = "contractId";
    public static final String ES_FIELD_COUNT = "count";
    public static final String ES_FIELD_CREATED_BY = "createdBy";
    public static final String ES_FIELD_CREATED_ON = "createdOn";
    public static final String ES_FIELD_DATA = "data";
    public static final String ES_FIELD_DEFINITION_ID = "definitionId";
    public static final String ES_FIELD_DEFINITION_TYPE = "definitionType";
    public static final String ES_FIELD_DEFINITION_URL = "definitionUrl";
    public static final String ES_FIELD_DELETED = "deleted";
    public static final String ES_FIELD_DESCRIPTION = "description";
    public static final String ES_FIELD_DISABLE_KEYS_STRIP = "disableKeysStrip";
    public static final String ES_FIELD_EMAIL = "email";
    public static final String ES_FIELD_ENDPOINT = "endpoint";
    public static final String ES_FIELD_ENDPOINT_CONTENT_TYPE = "endpointContentType";
    public static final String ES_FIELD_ENDPOINT_PROPERTIES = "endpointProperties";
    public static final String ES_FIELD_ENDPOINT_TYPE = "endpointType";
    public static final String ES_FIELD_ENTITY_ID = "entityId";
    public static final String ES_FIELD_ENTITY_TYPE = "entityType";
    public static final String ES_FIELD_ENTITY_VERSION = "entityVersion";
    public static final String ES_FIELD_ERROR = "error";
    public static final String ES_FIELD_ERROR_MESSAGE = "errorMessage";
    public static final String ES_FIELD_EXPIRES = "expires";
    public static final String ES_FIELD_EXPORTED_ON = "exportedOn";
    public static final String ES_FIELD_FAILURE = "failure";
    public static final String ES_FIELD_FAILURE_CODE = "failureCode";
    public static final String ES_FIELD_FAILURE_REASON = "failureReason";
    public static final String ES_FIELD_FORM = "form";
    public static final String ES_FIELD_FORM_TYPE = "formType";
    public static final String ES_FIELD_FULL_NAME = "fullName";
    public static final String ES_FIELD_GATEWAY_ID = "gatewayId";
    public static final String ES_FIELD_GROUP_ID = "groupId";
    public static final String ES_FIELD_ICON = "icon";
    public static final String ES_FIELD_ID = "id";
    public static final String ES_FIELD_IMPORTED_ON = "importedOn";
    public static final String ES_FIELD_JOINED_ON = "joinedOn";
    public static final String ES_FIELD_KEYS_STRIPPING_DISABLED = "keysStrippingDisabled";
    public static final String ES_FIELD_LAST = "last";
    public static final String ES_FIELD_LOCKED_ON = "lockedOn";
    public static final String ES_FIELD_METHOD = "method";
    public static final String ES_FIELD_MODIFIED_BY = "modifiedBy";
    public static final String ES_FIELD_MODIFIED_ON = "modifiedOn";
    public static final String ES_FIELD_NAME = "name";
    public static final String ES_FIELD_NUM_PUBLISHED = "numPublished";
    public static final String ES_FIELD_ORDER_INDEX = "orderIndex";
    public static final String ES_FIELD_ORGANIZATION_ID = "organizationId";
    public static final String ES_FIELD_ORGANIZATION_NAME = "organizationName";
    public static final String ES_FIELD_PARSE_PAYLOAD = "parsePayload";
    public static final String ES_FIELD_PATH = "path";
    public static final String ES_FIELD_PERMISSIONS = "permissions";
    public static final String ES_FIELD_PLAN = "plan";
    public static final String ES_FIELD_PLAN_DESCRIPTION = "planDescription";
    public static final String ES_FIELD_PLAN_ID = "planId";
    public static final String ES_FIELD_PLAN_NAME = "planName";
    public static final String ES_FIELD_PLAN_VERSION = "planVersion";
    public static final String ES_FIELD_PLUGIN_ID = "pluginId";
    public static final String ES_FIELD_POLICY_IMPL = "policyImpl";
    public static final String ES_FIELD_POLICY_JSON_CONFIG = "policyJsonConfig";
    public static final String ES_FIELD_PUBLIC_API = "publicAPI";
    public static final String ES_FIELD_PUBLISHED_ON = "publishedOn";
    public static final String ES_FIELD_REMOTE_ADDR = "remoteAddr";
    public static final String ES_FIELD_REQUEST_DURATION = "requestDuration";
    public static final String ES_FIELD_REQUEST_END = "requestEnd";
    public static final String ES_FIELD_REQUEST_START = "requestStart";
    public static final String ES_FIELD_RESOURCE = "resource";
    public static final String ES_FIELD_RESPONSE_CODE = "responseCode";
    public static final String ES_FIELD_RESPONSE_MESSAGE = "responseMessage";
    public static final String ES_FIELD_RETIRED_ON = "retiredOn";
    public static final String ES_FIELD_ROLE_ID = "roleId";
    public static final String ES_FIELD_STATUS = "status";
    public static final String ES_FIELD_SUCCESS = "success";
    public static final String ES_FIELD_TEMPLATE = "template";
    public static final String ES_FIELD_TYPE = "type";
    public static final String ES_FIELD_UPDATED_ON = "updatedOn";
    public static final String ES_FIELD_URL = "url";
    public static final String ES_FIELD_USER = "user";
    public static final String ES_FIELD_USERNAME = "username";
    public static final String ES_FIELD_USER_ID = "userId";
    public static final String ES_FIELD_VALUE = "value";
    public static final String ES_FIELD_VERSION = "version";
    public static final String ES_FIELD_WHAT = "what";
    public static final String ES_FIELD_WHO = "who";

    // nested es field prefixes
    public static final String ES_NESTED_FIELD_PREFIX_API_POLICIES = "apiPolicies.";
    public static final String ES_NESTED_FIELD_PREFIX_CLIENTS = "clients.";
    public static final String ES_NESTED_FIELD_PREFIX_CONTRACTS = "contracts.";
    public static final String ES_NESTED_FIELD_PREFIX_GATEWAYS = "gateways.";
    public static final String ES_NESTED_FIELD_PREFIX_PLANS = "plans.";
    public static final String ES_NESTED_FIELD_PREFIX_POLICIES = "policies.";
    public static final String ES_NESTED_FIELD_PREFIX_TEMPLATES = "templates.";


    // nested es fields
    public static final String ES_NESTED_FIELD_API_POLICIES_POLICY_IMPL = ES_NESTED_FIELD_PREFIX_API_POLICIES + ES_FIELD_POLICY_IMPL;
    public static final String ES_NESTED_FIELD_API_POLICIES_POLICY_JSON_CONFIG = ES_NESTED_FIELD_PREFIX_API_POLICIES + ES_FIELD_POLICY_JSON_CONFIG;
    public static final String ES_NESTED_FIELD_POLICIES_CONFIGURATION = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_CONFIGURATION;
    public static final String ES_NESTED_FIELD_POLICIES_CREATED_BY = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_CREATED_BY;
    public static final String ES_NESTED_FIELD_POLICIES_CREATED_ON = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_CREATED_ON;
    public static final String ES_NESTED_FIELD_POLICIES_DEFINITION_ID = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_DEFINITION_ID;
    public static final String ES_NESTED_FIELD_POLICIES_ID = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_ID;
    public static final String ES_NESTED_FIELD_POLICIES_MODIFIED_BY = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_MODIFIED_BY;
    public static final String ES_NESTED_FIELD_POLICIES_MODIFIED_ON = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_MODIFIED_ON;
    public static final String ES_NESTED_FIELD_POLICIES_NAME = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_NAME;
    public static final String ES_NESTED_FIELD_POLICIES_ORDER_INDEX = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_ORDER_INDEX;
    public static final String ES_NESTED_FIELD_POLICIES_POLICY_IMPL = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_POLICY_IMPL;
    public static final String ES_NESTED_FIELD_POLICIES_POLICY_JSON_CONFIG = ES_NESTED_FIELD_PREFIX_POLICIES + ES_FIELD_POLICY_JSON_CONFIG;
    public static final String ES_NESTED_FIELD_CLIENTS_CLIENT_ID = ES_NESTED_FIELD_PREFIX_CLIENTS + ES_FIELD_CLIENT_ID;
    public static final String ES_NESTED_FIELD_CLIENTS_ORGANIZATION_ID = ES_NESTED_FIELD_PREFIX_CLIENTS + ES_FIELD_ORGANIZATION_ID;
    public static final String ES_NESTED_FIELD_GATEWAYS_GATEWAY_ID = ES_NESTED_FIELD_PREFIX_GATEWAYS + ES_FIELD_GATEWAY_ID;
    public static final String ES_NESTED_FIELD_PLANS_PLAN_ID = ES_NESTED_FIELD_PREFIX_PLANS + ES_FIELD_PLAN_ID;
    public static final String ES_NESTED_FIELD_PLANS_VERSION = ES_NESTED_FIELD_PREFIX_PLANS + ES_FIELD_VERSION;
    public static final String ES_NESTED_FIELD_CONTRACTS_API_ID = ES_NESTED_FIELD_PREFIX_CONTRACTS + ES_FIELD_API_ID;
    public static final String ES_NESTED_FIELD_CONTRACTS_API_ORGANIZATION_ID = ES_NESTED_FIELD_PREFIX_CONTRACTS + ES_FIELD_API_ORGANIZATION_ID;
    public static final String ES_NESTED_FIELD_CONTRACTS_API_VERSION = ES_NESTED_FIELD_PREFIX_CONTRACTS + ES_FIELD_API_VERSION;
    public static final String ES_NESTED_FIELD_CONTRACTS_PLAN = ES_NESTED_FIELD_PREFIX_CONTRACTS + ES_FIELD_PLAN;
    public static final String ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_IMPL = ES_NESTED_FIELD_PREFIX_CONTRACTS + ES_NESTED_FIELD_POLICIES_POLICY_IMPL;
    public static final String ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_JSON_CONFIG = ES_NESTED_FIELD_PREFIX_CONTRACTS + ES_NESTED_FIELD_POLICIES_POLICY_JSON_CONFIG;
    public static final String ES_NESTED_FIELD_TEMPLATES_TEMPLATE = ES_NESTED_FIELD_PREFIX_TEMPLATES + ES_FIELD_TEMPLATE;

    public static final String ES_MAPPING_TYPE_BINARY = "binary";
    public static final String ES_MAPPING_TYPE_BOOLEAN = "boolean";
    public static final String ES_MAPPING_TYPE_DATE = "date";
    public static final String ES_MAPPING_TYPE_IP = "ip";
    public static final String ES_MAPPING_TYPE_KEYWORD = "keyword";
    public static final String ES_MAPPING_TYPE_LONG = "long";
    public static final String ES_MAPPING_TYPE_OBJECT = "object";
    public static final String ES_MAPPING_TYPE_TEXT = "text";

    public static final Map<String, String> esFieldMapping = new HashMap<String, String>() {{
        put(ES_FIELD_APIMAN_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_APIMAN_VERSION_AT_IMPORT, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_API_DESCRIPTION, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_API_DURATION, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_API_END, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_API_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_API_KEY, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_API_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_API_ORGANIZATION_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_API_ORGANIZATION_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_API_ORG_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_API_START, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_API_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ARTIFACT_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_AUTO_GRANT, ES_MAPPING_TYPE_BOOLEAN);
        put(ES_FIELD_BYTES_DOWNLOADED, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_BYTES_UPLOADED, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_CLIENT_DESCRIPTION, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_CLIENT_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_CLIENT_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_CLIENT_ORGANIZATION_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_CLIENT_ORGANIZATION_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_CLIENT_ORG_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_CLIENT_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_CONFIGURATION, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_CONTRACT_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_COUNT, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_CREATED_BY, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_CREATED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_DATA, ES_MAPPING_TYPE_BINARY);
        put(ES_FIELD_DEFINITION_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_DEFINITION_TYPE, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_DEFINITION_URL, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_DELETED, ES_MAPPING_TYPE_BOOLEAN);
        put(ES_FIELD_DESCRIPTION, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_DISABLE_KEYS_STRIP, ES_MAPPING_TYPE_BOOLEAN);
        put(ES_FIELD_EMAIL, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_ENDPOINT, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_ENDPOINT_CONTENT_TYPE, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ENDPOINT_PROPERTIES, ES_MAPPING_TYPE_OBJECT);
        put(ES_FIELD_ENDPOINT_TYPE, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ENTITY_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ENTITY_TYPE, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ENTITY_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ERROR, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_ERROR_MESSAGE, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_EXPIRES, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_EXPORTED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_FAILURE, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_FAILURE_CODE, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_FAILURE_REASON, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_FORM, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_FORM_TYPE, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_FULL_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_GATEWAY_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_GROUP_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ICON, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_IMPORTED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_JOINED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_KEYS_STRIPPING_DISABLED, ES_MAPPING_TYPE_BOOLEAN);
        put(ES_FIELD_LAST, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_LOCKED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_METHOD, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_MODIFIED_BY, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_MODIFIED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_NUM_PUBLISHED, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_ORDER_INDEX, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_ORGANIZATION_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_ORGANIZATION_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_PARSE_PAYLOAD, ES_MAPPING_TYPE_BOOLEAN);
        put(ES_FIELD_PATH, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_PERMISSIONS, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_PLAN, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_PLAN_DESCRIPTION, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_PLAN_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_PLAN_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_PLAN_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_PLUGIN_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_POLICY_IMPL, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_POLICY_JSON_CONFIG, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_PUBLIC_API, ES_MAPPING_TYPE_BOOLEAN);
        put(ES_FIELD_PUBLISHED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_REMOTE_ADDR, ES_MAPPING_TYPE_IP);
        put(ES_FIELD_REQUEST_DURATION, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_REQUEST_END, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_REQUEST_START, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_RESOURCE, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_RESPONSE_CODE, ES_MAPPING_TYPE_LONG);
        put(ES_FIELD_RESPONSE_MESSAGE, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_RETIRED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_ROLE_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_STATUS, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_SUCCESS, ES_MAPPING_TYPE_BOOLEAN);
        put(ES_FIELD_TEMPLATE, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_TYPE, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_UPDATED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_FIELD_URL, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_USER, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_USERNAME, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_USER_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_VALUE, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_FIELD_WHAT, ES_MAPPING_TYPE_TEXT);
        put(ES_FIELD_WHO, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_POLICIES_CONFIGURATION, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_POLICIES_CREATED_BY, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_POLICIES_CREATED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_NESTED_FIELD_POLICIES_DEFINITION_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_POLICIES_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_POLICIES_MODIFIED_BY, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_POLICIES_MODIFIED_ON, ES_MAPPING_TYPE_DATE);
        put(ES_NESTED_FIELD_POLICIES_NAME, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_POLICIES_ORDER_INDEX, ES_MAPPING_TYPE_LONG);
        put(ES_NESTED_FIELD_POLICIES_POLICY_IMPL, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_POLICIES_POLICY_JSON_CONFIG, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_CLIENTS_CLIENT_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_CLIENTS_ORGANIZATION_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_GATEWAYS_GATEWAY_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_PLANS_PLAN_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_PLANS_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_CONTRACTS_API_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_CONTRACTS_API_ORGANIZATION_ID, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_CONTRACTS_API_VERSION, ES_MAPPING_TYPE_KEYWORD);
        put(ES_NESTED_FIELD_CONTRACTS_PLAN, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_IMPL, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_JSON_CONFIG, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_API_POLICIES_POLICY_IMPL, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_API_POLICIES_POLICY_JSON_CONFIG, ES_MAPPING_TYPE_TEXT);
        put(ES_NESTED_FIELD_TEMPLATES_TEMPLATE, ES_MAPPING_TYPE_TEXT);
    }};

    // caches the es version read from the property file
    private static String esVersion;

    /**
     * Reads the elasticsearch version from the maven-generated properties file
     *
     * @return version the elasticsearch version
     */
    public static String getEsVersion() {
        if (esVersion != null) {
            return esVersion;
        } else {
            java.io.InputStream is = EsConstants.class.getResourceAsStream("apiman-es.properties");
            java.util.Properties p = new Properties();
            String version;
            try {
                p.load(is);
            } catch (IOException e) {
            }
            version = p.getProperty("apiman.elasticsearch-version");
            if (version == null || version.isEmpty()) {
                version = "latest";
            }
            esVersion = version;
            return version;
        }
    }
}
