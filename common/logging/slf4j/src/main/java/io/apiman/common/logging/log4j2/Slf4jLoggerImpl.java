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
import org.slf4j.Logger;

/**
 * Simple SLF4J logger wrapper.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class Slf4jLoggerImpl implements IApimanLogger {
    private final Logger logger;

    public Slf4jLoggerImpl(Logger slf4jLogger) {
        this.logger = slf4jLogger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void error(Throwable error) {
        logger.error(error.getMessage(), error);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.error(message, error);
    }
}
