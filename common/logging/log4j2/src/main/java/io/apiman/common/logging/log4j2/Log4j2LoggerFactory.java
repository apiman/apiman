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
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.FormattedMessageFactory;

;

/**
 * Log4j2 logger factory.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@ApimanLoggerFactory("log4j2")
public class Log4j2LoggerFactory implements IDelegateFactory {
    private final FormattedMessageFactory formattedMessageFactory =  new FormattedMessageFactory();
    private final LoggerContext context = (LoggerContext) LogManager.getContext(false);

    @Override
    public IApimanLogger createLogger(String name) {
        return new Log4j2LoggerImpl(context.getLogger(name, formattedMessageFactory));
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return new Log4j2LoggerImpl(context.getLogger(klazz, formattedMessageFactory));
    }

    @Override
    public IDelegateFactory overrideLoggerConfig(File newLoggerConfig) {
        context.setConfigLocation(newLoggerConfig.toURI());
        return this;
    }

    @Override
    public IDelegateFactory overrideLoggerConfig(Map<String, LogLevel> newLoggerConfig) {
        Map<String, Level> log4j2LevelMap = new HashMap<>(newLoggerConfig.size());
        for (Entry<String, LogLevel> entry : newLoggerConfig.entrySet()) {
            String loggerName = entry.getKey();
            Level level = translateLoggerLevel(entry.getValue());
            log4j2LevelMap.put(loggerName, level);
        }
        org.apache.logging.log4j.core.config.Configurator.setLevel(log4j2LevelMap);
        return this;
    }

    private Level translateLoggerLevel(LogLevel value) {
        switch (value) {
            case ERROR:
                return Level.ERROR;
            case WARN:
                return Level.WARN;
            case INFO:
                return Level.INFO;
            case DEBUG:
                return Level.DEBUG;
            case TRACE:
                return Level.TRACE;
            default:
                throw new IllegalStateException("Unexpected logger level value: " + value);
        }
    }
}
