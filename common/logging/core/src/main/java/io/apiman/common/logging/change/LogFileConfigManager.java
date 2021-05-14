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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Log file config manager.
 *
 * <p>Watches a configuration file on the local filesystem.
 *
 * <p>If the file is changed, the data inside will be parsed into a {@link LoggingChangeRequest} and the
 * changeHandler will be invoked.
 *
 * <p>This provides a simple way for deployments on the same node (e.g. a container) to synchronise their
 * logging configuration at runtime, despite being in separate classloaders.
 */
public class LogFileConfigManager {
    private final Path fileToWatch;
    private final WatchService watchService;
    // WatchService can only watch directories, not individual file(s).
    private final Path fileParent;
    private final SimpleChangeRequestHandler changeHandler;
    private final ObjectMapper om = new ObjectMapper();
    private boolean running = true;

    /**
     * Instantiates a new log file config manager.
     *
     * @param changeHandler the change handler, called when the logging configuration has been changed
     * @throws IOException the io exception
     */
    public LogFileConfigManager(SimpleChangeRequestHandler changeHandler) throws IOException {
        this.fileToWatch = Paths.get(System.getProperty("java.io.tmpdir"), "local-deployment-logger-config");
        this.fileParent = fileToWatch.getParent();
        this.changeHandler = changeHandler;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    /**
     * Write a logging change request.
     *
     * @param loggingChangeRequest the logging change request to write
     * @throws IOException the io exception
     */
    public void write(LoggingChangeRequest loggingChangeRequest) throws IOException {
        Files.write(fileToWatch, om.writeValueAsBytes(loggingChangeRequest));
    }

    /**
     * Watch the log file.
     */
    public void watch() {
        Runnable runnable = () -> {
            try {
                doPollingLoop();
            } catch (IOException ioe) {
                // LOGGER.error(ioe, "Unable to watch logging config file: {0}", ioe.getMessage());
                throw new UncheckedIOException(ioe);
            } catch (InterruptedException e) {
                // LOGGER.error(e, "Log file watcher was interrupted: {0}", e.getMessage());
            } finally {
                running = false;
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

        while (running) {
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
    }

    private void trigger() throws IOException {
        LoggingChangeRequest change = om.readValue(fileToWatch.toFile(), LoggingChangeRequest.class);
        // LOGGER.trace("Logging change request: {}", change);
        this.changeHandler.handle(change);
    }

    /**
     * Simple change request handler.
     *
     * <p>We need to avoid a cyclical dependency on Apiman Core, so we just create a one-use interface.
     */
    @FunctionalInterface
    public interface SimpleChangeRequestHandler {

        /**
         * Called when an async result is available.
         *
         * @param loggingChangeRequest the logging change request
         */
        void handle(LoggingChangeRequest loggingChangeRequest);
    }
}
