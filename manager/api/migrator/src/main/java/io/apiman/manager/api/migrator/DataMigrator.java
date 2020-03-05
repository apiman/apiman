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

import io.apiman.common.logging.impl.SystemOutLogger;
import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.migrator.i18n.Messages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Used to migrate exported data from an older version of apiman to the 
 * latest version.  This is useful when upgrading from older versions of
 * apiman to the latest.  
 * @author eric.wittmann@gmail.com
 */
@Dependent
public class DataMigrator implements IReaderHandler {

    @Inject
    private Version version;
    @Inject @ApimanLogger(DataMigrator.class)
    private IApimanLogger logger;

    private IDataMigratorWriter writer;
    private VersionMigratorChain chain;
    
    /**
     * Constructor.
     */
    public DataMigrator() {
    }
    
    /**
     * Migrate the data export file (fromSource) from whatever version it
     * is to the latest apiman version format and write the result to a
     * file (toDest).
     * @param fromSource
     * @param toDest
     */
    public void migrate(File fromSource, File toDest) {
        IReaderHandler readerHandler = this;
        try (IDataMigratorReader reader = new JsonDataMigratorReader(fromSource);
             IDataMigratorWriter writer = new JsonDataMigratorWriter(toDest)) {
            this.writer = writer;
            reader.read(readerHandler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Migrate the data export file (fromSource) from whatever version it
     * is to the latest apiman version format and write the result to a
     * file (toDest).  
     * 
     * Note: this method will automatically close the streams - the caller
     * does not need to do this.
     * @param fromSource
     * @param toDest
     */
    public void migrate(InputStream fromSource, OutputStream toDest) {
        IReaderHandler readerHandler = this;
        try (IDataMigratorReader reader = new JsonDataMigratorReader(fromSource);
             IDataMigratorWriter writer = new JsonDataMigratorWriter(toDest)) {
            this.writer = writer;
            reader.read(readerHandler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Main method - used when running the data migrator in standalone
     * mode.
     * @param args
     */
    public static void main(String[] args) {
        File from;
        File to;
        
        if (args.length < 2) {
            System.out.println("Usage:  DataMigrator <pathToSourceFile> <pathToDestFile>"); //$NON-NLS-1$
            return;
        }
        
        String frompath = args[0];
        String topath = args[1];
        
        from = new File(frompath);
        to = new File(topath);

        System.out.println("Starting data migration."); //$NON-NLS-1$
        System.out.println("  From: " + from); //$NON-NLS-1$
        System.out.println("  To:   " + to); //$NON-NLS-1$
        DataMigrator migrator = new DataMigrator();
        migrator.setLogger(new SystemOutLogger());
        Version version = new Version();
        migrator.setVersion(version);
        migrator.migrate(from, to);
    }

    /**
     * @see io.apiman.manager.api.migrator.IReaderHandler#onMetaData(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void onMetaData(ObjectNode node) throws IOException {
        String fromVersion = node.get("apimanVersion").asText(); //$NON-NLS-1$
        String toVersion = this.getVersion().getVersionString();

        chain = VersionMigrators.chain(fromVersion, toVersion);
        
        if (chain.hasMigrators()) {
            logger.info(Messages.i18n.format("DataMigrator.MigratingNow", fromVersion)); //$NON-NLS-1$
            chain.migrateMetaData(node);
            node.put("apimanVersion", toVersion); //$NON-NLS-1$
        }
        writer.writeMetaData(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IReaderHandler#onUser(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void onUser(ObjectNode node) throws IOException {
        if (chain.hasMigrators()) {
            logger.info(Messages.i18n.format("DataMigrator.MigratingUser", node.get("username"))); //$NON-NLS-1$ //$NON-NLS-2$
            chain.migrateUser(node);
        }
        writer.writeUser(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IReaderHandler#onGateway(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void onGateway(ObjectNode node) throws IOException {
        if (chain.hasMigrators()) {
            logger.info(Messages.i18n.format("DataMigrator.MigratingGateway", node.get("name"))); //$NON-NLS-1$ //$NON-NLS-2$
            chain.migrateGateway(node);
        }
        writer.writeGateway(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IReaderHandler#onPlugin(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void onPlugin(ObjectNode node) throws IOException {
        if (chain.hasMigrators()) {
            logger.info(Messages.i18n.format("DataMigrator.MigratingPlugin", node.get("name"))); //$NON-NLS-1$ //$NON-NLS-2$
            chain.migratePlugin(node);
        }
        writer.writePlugin(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IReaderHandler#onRole(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void onRole(ObjectNode node) throws IOException {
        if (chain.hasMigrators()) {
            logger.info(Messages.i18n.format("DataMigrator.MigratingRole", node.get("name"))); //$NON-NLS-1$ //$NON-NLS-2$
            chain.migrateRole(node);
        }
        writer.writeRole(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IReaderHandler#onPolicyDefinition(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void onPolicyDefinition(ObjectNode node) throws IOException {
        if (chain.hasMigrators()) {
            logger.info(Messages.i18n.format("DataMigrator.MigratingPolicyDef", node.get("name"))); //$NON-NLS-1$ //$NON-NLS-2$
            chain.migratePolicyDefinition(node);
        }
        writer.writePolicyDefinition(node);
    }

    /**
     * @see io.apiman.manager.api.migrator.IReaderHandler#onOrg(com.fasterxml.jackson.databind.node.ObjectNode)
     */
    @Override
    public void onOrg(ObjectNode node) throws IOException {
        if (chain.hasMigrators()) {
            logger.info(Messages.i18n.format("DataMigrator.MigratingOrg", node.get("OrganizationBean").get("name"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            chain.migrateOrg(node);
        }
        writer.writeOrg(node);
    }

    /**
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    /**
     * @return the logger
     */
    public IApimanLogger getLogger() {
        return logger;
    }

    /**
     * @param logger the logger to set
     */
    public void setLogger(IApimanLogger logger) {
        this.logger = logger;
    }

}
