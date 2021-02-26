/*
 * Copyright 2020 Scheer PAS Schweiz AG
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

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides helper functions for Field type mapping of Elasticsearch
 */
public class EsIndexMapping {

    /**
     * Add document mapping for each elasticsearch field
     *
     * @param indexPrefix  the index prefix
     * @param indexPostfix the index postfix
     * @return document mapping for index
     */

    public static Map<String, Object> getDocumentMapping(String indexPrefix, String indexPostfix) {

        Map<String, Object> indexFieldProperties = new HashMap<String, Object>();
        String[] fieldNames = null;

        if (indexPrefix.equals(EsConstants.GATEWAY_INDEX_NAME)) {
            switch (indexPostfix) {
                case EsConstants.INDEX_APIS:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_API_ID, EsConstants.ES_FIELD_ENDPOINT,
                            EsConstants.ES_FIELD_ENDPOINT_CONTENT_TYPE, EsConstants.ES_FIELD_ENDPOINT_PROPERTIES,
                            EsConstants.ES_FIELD_ENDPOINT_TYPE, EsConstants.ES_FIELD_KEYS_STRIPPING_DISABLED,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_PARSE_PAYLOAD,
                            EsConstants.ES_FIELD_PUBLIC_API, EsConstants.ES_FIELD_VERSION,
                            EsConstants.ES_NESTED_FIELD_API_POLICIES_POLICY_IMPL,
                            EsConstants.ES_NESTED_FIELD_API_POLICIES_POLICY_JSON_CONFIG
                    };
                    break;
                case EsConstants.INDEX_CLIENTS:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_API_KEY, EsConstants.ES_FIELD_CLIENT_ID,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_VERSION,
                            EsConstants.ES_NESTED_FIELD_CONTRACTS_API_ID, EsConstants.ES_NESTED_FIELD_CONTRACTS_API_ORGANIZATION_ID,
                            EsConstants.ES_NESTED_FIELD_CONTRACTS_API_VERSION, EsConstants.ES_NESTED_FIELD_CONTRACTS_PLAN,
                            EsConstants.ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_IMPL,
                            EsConstants.ES_NESTED_FIELD_CONTRACTS_POLICIES_POLICY_JSON_CONFIG
                    };
                    break;
                case EsConstants.INDEX_DATA_VERSION:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_UPDATED_ON,
                            EsConstants.ES_FIELD_VERSION
                    };
                    break;
                case EsConstants.INDEX_RATE_BUCKET:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_COUNT, EsConstants.ES_FIELD_LAST,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_VERSION
                    };
                    break;
                case EsConstants.INDEX_SHARED_STATE_PROPERTY:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_TYPE,
                            EsConstants.ES_FIELD_VALUE, EsConstants.ES_FIELD_VERSION
                    };
                    break;
                default:
                    break;
            }
        }

        if (indexPrefix.equals(EsConstants.METRICS_INDEX_NAME)) {
            fieldNames = new String[]{
                    EsConstants.ES_FIELD_API_DURATION, EsConstants.ES_FIELD_API_END,
                    EsConstants.ES_FIELD_API_ID, EsConstants.ES_FIELD_API_ORG_ID,
                    EsConstants.ES_FIELD_API_START, EsConstants.ES_FIELD_API_VERSION,
                    EsConstants.ES_FIELD_BYTES_DOWNLOADED, EsConstants.ES_FIELD_BYTES_UPLOADED,
                    EsConstants.ES_FIELD_CLIENT_ID, EsConstants.ES_FIELD_CLIENT_ORG_ID,
                    EsConstants.ES_FIELD_CLIENT_VERSION, EsConstants.ES_FIELD_CONTRACT_ID,
                    EsConstants.ES_FIELD_ERROR, EsConstants.ES_FIELD_ERROR_MESSAGE,
                    EsConstants.ES_FIELD_FAILURE, EsConstants.ES_FIELD_FAILURE_CODE,
                    EsConstants.ES_FIELD_FAILURE_REASON, EsConstants.ES_FIELD_METHOD,
                    EsConstants.ES_FIELD_PLAN_ID, EsConstants.ES_FIELD_REMOTE_ADDR,
                    EsConstants.ES_FIELD_REQUEST_DURATION, EsConstants.ES_FIELD_REQUEST_END,
                    EsConstants.ES_FIELD_REQUEST_START, EsConstants.ES_FIELD_RESOURCE,
                    EsConstants.ES_FIELD_RESPONSE_CODE, EsConstants.ES_FIELD_RESPONSE_MESSAGE,
                    EsConstants.ES_FIELD_URL, EsConstants.ES_FIELD_USER
            };
        }

        if (indexPrefix.equals(EsConstants.MANAGER_INDEX_NAME)) {

            switch (indexPostfix) {
                case EsConstants.INDEX_MANAGER_POSTFIX_API:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_ID, EsConstants.ES_FIELD_ORGANIZATION_ID,
                            EsConstants.ES_FIELD_CREATED_BY, EsConstants.ES_FIELD_DESCRIPTION,
                            EsConstants.ES_FIELD_NAME, EsConstants.ES_FIELD_ORGANIZATION_NAME,
                            EsConstants.ES_FIELD_NUM_PUBLISHED, EsConstants.ES_FIELD_CREATED_ON
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_API_DEFINITION:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_DATA
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_API_POLICIES:
                    fieldNames = new String[]{
                            EsConstants.ES_NESTED_FIELD_POLICIES_DEFINITION_ID, EsConstants.ES_FIELD_ENTITY_ID,
                            EsConstants.ES_NESTED_FIELD_POLICIES_ID, EsConstants.ES_FIELD_ORGANIZATION_ID,
                            EsConstants.ES_FIELD_TYPE, EsConstants.ES_NESTED_FIELD_POLICIES_CONFIGURATION,
                            EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_BY, EsConstants.ES_FIELD_ENTITY_VERSION,
                            EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_BY, EsConstants.ES_NESTED_FIELD_POLICIES_NAME,
                            EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_ON, EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_ON,
                            EsConstants.ES_NESTED_FIELD_POLICIES_ORDER_INDEX
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_API_DESCRIPTION, EsConstants.ES_FIELD_API_ID,
                            EsConstants.ES_FIELD_API_NAME, EsConstants.ES_FIELD_CREATED_BY,
                            EsConstants.ES_FIELD_CREATED_ON, EsConstants.ES_FIELD_DEFINITION_TYPE,
                            EsConstants.ES_FIELD_DEFINITION_URL, EsConstants.ES_FIELD_DISABLE_KEYS_STRIP,
                            EsConstants.ES_FIELD_ENDPOINT, EsConstants.ES_FIELD_ENDPOINT_CONTENT_TYPE,
                            EsConstants.ES_FIELD_ENDPOINT_PROPERTIES, EsConstants.ES_FIELD_ENDPOINT_TYPE,
                            EsConstants.ES_FIELD_MODIFIED_BY, EsConstants.ES_FIELD_MODIFIED_ON,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_ORGANIZATION_NAME,
                            EsConstants.ES_FIELD_PARSE_PAYLOAD, EsConstants.ES_FIELD_PUBLIC_API,
                            EsConstants.ES_FIELD_PUBLISHED_ON, EsConstants.ES_FIELD_RETIRED_ON,
                            EsConstants.ES_FIELD_STATUS, EsConstants.ES_FIELD_VERSION,
                            EsConstants.ES_NESTED_FIELD_GATEWAYS_GATEWAY_ID, EsConstants.ES_NESTED_FIELD_PLANS_PLAN_ID,
                            EsConstants.ES_NESTED_FIELD_PLANS_VERSION
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_CREATED_ON, EsConstants.ES_FIELD_DATA,
                            EsConstants.ES_FIELD_ENTITY_ID, EsConstants.ES_FIELD_ENTITY_TYPE,
                            EsConstants.ES_FIELD_ENTITY_VERSION, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_WHAT,
                            EsConstants.ES_FIELD_WHO
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_CLIENT:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_CREATED_BY, EsConstants.ES_FIELD_CREATED_ON,
                            EsConstants.ES_FIELD_DESCRIPTION, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_NAME, EsConstants.ES_FIELD_ORGANIZATION_ID,
                            EsConstants.ES_FIELD_ORGANIZATION_NAME
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_POLICIES:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_DEFINITION_ID, EsConstants.ES_FIELD_ENTITY_ID,
                            EsConstants.ES_FIELD_ENTITY_VERSION, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_TYPE,
                            EsConstants.ES_NESTED_FIELD_POLICIES_CONFIGURATION, EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_BY,
                            EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_ON, EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_BY,
                            EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_ON, EsConstants.ES_NESTED_FIELD_POLICIES_NAME,
                            EsConstants.ES_NESTED_FIELD_POLICIES_ORDER_INDEX
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_API_KEY, EsConstants.ES_FIELD_CLIENT_DESCRIPTION,
                            EsConstants.ES_FIELD_CLIENT_ID, EsConstants.ES_FIELD_CLIENT_NAME,
                            EsConstants.ES_FIELD_CREATED_BY, EsConstants.ES_FIELD_CREATED_ON,
                            EsConstants.ES_FIELD_MODIFIED_BY, EsConstants.ES_FIELD_MODIFIED_ON,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_ORGANIZATION_NAME,
                            EsConstants.ES_FIELD_PUBLISHED_ON, EsConstants.ES_FIELD_RETIRED_ON,
                            EsConstants.ES_FIELD_STATUS, EsConstants.ES_FIELD_VERSION
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_API_DESCRIPTION, EsConstants.ES_FIELD_API_ID,
                            EsConstants.ES_FIELD_API_NAME, EsConstants.ES_FIELD_API_ORGANIZATION_ID,
                            EsConstants.ES_FIELD_API_ORGANIZATION_NAME, EsConstants.ES_FIELD_API_VERSION,
                            EsConstants.ES_FIELD_CLIENT_ID, EsConstants.ES_FIELD_CLIENT_NAME,
                            EsConstants.ES_FIELD_CLIENT_ORGANIZATION_ID, EsConstants.ES_FIELD_CLIENT_ORGANIZATION_NAME,
                            EsConstants.ES_FIELD_CLIENT_VERSION, EsConstants.ES_FIELD_CREATED_BY,
                            EsConstants.ES_FIELD_CREATED_ON, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_PLAN_ID, EsConstants.ES_FIELD_PLAN_NAME,
                            EsConstants.ES_FIELD_PLAN_VERSION
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_DEVELOPER:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_ID, EsConstants.ES_NESTED_FIELD_CLIENTS_CLIENT_ID,
                            EsConstants.ES_NESTED_FIELD_CLIENTS_ORGANIZATION_ID
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_DOWNLOAD:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_EXPIRES, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_PATH, EsConstants.ES_FIELD_TYPE
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_CONFIGURATION, EsConstants.ES_FIELD_CREATED_BY,
                            EsConstants.ES_FIELD_CREATED_ON, EsConstants.ES_FIELD_DESCRIPTION,
                            EsConstants.ES_FIELD_ID, EsConstants.ES_FIELD_MODIFIED_BY,
                            EsConstants.ES_FIELD_MODIFIED_ON, EsConstants.ES_FIELD_NAME,
                            EsConstants.ES_FIELD_TYPE
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_METADATA:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_APIMAN_VERSION, EsConstants.ES_FIELD_APIMAN_VERSION_AT_IMPORT,
                            EsConstants.ES_FIELD_EXPORTED_ON, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_IMPORTED_ON, EsConstants.ES_FIELD_SUCCESS
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_CREATED_BY, EsConstants.ES_FIELD_CREATED_ON,
                            EsConstants.ES_FIELD_DESCRIPTION, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_MODIFIED_BY, EsConstants.ES_FIELD_MODIFIED_ON,
                            EsConstants.ES_FIELD_NAME
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_PLAN:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_CREATED_BY, EsConstants.ES_FIELD_CREATED_ON,
                            EsConstants.ES_FIELD_DESCRIPTION, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_NAME, EsConstants.ES_FIELD_ORGANIZATION_ID,
                            EsConstants.ES_FIELD_ORGANIZATION_NAME
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_PLAN_POLICIES:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_ENTITY_ID, EsConstants.ES_FIELD_ENTITY_VERSION,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_TYPE,
                            EsConstants.ES_NESTED_FIELD_POLICIES_CONFIGURATION, EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_BY,
                            EsConstants.ES_NESTED_FIELD_POLICIES_CREATED_ON, EsConstants.ES_NESTED_FIELD_POLICIES_DEFINITION_ID,
                            EsConstants.ES_NESTED_FIELD_POLICIES_ID, EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_BY,
                            EsConstants.ES_NESTED_FIELD_POLICIES_MODIFIED_ON, EsConstants.ES_NESTED_FIELD_POLICIES_NAME,
                            EsConstants.ES_NESTED_FIELD_POLICIES_ORDER_INDEX
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_CREATED_BY, EsConstants.ES_FIELD_CREATED_ON,
                            EsConstants.ES_FIELD_LOCKED_ON, EsConstants.ES_FIELD_MODIFIED_BY,
                            EsConstants.ES_FIELD_MODIFIED_ON, EsConstants.ES_FIELD_ORGANIZATION_ID,
                            EsConstants.ES_FIELD_ORGANIZATION_NAME, EsConstants.ES_FIELD_PLAN_DESCRIPTION,
                            EsConstants.ES_FIELD_PLAN_ID, EsConstants.ES_FIELD_PLAN_NAME,
                            EsConstants.ES_FIELD_STATUS, EsConstants.ES_FIELD_VERSION
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_ARTIFACT_ID, EsConstants.ES_FIELD_CREATED_BY,
                            EsConstants.ES_FIELD_CREATED_ON, EsConstants.ES_FIELD_DELETED,
                            EsConstants.ES_FIELD_DESCRIPTION, EsConstants.ES_FIELD_GROUP_ID,
                            EsConstants.ES_FIELD_ID, EsConstants.ES_FIELD_NAME,
                            EsConstants.ES_FIELD_VERSION
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_FORM_TYPE, EsConstants.ES_FIELD_ICON,
                            EsConstants.ES_FIELD_ID, EsConstants.ES_FIELD_PLUGIN_ID,
                            EsConstants.ES_FIELD_DESCRIPTION, EsConstants.ES_FIELD_FORM,
                            EsConstants.ES_FIELD_NAME, EsConstants.ES_NESTED_FIELD_TEMPLATES_TEMPLATE,
                            EsConstants.ES_FIELD_DELETED
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_ROLE:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_AUTO_GRANT, EsConstants.ES_FIELD_CREATED_BY,
                            EsConstants.ES_FIELD_CREATED_ON, EsConstants.ES_FIELD_DESCRIPTION,
                            EsConstants.ES_FIELD_ID, EsConstants.ES_FIELD_NAME,
                            EsConstants.ES_FIELD_PERMISSIONS
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_CREATED_ON, EsConstants.ES_FIELD_ID,
                            EsConstants.ES_FIELD_ORGANIZATION_ID, EsConstants.ES_FIELD_ROLE_ID,
                            EsConstants.ES_FIELD_USER_ID
                    };
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_USER:
                    fieldNames = new String[]{
                            EsConstants.ES_FIELD_EMAIL, EsConstants.ES_FIELD_FULL_NAME,
                            EsConstants.ES_FIELD_JOINED_ON, EsConstants.ES_FIELD_USERNAME
                    };
                    break;
                default:
                    break;
            }
        }
        if (fieldNames != null) {
            setIndexMapping(fieldNames, indexFieldProperties);
        }
        return indexFieldProperties;
    }

    /**
     * Sets Keyword Type to index field
     *
     * @param fieldName the field name
     */
    private static void setKeywordTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create keyword type property
        Map<String, Object> keywordTypeProperty = new HashMap<>();
        keywordTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_KEYWORD);
        setFieldMapping(fieldName, keywordTypeProperty, indexFieldProperties);
    }

    /**
     * Sets Date Type to index field
     *
     * @param fieldName            the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setDateTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create date type property
        Map<String, Object> dateTypeProperty = new HashMap<>();
        dateTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_DATE);
        setFieldMapping(fieldName, dateTypeProperty, indexFieldProperties);
    }

    /**
     * Sets Boolean Type to index field
     *
     * @param fieldName            the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setBooleanTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create boolean type property
        Map<String, Object> booleanTypeProperty = new HashMap<>();
        booleanTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_BOOLEAN);
        setFieldMapping(fieldName, booleanTypeProperty, indexFieldProperties);
    }

    /**
     * Sets Ip Type to index field
     *
     * @param fieldName            the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setIpTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create ip type property
        Map<String, Object> ipTypeProperty = new HashMap<>();
        ipTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_IP);
        setFieldMapping(fieldName, ipTypeProperty, indexFieldProperties);
    }

    /**
     * Sets Long Type to index field
     *
     * @param fieldName            the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setLongTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create long type property
        Map<String, Object> longTypeProperty = new HashMap<>();
        longTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_LONG);
        setFieldMapping(fieldName, longTypeProperty, indexFieldProperties);
    }

    /**
     * Sets Object Type to index field
     *
     * @param fieldName            the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setObjectTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create object type property
        Map<String, Object> objectTypeProperty = new HashMap<>();
        objectTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_OBJECT);
        setFieldMapping(fieldName, objectTypeProperty, indexFieldProperties);
    }

    /**
     * Sets Binary Type to index field
     *
     * @param fieldName            the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setBinaryTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create binary type property
        Map<String, Object> binaryTypeProperty = new HashMap<>();
        binaryTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_BINARY);
        setFieldMapping(fieldName, binaryTypeProperty, indexFieldProperties);
    }

    /**
     * Set text Type to index field
     *
     * @param fieldName            the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setTextTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create type properties
        Map<String, Object> textTypeProperty = new HashMap<>();
        Map<String, Object> keywordTypeProperty = new HashMap<>();
        Map<String, Object> multiFieldProperty = new HashMap<>();

        textTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_TEXT);
        textTypeProperty.put("fielddata", true);
        keywordTypeProperty.put("type", EsConstants.ES_MAPPING_TYPE_KEYWORD);
        keywordTypeProperty.put("ignore_above", 256);
        multiFieldProperty.put("keyword", keywordTypeProperty);
        textTypeProperty.put("fields", multiFieldProperty);
        setFieldMapping(fieldName, textTypeProperty, indexFieldProperties);
    }

    /**
     * Does the request to set the field mapping to elasticsearch
     *
     * @param fieldName            the field name
     * @param typeProperty         the type property
     * @param indexFieldProperties the index field properties
     */
    private static void setFieldMapping(String fieldName, Map<String, Object> typeProperty, Map<String, Object> indexFieldProperties) {
        if (indexFieldProperties.isEmpty()) {
            indexFieldProperties.put("properties", new HashMap<String, Object>());
        }
        ((Map<String, Object>) indexFieldProperties.get("properties")).put(fieldName, typeProperty);
    }

    /**
     * Maps all fields according to their type
     *
     * @param fieldNames           array of field names for this index
     * @param indexFieldProperties the index field properties
     */
    private static void setIndexMapping(String[] fieldNames, Map<String, Object> indexFieldProperties) {
        for (String fieldName : fieldNames) {
            String mappingType = EsConstants.esFieldMapping.get(fieldName);
            switch (mappingType) {
                case EsConstants.ES_MAPPING_TYPE_BINARY:
                    setBinaryTypeToIndexField(fieldName, indexFieldProperties);
                    break;
                case EsConstants.ES_MAPPING_TYPE_BOOLEAN:
                    setBooleanTypeToIndexField(fieldName, indexFieldProperties);
                    break;
                case EsConstants.ES_MAPPING_TYPE_DATE:
                    setDateTypeToIndexField(fieldName, indexFieldProperties);
                    break;
                case EsConstants.ES_MAPPING_TYPE_IP:
                    setIpTypeToIndexField(fieldName, indexFieldProperties);
                    break;
                case EsConstants.ES_MAPPING_TYPE_KEYWORD:
                    setKeywordTypeToIndexField(fieldName, indexFieldProperties);
                    break;
                case EsConstants.ES_MAPPING_TYPE_LONG:
                    setLongTypeToIndexField(fieldName, indexFieldProperties);
                    break;
                case EsConstants.ES_MAPPING_TYPE_OBJECT:
                    setObjectTypeToIndexField(fieldName, indexFieldProperties);
                    break;
                case EsConstants.ES_MAPPING_TYPE_TEXT:
                    setTextTypeToIndexField(fieldName, indexFieldProperties);
                    break;
            }
        }
    }

    /**
     * Computes the full index name for an index
     *
     * @param indexPrefix  the index prefix
     * @param indexPostfix the index postfix
     * @return the full index name
     */
    public static String getFullIndexName(String indexPrefix, String indexPostfix) {
        String fullIndexName = indexPrefix;
        if (!indexPostfix.isEmpty()) {
            fullIndexName += "_" + indexPostfix;
        }
        return fullIndexName.toLowerCase();
    }

}
