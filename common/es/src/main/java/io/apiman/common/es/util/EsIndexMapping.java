package io.apiman.common.es.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides helper functions for Field type mapping of Elasticsearch
 */
public class EsIndexMapping {

    /**
     * Add document mapping for each elasticsearch field
     * @param indexPrefix the index prefix
     * @param indexPostfix the index postfix
     * @return document mapping for index
     */
    public static Map<String, Object> getDocumentMapping(String indexPrefix, String indexPostfix) {

        Map<String, Object> indexFieldProperties = new HashMap<String, Object>();

        if (indexPrefix.equals(EsConstants.GATEWAY_INDEX_NAME)) {
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_VERSION, indexFieldProperties);
        }
        // rule for gateway and manager
        if (indexPostfix.equals(EsConstants.INDEX_APIS)) {
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_API_ID, indexFieldProperties);
        }
        // rule for gateway and manager
        if (indexPostfix.equals(EsConstants.INDEX_CLIENTS)) {
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_ID, indexFieldProperties);
        }
        if (indexPrefix.equals(EsConstants.METRICS_INDEX_NAME)) {
            // set keyword types
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_API_ID, indexFieldProperties);
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_API_ORG_ID, indexFieldProperties);
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_ID, indexFieldProperties);
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_ORG_ID, indexFieldProperties);
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_PLAN_ID, indexFieldProperties);
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_API_VERSION, indexFieldProperties);
            addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_VERSION, indexFieldProperties);

            addBooleanTypeToIndexField(EsConstants.ES_FIELD_ERROR, indexFieldProperties);
            addBooleanTypeToIndexField(EsConstants.ES_FIELD_FAILURE, indexFieldProperties);

            // set date types
            addDateTypeToIndexField(EsConstants.ES_FIELD_REQUEST_START, indexFieldProperties);
            addDateTypeToIndexField(EsConstants.ES_FIELD_REQUEST_END, indexFieldProperties);
            addDateTypeToIndexField(EsConstants.ES_FIELD_API_START, indexFieldProperties);
            addDateTypeToIndexField(EsConstants.ES_FIELD_API_END, indexFieldProperties);

            // set ip type
            addIpTypeToIndexField(EsConstants.ES_FIELD_REMOTE_ADDR, indexFieldProperties);
        }

        if (indexPrefix.equals(EsConstants.MANAGER_INDEX_NAME)) {

            switch (indexPostfix) {
                case EsConstants.INDEX_MANAGER_POSTFIX_API:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_NAME, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_API_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_STATUS, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_MODIFIED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_PUBLISHED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_RETIRED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_CLIENT:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_NAME, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_STATUS, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_MODIFIED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_PUBLISHED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_RETIRED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ENTITY_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ENTITY_TYPE, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ID, indexFieldProperties);
                    setFieldDataToIndexTextField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_MODIFIED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_USER:
                    setFieldDataToIndexTextField(EsConstants.ES_FIELD_FULL_NAME, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_JOINED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_GROUP_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ARTIFACT_ID, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_API_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_API_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_CLIENT_VERSION, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_PLAN:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_NAME, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_PLAN_ID, indexFieldProperties);
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_STATUS, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_MODIFIED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_LOCKED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    addDateTypeToIndexField(EsConstants.ES_FIELD_MODIFIED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_ROLE:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ID, indexFieldProperties);
                    setFieldDataToIndexTextField(EsConstants.ES_FIELD_NAME, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ORGANIZATION_ID, indexFieldProperties);
                    // date types
                    addDateTypeToIndexField(EsConstants.ES_FIELD_CREATED_ON, indexFieldProperties);
                    break;
                case EsConstants.INDEX_MANAGER_POSTFIX_DEVELOPER:
                    addKeywordTypeToIndexField(EsConstants.ES_FIELD_ID, indexFieldProperties);
                    break;
                default:
                    break;
            }
        }

        return indexFieldProperties;
    }

    /**
     * Adds Keyword Type to index field
     * @param fieldName the field name
     */
    private static void addKeywordTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create keyword type property
        Map<String, Object> keywordTypeProperty = new HashMap<>();
        keywordTypeProperty.put("type", "keyword");
        addFieldMapping(fieldName, keywordTypeProperty, indexFieldProperties);
    }

    /**
     * Adds Date Type to index field
     * @param fieldName the field name
     * @param indexFieldProperties the index field properties
     */
    private static void addDateTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create keyword type property
        Map<String, Object> keywordTypeProperty = new HashMap<>();
        keywordTypeProperty.put("type", "date");
        addFieldMapping(fieldName, keywordTypeProperty, indexFieldProperties);
    }

    /**
     * Adds Boolean Type to index field
     * @param fieldName the field name
     * @param indexFieldProperties the index field properties
     */
    private static void addBooleanTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create keyword type property
        Map<String, Object> keywordTypeProperty = new HashMap<>();
        keywordTypeProperty.put("type", "boolean");
        addFieldMapping(fieldName, keywordTypeProperty, indexFieldProperties);
    }

    /**
     * Adds Ip Type to index field
     * @param fieldName the field name
     * @param indexFieldProperties the index field properties
     */
    private static void addIpTypeToIndexField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create keyword type property
        Map<String, Object> keywordTypeProperty = new HashMap<>();
        keywordTypeProperty.put("type", "ip");
        addFieldMapping(fieldName, keywordTypeProperty, indexFieldProperties);
    }

    /**
     * Set field data to index text field
     * @param fieldName the field name
     * @param indexFieldProperties the index field properties
     */
    private static void setFieldDataToIndexTextField(String fieldName, Map<String, Object> indexFieldProperties) {
        // create keyword type property
        Map<String, Object> textTypeProperty = new HashMap<>();
        textTypeProperty.put("type", "text");
        textTypeProperty.put("fielddata", true);
        addFieldMapping(fieldName, textTypeProperty, indexFieldProperties);
    }

    /**
     * Does the request to add the field mapping to elasticsearch
     * @param fieldName the field name
     * @param typeProperty the type property
     * @param indexFieldProperties the index field properties
     */
    private static void addFieldMapping(String fieldName, Map<String, Object> typeProperty, Map<String, Object> indexFieldProperties) {
        if (indexFieldProperties.isEmpty()) {
            indexFieldProperties.put("properties", new HashMap<String, Object>());
        }
        ((Map<String, Object>) indexFieldProperties.get("properties")).put(fieldName, typeProperty);
    }

    /**
     * Computes the full index name for an index
     * @param indexPrefix the index prefix
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
