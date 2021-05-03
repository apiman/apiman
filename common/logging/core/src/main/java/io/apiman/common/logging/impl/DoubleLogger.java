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

package io.apiman.common.logging.impl;

import io.apiman.common.logging.IApimanLogger;

/**
 * Combine two loggers together!
 *
 * Can be useful for testing and if logging output should go to somewhere in addition to the logging
 * subsystem (e.g. response body).
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class DoubleLogger implements IApimanLogger {

    private final IApimanLogger loggerA;
    private final IApimanLogger loggerB;

    /**
     * Constructor.
     * <p>
     * Loggers are logged to in the order provided to this constructor (i.e. loggerA then loggerB).
     *
     * @param loggerA first logger
     * @param loggerB second logger
     */
    public DoubleLogger(IApimanLogger loggerA, IApimanLogger loggerB) {
        this.loggerA = loggerA;
        this.loggerB = loggerB;
    }

    @Override
    public void info(String message) {
        loggerA.info(message);
        loggerB.info(message);
    }

    @Override
    public void info(String message, Object... args) {
        loggerA.info(message, args);
        loggerB.info(message, args);
    }

    @Override
    public void warn(String message) {
        loggerA.warn(message);
        loggerB.warn(message);
    }

    @Override
    public void warn(String message, Object... args) {
        loggerA.warn(message, args);
        loggerB.warn(message, args);
    }

    @Override
    public void debug(String message) {
        loggerA.debug(message);
        loggerB.debug(message);
    }

    @Override
    public void debug(String message, Object... args) {
        loggerA.debug(message, args);
        loggerB.debug(message, args);
    }

    @Override
    public void trace(String message) {
        loggerA.trace(message);
    }

    @Override
    public void trace(String message, Object... args) {
        loggerA.trace(message, args);
        loggerB.trace(message, args);
    }

    @Override
    public void error(Throwable error) {
        loggerA.error(error);
        loggerB.error(error);
    }

    @Override
    public void error(String message, Throwable error) {
        loggerA.error(message, error);
        loggerB.error(message, error);
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
        loggerA.error(error, message, args);
        loggerB.error(error, message, args);
    }

}
