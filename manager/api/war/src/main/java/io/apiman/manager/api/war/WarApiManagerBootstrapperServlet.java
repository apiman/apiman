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
package io.apiman.manager.api.war;

import io.apiman.common.config.ConfigFactory;
import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.json.JsonImportReader;
import io.apiman.manager.api.exportimport.manager.StorageImportDispatcher;
import io.apiman.manager.api.exportimport.read.IImportReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Performs some basic bootstrapping tasks for the API Manager.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarApiManagerBootstrapperServlet extends HttpServlet {

    private static final long serialVersionUID = -362982634664023862L;
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(WarApiManagerBootstrapperServlet.class);

    @Inject
    private StorageImportDispatcher importer;

    private static Configuration config;
    static {
        config = ConfigFactory.createConfig();
    }

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        Thread t = new Thread(() -> {
            if (isImportEnabled()){
                File dataDir = getDataDir();
                if (dataDir != null && dataDir.isDirectory()) {
                    LOGGER.info("Checking for bootstrap files in " + dataDir); //$NON-NLS-1$
                    Collection<File> files = FileUtils.listFiles(dataDir, new String[] { "json" }, false); //$NON-NLS-1$
                    TreeSet<File> sortedFiles = new TreeSet<>(files);
                    for (File file : sortedFiles) {
                        File alreadyProcessed = new File(file.getAbsolutePath() + ".imported"); //$NON-NLS-1$
                        if (!alreadyProcessed.isFile()) {
                            doImport(file);
                            // Do not create the *.imported file anymore, because it is not docker save
                        } else {
                            LOGGER.debug("Skipping (already processed) file: " + file); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * @param file the file to import
     */
    private void doImport(File file) {
        InputStream importData = null;
        IImportReader reader;
        try {
            importData = new FileInputStream(file);
            reader = new JsonImportReader(LOGGER, importData);
        } catch (IOException e) {
            IOUtils.closeQuietly(importData);
            LOGGER.error(e);
            return;
        }

        try {
            importer.start(file.getAbsolutePath());
            reader.setDispatcher(importer);
            reader.read();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(importData);
        }
    }

    /**
     * Get the data directory
     */
    private static File getDataDir() {
        File rval = null;

        // First check to see if a data directory has been explicitly configured via system property
        String dataDir = config.getString("apiman.bootstrap.data_dir"); //$NON-NLS-1$
        if (dataDir != null) {
            rval = new File(dataDir);
        }

        // If that wasn't set, then check to see if we're running in wildfly/eap
        if (rval == null) {
            dataDir = System.getProperty("jboss.server.data.dir"); //$NON-NLS-1$
            if (dataDir != null) {
                rval = new File(dataDir, "bootstrap"); //$NON-NLS-1$
            }
        }

        // If that didn't work, try to locate a tomcat data directory
        if (rval == null) {
            dataDir = System.getProperty("catalina.home"); //$NON-NLS-1$
            if (dataDir != null) {
                rval = new File(dataDir, "data/bootstrap"); //$NON-NLS-1$
            }
        }

        // If all else fails, just let it return null
        return rval;
    }

    private boolean isImportEnabled(){
        return !Boolean.getBoolean("apiman.bootstrap.disabled");
    }
}
