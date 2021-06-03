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
package io.apiman.common.logging;

import java.io.File;
import java.util.Map;

/**
 * Factory to create impl.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface IDelegateFactory {
    /**
     * Create a logger by name.
     *
     * @param name the name
     * @return the logger
     */
    IApimanLogger createLogger(String name);

    /**
     * Create a logger by class.
     *
     * @param klazz the class
     * @return the logger
     */
    IApimanLogger createLogger(Class<?> klazz);

    /**
     * Override the currently running logger configuration.
     * <p>
     * If the provided configuration file format is not recognised for example, if a log4j2 file is provided
     * to logback, etc, you will potentially get different (strange) exceptions or unusual behaviour depending
     * on the specific implementation that is active.
     *
     * @param newLoggerConfig the new logger configuration.
     */
    default IDelegateFactory overrideLoggerConfig(File newLoggerConfig) {
        IApimanLogger logger = ApimanLoggerFactory.getLogger(IDelegateFactory.class);
        logger.warn("The logger implementation you have selected does not support "
            + "dynamic logger configuration changes. This operation will be ignored");
        return this;
    }

    /**
     * Override the currently running logger configuration
     *
     * @param newLoggerConfig the new logger configuration.
     */
    default IDelegateFactory overrideLoggerConfig(Map<String, LogLevel> newLoggerConfig) {
        IApimanLogger logger = ApimanLoggerFactory.getLogger(IDelegateFactory.class);
        logger.warn("The logger implementation you have selected does not support "
            + "dynamic logger configuration changes. This operation will be ignored");
        return this;
    }
}
