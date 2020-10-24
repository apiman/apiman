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

package io.apiman.manager.api.migrator;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Models a chain of version migrators.  Used to apply a set of migrators to a 
 * piece of data.
 * @author eric.wittmann@gmail.com
 */
public class VersionMigratorChain implements IVersionMigrator {
    
    private final List<IVersionMigrator> migrators;

    /**
     * Constructor.
     */
    public VersionMigratorChain(List<IVersionMigrator> migrators) {
        this.migrators = migrators;
    }
    
    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateMetaData(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateMetaData(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migrateMetaData(node);
        }
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateUser(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateUser(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migrateUser(node);
        }
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateGateway(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateGateway(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migrateGateway(node);
        }
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migratePlugin(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migratePlugin(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migratePlugin(node);
        }
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateRole(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateRole(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migrateRole(node);
        }
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migratePolicyDefinition(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migratePolicyDefinition(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migratePolicyDefinition(node);
        }
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateOrg(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateOrg(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migrateOrg(node);
        }
    }

    /**
     * @see io.apiman.manager.api.migrator.IVersionMigrator#migrateDeveloper(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void migrateDeveloper(ObjectNode node) {
        for (IVersionMigrator migrator : migrators) {
            migrator.migrateDeveloper(node);
        }
    }

    /**
     * @return true if there is at least one migrator in the chain
     */
    public boolean hasMigrators() {
        return !migrators.isEmpty();
    }

}
