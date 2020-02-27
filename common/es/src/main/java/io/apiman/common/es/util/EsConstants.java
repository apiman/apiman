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
            INDEX_MANAGER_POSTFIX_DEVELOPER
    };

    // es fields  (field names could be camelcase)
    public static final String ES_FIELD_ORGANIZATION_ID = "organizationId";
    public static final String ES_FIELD_VERSION = "version";
    public static final String ES_FIELD_API_ID = "apiId";
    public static final String ES_FIELD_CLIENT_ID = "clientId";
    public static final String ES_FIELD_API_ORG_ID = "apiOrgId";
    public static final String ES_FIELD_ENTITY_ID = "entityId";
    public static final String ES_FIELD_ORGANIZATION_NAME = "organizationName";
    public static final String ES_FIELD_CLIENT_ORG_ID = "clientOrgId";
    public static final String ES_FIELD_PLAN_ID = "planId";
    public static final String ES_FIELD_API_VERSION = "apiVersion";
    public static final String ES_FIELD_CLIENT_VERSION = "clientVersion";
    public static final String ES_FIELD_ERROR = "error";
    public static final String ES_FIELD_FAILURE = "failure";
    public static final String ES_FIELD_ID = "id";
    public static final String ES_FIELD_NAME = "name";
    public static final String ES_FIELD_STATUS = "status";
    public static final String ES_FIELD_ENTITY_TYPE = "entityType";
    public static final String ES_FIELD_FULL_NAME = "fullName";
    public static final String ES_FIELD_GROUP_ID = "groupId";
    public static final String ES_FIELD_ARTIFACT_ID = "artifactId";
    public static final String ES_FIELD_API_ORGANIZATION_ID = "apiOrganizationId";
    public static final String ES_FIELD_CLIENT_ORGANIZATION_ID = "clientOrganizationId";
    public static final String ES_FIELD_REQUEST_START = "requestStart";
    public static final String ES_FIELD_REQUEST_END = "requestEnd";
    public static final String ES_FIELD_API_START = "apiStart";
    public static final String ES_FIELD_API_END = "apiEnd";
    public static final String ES_FIELD_REMOTE_ADDR = "remoteAddr";
    public static final String ES_FIELD_CREATED_ON = "createdOn";
    public static final String ES_FIELD_MODIFIED_ON = "modifiedOn";
    public static final String ES_FIELD_JOINED_ON = "joinedOn";
    public static final String ES_FIELD_PUBLISHED_ON = "publishedOn";
    public static final String ES_FIELD_RETIRED_ON = "retiredOn";
    public static final String ES_FIELD_LOCKED_ON = "lockedOn";

    // caches the es version read from the property file
    private static String esVersion;
    /**
     * Reads the elasticsearch version from the maven-generated properties file
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
