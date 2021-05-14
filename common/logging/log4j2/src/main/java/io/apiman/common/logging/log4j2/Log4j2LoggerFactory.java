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
import io.apiman.common.logging.LogLevel;
import io.apiman.common.logging.change.LoggingChangeRequest;
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.FormattedMessageFactory;

/**
 * Log4j2 logger factory.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@ApimanLoggerFactory("log4j2")
public class Log4j2LoggerFactory implements IDelegateFactory {
    private final FormattedMessageFactory formattedMessageFactory =  new FormattedMessageFactory();
    private final LoggerContext context = (LoggerContext) LogManager.getContext(false);

    private File writeLog4j2ConfigToTemp(byte[] bytes) throws IOException {
        File loggerConfigTmp = File.createTempFile("ApimanLoggerConfig", "temp");
        loggerConfigTmp.deleteOnExit();
        return Files.write(loggerConfigTmp.toPath(), bytes).toFile();
    }

    @Override
    public IApimanLogger createLogger(String name) {
//        System.err.println(Log4j2LoggerFactory.class.getName() + "=" + id);
        return new Log4j2LoggerImpl(context.getLogger(name, formattedMessageFactory));
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
//        System.err.println(Log4j2LoggerFactory.class.getName() + "=" + id);
        return new Log4j2LoggerImpl(context.getLogger(klazz, formattedMessageFactory));
    }

    @Override
    public IDelegateFactory overrideLoggerConfig(Map<String, LogLevel> newLoggerConfig) {

        return this;
    }

    @Override
    public IDelegateFactory overrideLoggerConfig(File newLoggerConfig) {
        context.setConfigLocation(newLoggerConfig.toURI());
        return this;
    }

    //    @Override
//    public IDelegateFactory overrideLoggerConfig(LoggingChangeRequest newConfig) {
//        assert newConfig != null;
//        IApimanLogger log = createLogger(Log4j2LoggerFactory.class);
//        log.debug("Trying to load a new logging config: {0}", newConfig);
//        // If present, write the config from byte[] to tmp, then apply it.
//        // Each node on the local machine will separately perform this process
//        // (this allows us to get around CL isolation).
//        if (newConfig.getLoggerConfig() != null) {
//            try {
//                File configInTmp = writeLog4j2ConfigToTemp(newConfig.getLoggerConfig());
//                context.setConfigLocation(configInTmp.toURI());
//            } catch (IOException ioe) {
//                log.error(ioe, "Attempt to load a new logger configuration failed. "
//                    + "Logger configuration will be unchanged. {0}", ioe.getMessage());
//            }
//        }
//        return this;
//    }
}
