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
package io.apiman.common.logging.log4j2;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.logging.LogFileWatcher;
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.FormattedMessageFactory;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Log4j2 logger factory.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@ApimanLoggerFactory("log4j2")
public class Log4j2LoggerFactory implements IDelegateFactory {

    private final FormattedMessageFactory formattedMessageFactory =  new FormattedMessageFactory();
    private final LoggerContext context = (LoggerContext) LogManager.getContext(false);
    private final LogFileWatcher logFileWatcher;
    private final Path logConfig = Paths.get(System.getProperty("apiman.dynamic-logging"));

    {
        try {
            logFileWatcher = new LogFileWatcher(
                logConfig,
                this::reloadLoggingConfig
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void reloadLoggingConfig() {
        System.out.println("reloading logging stuff, woo");
    }

    @Override
    public IApimanLogger createLogger(String name) {
        System.err.println(Log4j2LoggerFactory.class.getName() + "=" + id);
        return new Log4j2LoggerImpl(context.getLogger(name, formattedMessageFactory));
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        System.err.println(Log4j2LoggerFactory.class.getName() + "=" + id);
        return new Log4j2LoggerImpl(context.getLogger(klazz, formattedMessageFactory));
    }

    @Override
    public IDelegateFactory overrideLoggerConfig(File newLoggerConfig) {






        //System.setProperty("log4j.configurationFile", newLoggerConfig.getAbsolutePath());
        //context.setConfigLocation(newLoggerConfig.toURI());
        //context.updateLoggers();





//
//        String oldPath = System.getProperty("log4j.configurationFile");
//        try {
//            Files.copy(Paths.get(oldPath), Paths.get(oldPath + "_old" + UUID.randomUUID()));
//            Files.copy(newLoggerConfig.toPath(), Paths.get(oldPath), REPLACE_EXISTING);
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
        return this;
    }

    private void watchFile() {

    }
}
