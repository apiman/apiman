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

/**
 * Logs to a string builder.
 * @author eric.wittmann@gmail.com
 */
@SuppressWarnings("nls")
public class StringBuilderLogger implements IApimanLogger {
    
    private StringBuilder builder = new StringBuilder();

    /**
     * Constructor.
     */
    public StringBuilderLogger() {
    }

    /**
     * @see IApimanLogger#info(java.lang.String)
     */
    @Override
    public void info(String message) {
        append("INFO: " + message);
    }

    /**
     * @see IApimanLogger#warn(java.lang.String)
     */
    @Override
    public void warn(String message) {
        append("WARN: " + message);
    }

    /**
     * @see IApimanLogger#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
        append("DEBUG: " + message);
    }

    /**
     * @see IApimanLogger#trace(java.lang.String)
     */
    @Override
    public void trace(String message) {
        append("TRACE: " + message);
    }

    /**
     * @see IApimanLogger#error(java.lang.Throwable)
     */
    @Override
    public void error(Throwable error) {
        append("ERROR: " + error.getMessage());
    }

    /**
     * @see IApimanLogger#error(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error(String message, Throwable error) {
        append("ERROR: " + message);
    }
    
    /**
     * Append to the builder.
     */
    private void append(String message) {
        builder.append(message);
        builder.append("\n");
    }
    
    /**
     * @return the log data
     */
    public String string() {
        return builder.toString();
    }
    
    /**
     * Reset the builder to empty.
     */
    public void reset() {
        builder = new StringBuilder();
    }

}
