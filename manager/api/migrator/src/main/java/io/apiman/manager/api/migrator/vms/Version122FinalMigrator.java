/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.manager.api.migrator.vms;

import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.UuidApiKeyGenerator;
import io.apiman.manager.api.migrator.IVersionMigrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author eric.wittmann@gmail.com
 */
public class Version122FinalMigrator implements IVersionMigrator {
    
    IApiKeyGenerator keyGenerator = new UuidApiKeyGenerator();
    
    /**
     * Constructor.
     */
    public Version122FinalMigrator() {
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateMetaData(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateMetaData(ObjectNode node) {
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateUser(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateUser(ObjectNode node) {
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateGateway(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateGateway(ObjectNode node) {
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migratePlugin(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migratePlugin(ObjectNode node) {
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateRole(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateRole(ObjectNode node) {
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migratePolicyDefinition(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migratePolicyDefinition(ObjectNode node) {
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateOrg(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateOrg(ObjectNode node) {
        ArrayNode clients = (ArrayNode) node.get("Clients"); //$NON-NLS-1$
        if (clients != null && clients.size() > 0) {
            for (JsonNode clientNode : clients) {
                ObjectNode client = (ObjectNode) clientNode;
                ArrayNode versions = (ArrayNode) client.get("Versions"); //$NON-NLS-1$
                if (versions != null && versions.size() > 0) {
                    for (JsonNode versionNode : versions) {
                        ObjectNode version = (ObjectNode) versionNode;
                        
                        ObjectNode clientVersionBean = (ObjectNode) version.get("ClientVersionBean"); //$NON-NLS-1$
                        clientVersionBean.put("apikey", keyGenerator.generate()); //$NON-NLS-1$
                        
                        ArrayNode contracts = (ArrayNode) version.get("Contracts"); //$NON-NLS-1$
                        if (contracts != null && contracts.size() > 0) {
                            for (JsonNode contractNode : contracts) {
                                ObjectNode contract = (ObjectNode) contractNode;
                                contract.remove("apikey"); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }
        }
        
    }
}
