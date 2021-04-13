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
package io.apiman.common.logging;

/**
 * Factory for creating Apiman loggers.
 *
 * <p>Within the Apiman codebase, you should generally use this factory rather than the native logger where
 * possible to ensure that logging is piped through to the appropriate logging framework.
 *
 * <p>To choose a logger, set the system property <tt>apiman.logger-delegate</tt> to the appropriate value
 * (e.g. <tt>log4j2</tt>). This should be set <strong>as early as possible</strong> to avoid static
 * initialisation problems which could result in the wrong logger being instantiated (commonly, the default).
 * Typically, setting this when you launch the JVM via <tt>-P</tt> is the most sensible approach, but
 * different platforms have alternative approaches which are viable (for example <tt>standalone.xml</tt>
 * properties section, <tt>JAVA_OPTS</tt>, <tt>CATALINA_OPTS</tt>, etc, which could be more attractive
 * depending on your specific deployment situation.
 *
 * <p>Implementations are discovered and registered via the {@link ApimanLoggerFactoryRegistry}, which will
 * use the name provided in the {@link io.apiman.common.logging.annotations.ApimanLoggerFactory} annotation.
 *
 * <p>Those wishing to implement their own custom logging implementations should refer to
 * {@link ApimanLoggerFactoryRegistry} for more information.
 *
 * @see ApimanLoggerFactoryRegistry
 * @see ApimanLoggerFactory
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class ApimanLoggerFactory {

    public static final String APIMAN_LOGGER = "apiman.logger-delegate";

    private static volatile boolean LOGGER_RESOLVED = false;
    private static IDelegateFactory LOGGER_FACTORY;

    /**
     * Create a logger that delegates onto the configured logging framework
     *
     * @param name name of the logger
     */
    public static IApimanLogger getLogger(String name) {
        return getLoggerFactory().createLogger(name);
    }

    /**
     * Create a logger that delegates onto the configured logging framework
     *
     * @param klazz the class to use as the logger name
     */
    public static IApimanLogger getLogger(Class<?> klazz) {
        return getLoggerFactory().createLogger(klazz);
    }

    /**
     * Using safe DCL, resolve or get the logger factory.
     */
    private static IDelegateFactory getLoggerFactory() {
        if (!LOGGER_RESOLVED) {
            synchronized (ApimanLoggerFactory.class) {
                if (!LOGGER_RESOLVED) {
                    LOGGER_FACTORY = resolveLoggerFactory();
                    LOGGER_RESOLVED = true;
                }
            }
        }
        return LOGGER_FACTORY;
    }

    /**
     * If system property is set, resolve the factory of that name, otherwise use the default logger.
     * <p>
     * In future we could use a more intelligent approach that attempts to detect which implementation should
     * be used, and falls back onto an ordered list of known implementations.
     */
    private static IDelegateFactory resolveLoggerFactory() {
        String sysProp = System.getProperty(APIMAN_LOGGER);
        if (sysProp == null) {
            return ApimanLoggerFactoryRegistry.getDefaultLoggerFactory();
        }
        return ApimanLoggerFactoryRegistry.getLoggerFactory(sysProp);
    }

    /**
     * Set an alternative delegate at runtime. This is very useful for testing, but could have odd behaviour
     * if used at runtime; caveat utilitor.
     */
    public synchronized static void setDelegate(IDelegateFactory delegate) {
        LOGGER_FACTORY = delegate;
        LOGGER_RESOLVED = true;
    }
}
