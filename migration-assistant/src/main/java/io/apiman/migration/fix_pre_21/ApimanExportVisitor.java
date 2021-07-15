/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apiman.migration.fix_pre_21;

import io.apiman.migration.exceptions.CliException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simplified visitor for iterating Org -> Api -> API Version
 * <p>
 * Ordering of visitor is guaranteed to be logically consistent.
 * i.e.: Org[FooOrg] -> Api [FooOrg, ApiEcho] -> API Version [FooOrg, ApiEcho, Version1]
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public abstract class ApimanExportVisitor {
    private static final Logger LOGGER = LogManager.getLogger(ApimanExportVisitor.class);

    public ApimanExportVisitor(JsonNode root) throws Exception {
        parse(root);
    }

    /**
     * Provide the root node of an Apiman export file and iterate through, visiting Org, Api, ApiVersion.
     */
    public void parse(JsonNode root) throws Exception {
        ArrayNode orgArray = getOrThrowArray(root, "Orgs");

        String orgId;
        String apiId;

        for (JsonNode orgNode : orgArray) {
            orgId = getOrThrow(orgNode, "OrganizationBean.id").asText();
            ArrayNode apiArray = getOrThrowArray(orgNode, "Apis");

            LOGGER.trace("Visiting Org: {}", orgId);
            visitOrg(orgId, orgNode);

            for (JsonNode apiNode : apiArray) {
                apiId = getOrThrow(apiNode, "ApiBean.id").asText();
                ArrayNode versionArray = getOrThrowArray(apiNode, "Versions");

                LOGGER.trace("Visiting API: {}", apiId);
                visitApi(apiId, apiNode);

                for (JsonNode versionNode : versionArray) {
                    LOGGER.trace("Visiting API Version: {}", versionNode);
                    visitApiVersion(orgId, apiId, versionNode);
                }
            }
        }
    }

    /**
     * Override this method to visit an Organization
     */
    public void visitOrg(String orgId, JsonNode orgNode) throws Exception {
    }

    /**
     * Override this method to visit an API
     */
    public void visitApi(String apiId, JsonNode apiNode) throws Exception {
    }

    /**
     * Override this method to visit an API Version
     */
    public void visitApiVersion(String orgId, String apiId, JsonNode apiVersionNode) throws Exception {
    }

    private ArrayNode getOrThrowArray(JsonNode node, String key) {
        return (ArrayNode) getOrThrow(node, key);
    }

    private JsonNode getOrThrow(JsonNode node, String key) {
        String[] path = key.split("\\.");

        JsonNode currentNode = node;

        for (String pathElement : path) {
            if (currentNode.hasNonNull(pathElement)) {
                currentNode = currentNode.get(pathElement);
            } else {
                throw new CliException("Required element '" + pathElement + "' does not exist from " + key + ". "
                    + "File may be corrupted or altered?");
            }
        }

        return currentNode;
    }
}
