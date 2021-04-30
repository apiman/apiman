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

import java.util.List;
import java.util.function.Consumer;

/**
 * Combine any number of loggers together. Order of logging is defined by the list provided.
 *
 * If you only need 2 loggers, {@link DoubleLogger} will likely perform better.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class MultiLogger implements IApimanLogger {

    private final List<IApimanLogger> delegates;

    /**
     * Constructor
     * <p>
     * Combine any number of loggers together. Order of logger invocation is defined by the list provided.
     *
     * @param delegates the logger delegates for this multi-logger to invoke in order
     */
    public MultiLogger(List<IApimanLogger> delegates) {
        this.delegates = delegates;
    }

    @Override
    public void info(String message) {
        doLogging(delegateLogger -> delegateLogger.info(message));
    }

    @Override
    public void info(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.info(message, args));
    }

    @Override
    public void warn(String message) {
        doLogging(delegateLogger -> delegateLogger.warn(message));
    }

    @Override
    public void warn(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.warn(message, args));
    }

    @Override
    public void debug(String message) {
        doLogging(delegateLogger -> delegateLogger.debug(message));
    }

    @Override
    public void debug(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.debug(message, args));
    }

    @Override
    public void trace(String message) {
        doLogging(delegateLogger -> delegateLogger.trace(message));
    }

    @Override
    public void trace(String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.trace(message, args));
    }

    @Override
    public void error(Throwable error) {
        doLogging(delegateLogger -> delegateLogger.error(error));
    }

    @Override
    public void error(String message, Throwable error) {
        doLogging(delegateLogger -> delegateLogger.error(message, error));
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
        doLogging(delegateLogger -> delegateLogger.error(error, message, args));
    }

    private void doLogging(Consumer<IApimanLogger> consumerFunction) {
        for (IApimanLogger delegate : delegates) {
            consumerFunction.accept(delegate);
        }
    }
}
