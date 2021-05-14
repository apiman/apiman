package io.apiman.common.logging.change;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LogFileConfigManager {
    private static final String NODE_ID = UUID.randomUUID().toString();
    private final Path fileToWatch;
    private final WatchService watchService;
    // WatchService can only watch directories, not individual file(s).
    private final Path fileParent;
    private final SimpleChangeRequestHandler changeHandler;
    private final ObjectMapper om = new ObjectMapper();


    public LogFileConfigManager(SimpleChangeRequestHandler changeHandler) throws IOException {
        this.fileToWatch = getOrCreate();
        this.fileParent = fileToWatch.getParent();
        this.changeHandler = changeHandler;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    private Path getOrCreate() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "local-deployment-logger-config");
    }

    public void write(LoggingChangeRequest loggingChangeRequest) throws IOException {
        Files.write(fileToWatch, om.writeValueAsBytes(loggingChangeRequest));
    }

    public void watch() {
        Runnable runnable = () -> {
            try {
                doPollingLoop();
            } catch (IOException ioe) {
                // LOGGER.error(ioe, "Unable to watch logging config file: {0}", ioe.getMessage());
                throw new UncheckedIOException(ioe);
            } catch (InterruptedException e) {
                // LOGGER.error(e, "Log file watcher was interrupted: {0}", e.getMessage());
            }
        };
        final Thread watchThread = new Thread(runnable);
        watchThread.setDaemon(true);
        watchThread.start();
    }

    private void doPollingLoop() throws IOException, InterruptedException {
        fileParent.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.OVERFLOW
        );

        WatchKey keys = watchService.take();

        for (WatchEvent<?> event : keys.pollEvents()) {
            Kind<?> kind = event.kind();

            if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)
                || kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                // Sometimes the file will just be an empty file when someone replaces it,
                // so let's ignore zero-length.
                final Path changed = fileParent.resolve((Path) event.context());

                // If it's our special file and size is >0
                if (changed.getFileName().equals(fileToWatch.getFileName())
                    && Files.exists(changed) && Files.size(changed) > 0) {
                    // LOGGER.info("Log file config was changed, will reload log config: {0}", fileToWatch);
                    trigger();
                }
            } else if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                // LOGGER.warn("Filesystem overflow occurred when attempting to get logging file "
                //    + "changes, will speculatively trigger change handler: {0}" + event);
                trigger();
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                // LOGGER.info("Hmm!");
            }
        }
    }

    private void trigger() throws IOException {
        LoggingChangeRequest change = om.readValue(fileToWatch.toFile(), LoggingChangeRequest.class);
        // LOGGER.trace("Logging change request: {}", change);
        this.changeHandler.handle(change);
    }

    // We need to avoid creating a circular dependency back to apiman core.
    public interface SimpleChangeRequestHandler {

        /**
         * Called when an async result is available.
         */
        void handle(LoggingChangeRequest loggingChangeRequest);
    }
}
