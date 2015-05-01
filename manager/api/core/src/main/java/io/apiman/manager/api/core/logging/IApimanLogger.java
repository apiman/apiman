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

/**
 * Simple logging interfaces. Later we'll add the ability to pass context info.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IApimanLogger {

    /**
     * Log an info level message
     *
     * @param message the message
     */
    void info(String message);

    /**
     * Log an info level message and error
     *
     * @param message the message
     * @param throwable the throwable cause
     */
    void info(String message, Throwable throwable);

    /**
     * Log a warning
     *
     * @param message the message
     */
    void warn(String message);

    /**
     * Log a warning and error
     *
     * @param message the message
     * @param throwable the throwable cause
     */
    void warn(String message, Throwable throwable);

    /**
     * Log a debug level message
     *
     * @param message the message
     */
    void debug(String message);

    /**
     * Log a debug level message and error
     *
     * @param message the message
     * @param throwable the throwable cause
     */
    void debug(String message, Throwable throwable);

    /**
     * Log a trace level message
     *
     * @param message the message
     */
    void trace(String message);

    /**
     * Log a trace level message and error
     *
     * @param message the message
     * @param throwable the throwable cause
     */
    void trace(String message, Throwable throwable);
}