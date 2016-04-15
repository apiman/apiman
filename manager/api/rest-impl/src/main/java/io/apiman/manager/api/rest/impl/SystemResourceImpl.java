/*
 * Copyright 2014 JBoss Inc
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

package io.apiman.manager.api.rest.impl;

import io.apiman.common.util.MediaType;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.download.DownloadType;
import io.apiman.manager.api.beans.system.SystemStatusBean;
import io.apiman.manager.api.config.Version;
import io.apiman.manager.api.core.IDownloadManager;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.json.JsonExportWriter;
import io.apiman.manager.api.exportimport.json.JsonImportReader;
import io.apiman.manager.api.exportimport.manager.StorageExporter;
import io.apiman.manager.api.exportimport.manager.StorageImportDispatcher;
import io.apiman.manager.api.exportimport.read.IImportReader;
import io.apiman.manager.api.exportimport.write.IExportWriter;
import io.apiman.manager.api.migrator.DataMigrator;
import io.apiman.manager.api.rest.contract.ISystemResource;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;

/**
 * Implementation of the System API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class SystemResourceImpl implements ISystemResource {

    @Inject
    private IStorage storage;
    @Inject
    private ISecurityContext securityContext;
    @Inject
    private Version version;
    @Inject @ApimanLogger(IImportReader.class)
    private IApimanLogger importLogger;
    @Inject @ApimanLogger(IExportWriter.class)
    private IApimanLogger exportLogger;
    @Inject
    private StorageExporter exporter;
    @Inject
    private StorageImportDispatcher importer;
    @Inject
    private DataMigrator migrator;
    @Inject
    private IDownloadManager downloadManager;

    @Context
    private HttpServletRequest request;

    /**
     * Constructor.
     */
    public SystemResourceImpl() {
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISystemResource#getStatus()
     */
    @Override
    public SystemStatusBean getStatus() {
        SystemStatusBean rval = new SystemStatusBean();
        rval.setId("apiman-manager-api"); //$NON-NLS-1$
        rval.setName("API Manager REST API"); //$NON-NLS-1$
        rval.setDescription("The API Manager REST API is used by the API Manager UI to get stuff done.  You can use it to automate any apiman task you wish.  For example, create new Organizations, Plans, Clients, and APIs."); //$NON-NLS-1$
        rval.setMoreInfo("http://www.apiman.io/latest/api-manager-restdocs.html"); //$NON-NLS-1$
        rval.setUp(getStorage() != null);
        if (getVersion() != null) {
            rval.setVersion(getVersion().getVersionString());
            rval.setBuiltOn(getVersion().getVersionDate());
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISystemResource#exportData(java.lang.String)
     */
    @Override
    public Response exportData(String download) {
        if (BooleanUtils.toBoolean(download)) {
            try {
                DownloadBean dbean = downloadManager.createDownload(DownloadType.exportJson, "/system/export"); //$NON-NLS-1$
                return Response.ok(dbean, MediaType.APPLICATION_JSON).build();
            } catch (StorageException e) {
                throw new SystemErrorException(e);
            }
        } else {
            if (!securityContext.isAdmin())
                throw ExceptionFactory.notAuthorizedException();
            return exportData();
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISystemResource#exportData()
     */
    @Override
    public Response exportData() {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                IExportWriter writer = new JsonExportWriter(os, exportLogger);
                getExporter().init(writer);
                getExporter().export();
                os.flush();
            }
        };
        return Response
                .ok(stream, MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=api-manager-export.json") //$NON-NLS-1$ //$NON-NLS-2$
                .build();
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISystemResource#importData()
     */
    @Override
    public Response importData() {
        if (!securityContext.isAdmin())
            throw ExceptionFactory.notAuthorizedException();

        // First, stream the import data to a temporary file.  We do this so
        // that we can stream the import logging statements back to the HTTP
        // response.  We can't stream the inbound data into the importer
        // *and* stream the importer's logging output back to the HTTP
        // response at the same time due to the nature of HTTP.
        File tempFile;
        InputStream data;
        try {
            tempFile = File.createTempFile("apiman_import", ".json"); //$NON-NLS-1$ //$NON-NLS-2$
            tempFile.deleteOnExit();
            data = request.getInputStream();
            FileUtils.copyInputStreamToFile(data, tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final File importFile = tempFile;

        // Next, do the import and stream the import logging output back to
        // the HTTP response output stream.
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                final PrintWriter writer = new PrintWriter(output);
                IApimanLogger logger = new IApimanLogger() {
                    @Override
                    public void warn(String message) {
                        writer.println("WARN: " + message); //$NON-NLS-1$
                        writer.flush();
                    }

                    @Override
                    public void trace(String message) {
                        writer.println("TRACE: " + message); //$NON-NLS-1$
                        writer.flush();
                    }

                    @Override
                    public void info(String message) {
                        writer.println("INFO: " + message); //$NON-NLS-1$
                        writer.flush();
                    }

                    @Override
                    public void error(String message, Throwable error) {
                        writer.println("ERROR: " + message); //$NON-NLS-1$
                        error.printStackTrace(writer);
                        writer.flush();
                    }

                    @Override
                    public void error(Throwable error) {
                        writer.println("ERROR: " + error.getMessage()); //$NON-NLS-1$
                        error.printStackTrace(writer);
                        writer.flush();
                    }

                    @Override
                    public void debug(String message) {
                        writer.println("DEBUG: " + message); //$NON-NLS-1$
                        writer.flush();
                    }
                };

                File migratedImportFile = File.createTempFile("apiman_import_migrated", ".json"); //$NON-NLS-1$ //$NON-NLS-2$
                migratedImportFile.deleteOnExit();
                
                // Migrate the data (if necessary)
                migrator.setLogger(logger);
                migrator.migrate(importFile, migratedImportFile);
                
                // Now import the migrated data
                InputStream importData = null;
                IImportReader reader;
                try {
                    importData = new FileInputStream(migratedImportFile);
                    reader = new JsonImportReader(logger, importData);
                } catch (IOException e) {
                    IOUtils.closeQuietly(importData);
                    throw new SystemErrorException(e);
                }

                try {
                    importer.setLogger(logger);
                    importer.start();
                    reader.setDispatcher(importer);
                    reader.read();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(importData);
                    FileUtils.deleteQuietly(importFile);
                    FileUtils.deleteQuietly(migratedImportFile);
                }
            }
        };

        return Response.ok(stream).build();
    }

    /**
     * @return the storage
     */
    public IStorage getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(IStorage storage) {
        this.storage = storage;
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
     * @return the exporter
     */
    public StorageExporter getExporter() {
        return exporter;
    }

    /**
     * @param exporter the exporter to set
     */
    public void setExporter(StorageExporter exporter) {
        this.exporter = exporter;
    }

    /**
     * @return the securityContext
     */
    public ISecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * @param securityContext the securityContext to set
     */
    public void setSecurityContext(ISecurityContext securityContext) {
        this.securityContext = securityContext;
    }
}
