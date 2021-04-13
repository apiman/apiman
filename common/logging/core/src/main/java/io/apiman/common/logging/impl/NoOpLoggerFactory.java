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

package io.apiman.common.logging.impl;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.util.Map;

/**
 * The fastest logger around :)
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@ApimanLoggerFactory("noop")
public class NoOpLoggerFactory implements IApimanLogger, IDelegateFactory {
    public static NoOpLoggerFactory INSTANCE = new NoOpLoggerFactory();

    public NoOpLoggerFactory() {
    }

    public NoOpLoggerFactory(Map<String, String> opts) {
    }

    @Override
    public IApimanLogger createLogger(String name) {
        return INSTANCE;
    }

    @Override
    public IApimanLogger createLogger(Class<?> klazz) {
        return INSTANCE;
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void debug(String message) {
    }

    @Override
    public void trace(String message) {
    }

    @Override
    public void error(Throwable error) {
    }

    @Override
    public void error(String message, Throwable error) {
    }

    @Override
    public void info(String message, Object... args) {
    }

    @Override
    public void warn(String message, Object... args) {
    }

    @Override
    public void debug(String message, Object... args) {
    }

    @Override
    public void trace(String message, Object... args) {
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
    }

}
