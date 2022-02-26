/*
 * Copyright 2021 Scheer PAS Schweiz AG
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
package io.apiman.common.logging.jboss;

import io.apiman.common.logging.IApimanLogger;

import org.jboss.logging.Logger;

/**
 * Jboss logger facade for ApimanLogger.
 * <p>
 * Jboss logger methods ending in <tt>v</tt> use the <tt>MessageFormat</tt>.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class JbossLoggerImpl implements IApimanLogger {

    private final Logger logger;

    /**
     * New Apiman logger using Jboss Logging underneath
     * @param logger the new logger instance to delegate to
     */
    JbossLoggerImpl(Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Object... args) {
        logger.infov(message, args);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Object... args) {
        logger.warnv(message, args);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void debug(String message, Object... args) {
        logger.debugv(message, args);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void trace(String message, Object... args) {
        logger.tracev(message, args);
    }

    @Override
    public void error(Throwable error) {
        logger.error(error.getMessage(), error);
    }

    @Override
    public void error(String message, Throwable error) {
        logger.error(message, error);
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
        logger.errorv(message, message, args);
    }
}
