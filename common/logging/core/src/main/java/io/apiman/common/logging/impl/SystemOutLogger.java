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

import java.text.MessageFormat;

/**
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class SystemOutLogger implements IApimanLogger {

    /**
     * Constructor.
     */
    public SystemOutLogger() {
    }

    /**
     * @see IApimanLogger#info(java.lang.String)
     */
    @Override
    public void info(String message) {
        System.out.println("INFO: " + message);
    }

    @Override
    public void info(String message, Object... args) {
        System.out.println("INFO: " + MessageFormat.format(message, args));
    }

    /**
     * @see IApimanLogger#warn(java.lang.String)
     */
    @Override
    public void warn(String message) {
        System.out.println("WARN: " + message);
    }

    @Override
    public void warn(String message, Object... args) {
        System.out.println("WARN: " + MessageFormat.format(message, args));
    }

    /**
     * @see IApimanLogger#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
        System.out.println("DEBUG: " + message);
    }

    @Override
    public void debug(String message, Object... args) {
        System.out.println("DEBUG: " + MessageFormat.format(message, args));
    }

    /**
     * @see IApimanLogger#trace(java.lang.String)
     */
    @Override
    public void trace(String message) {
        System.out.println("TRACE: " + message);
    }

    @Override
    public void trace(String message, Object... args) {
        System.out.println("TRACE: " + MessageFormat.format(message, args));
    }

    /**
     * @see IApimanLogger#error(java.lang.Throwable)
     */
    @Override
    public void error(Throwable error) {
        System.err.println("** ERROR **");
        error.printStackTrace(System.err);
    }

    /**
     * @see IApimanLogger#error(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error(String message, Throwable error) {
        System.err.println("** ERROR: " + message + " **");
        error.printStackTrace(System.err);
    }

    @Override
    public void error(Throwable error, String message, Object... args) {
        System.err.println("** ERROR: " + MessageFormat.format(message, args));
        error.printStackTrace(System.err);
    }

}
