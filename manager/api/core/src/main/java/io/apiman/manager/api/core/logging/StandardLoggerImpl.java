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
    public void info(String message, Throwable throwable) {
        delegatedLogger.info(message, throwable);
    }

    @Override
    public void debug(String message) {
        delegatedLogger.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        delegatedLogger.debug(message, throwable);
    }

    @Override
    public void trace(String message) {
        delegatedLogger.trace(message);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        delegatedLogger.trace(message, throwable);
    }

    @Override
    public void setTimeImpl(Time timeImpl) {
        // In this impl, do nothing.
    }
}
