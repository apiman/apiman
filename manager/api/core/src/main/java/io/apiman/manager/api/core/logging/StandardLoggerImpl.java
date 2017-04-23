/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.core.logging;

import io.apiman.common.logging.IApimanDelegateLogger;
import io.apiman.common.logging.IApimanLogger;

import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simplest possible logger implementation, just does a straight pass-through.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class StandardLoggerImpl implements IApimanDelegateLogger {

    private Logger delegatedLogger;

    @Override
    public IApimanLogger createLogger(String name) {
        delegatedLogger = LogManager.getLogger(name);
        return this;
    }

    @Override
    public IApimanLogger createLogger(Class <?> klazz) {
        delegatedLogger = LogManager.getLogger(klazz);
        return this;
    }

    @Override
    public void info(String message) {
        delegatedLogger.info(message);
    }


    @Override
    public void info(String message, Object... args) {
        delegatedLogger.info(message, args);
    }

    @Override
    public void debug(String message) {
        delegatedLogger.debug(message);
    }

    @Override
    public void debug(String message, Object... args) {
        delegatedLogger.debug(message, args);
    }

    @Override
    public void trace(String message) {
        delegatedLogger.trace(message);
    }

    @Override
    public void trace(String message, Object... args) {
        delegatedLogger.trace(message, args);
    }

    @Override
    public void warn(String message) {
        delegatedLogger.warn(message);
    }

    @Override
    public void warn(String message, Object... args) {
        delegatedLogger.warn(message, args);
    }

    @Override
    public void error(Throwable error) {
        delegatedLogger.error(error.getMessage(), error);
    }

    @Override
    public void error(String message, Throwable error) {
        delegatedLogger.error(message, error);
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
        if (delegatedLogger.isErrorEnabled()) {
            String formatted = MessageFormat.format(message, args);
            delegatedLogger.error(formatted, error);
        }
    }
}
