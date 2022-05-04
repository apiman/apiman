package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.events.EventService;
import io.apiman.manager.api.exportimport.json.JsonImportReader;
import io.apiman.manager.api.exportimport.manager.StorageImportDispatcher;
import io.apiman.manager.api.exportimport.read.IImportReader;
import io.apiman.manager.api.migrator.DataMigrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Transactional
@ApplicationScoped
public class ImportExportService {
    private final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(ImportExportService.class);
    private DataMigrator migrator;
    private StorageImportDispatcher importer;
    private EventService eventService;

    @Inject
    public ImportExportService(DataMigrator migrator, StorageImportDispatcher importer, EventService eventService) {
        this.migrator = migrator;
        this.importer = importer;
        this.eventService = eventService;
    }

    public ImportExportService() {
    }

    public void fullImport(File importFile, IApimanLogger logger) throws IOException {
        eventService.lock();
        try {
            eventService.deactivate();
            LOGGER.debug("Acquired exclusive lock on EventService and have deactivated event dispatch (all events will be dropped)");
            doImport(importFile, logger);
        } finally {
            eventService.activate();
            eventService.unlock();
            LOGGER.debug("Unlocked EventService and re-enabled event dispatch (events will now be sent)");
        }
    }

    private void doImport(File importFile, IApimanLogger logger) throws IOException {

        File migratedImportFile = File.createTempFile("apiman_import_migrated", ".json"); //$NON-NLS-1$ //$NON-NLS-2$
        migratedImportFile.deleteOnExit();

        // Migrate the data (if necessary)
        migrator.migrate(importFile, migratedImportFile, logger);

        // Now import the migrated data
        InputStream importData = null;
        IImportReader reader;
        try {
            importData = new FileInputStream(migratedImportFile);
            reader = new JsonImportReader(logger, importData);
        } catch (IOException e) {
            IOUtils.closeQuietly(importData);
            throw new UncheckedIOException(e);
        }

        try {
            importer.start(migratedImportFile.getAbsolutePath(), logger);
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
}
