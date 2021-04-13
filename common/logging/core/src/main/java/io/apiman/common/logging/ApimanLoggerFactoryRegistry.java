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

import io.apiman.common.logging.annotations.ApimanLoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

/**
 * Registry of Apiman Logger Factories.
 * <p>
 * Logger factories will be resolved via the {@link ApimanLoggerFactory} annotation. Any annotated class
 * <strong>must</strong> implement the {@link IDelegateFactory} interface and be within the
 * <tt>io.apiman.common.logging</tt> namespace (any sub-package is fine).
 * <p>
 * Factories are indexed by their names, and in the case of duplication, 'last in' will win.
 * <p>
 * A default (functional but rather rubbish stdout) logger {@link SoutDelegateFactory} is available if none
 * has been selected by the user. In future this could be handled more intelligently to provide some
 * auto-detection of which platform Apiman is running on to select a reasonable default.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class ApimanLoggerFactoryRegistry {
    private static final Map<String, IDelegateFactory> DELEGATE_FACTORY_MAP = new HashMap<>();
    private static IDelegateFactory  DEFAULT_FACTORY = new SoutDelegateFactory();

    static {
        resolveAllImplementations();
    }

    private static void resolveAllImplementations() {
        Reflections reflection = new Reflections("io.apiman.common.logging");
        Set<Class<?>> loggerFactories = reflection.getTypesAnnotatedWith(ApimanLoggerFactory.class);

        for (Class<?> loggerFactory : loggerFactories) {
            try {
                IDelegateFactory instance = (IDelegateFactory) loggerFactory.newInstance();
                String name = loggerFactory.getAnnotation(ApimanLoggerFactory.class).name();
                DELEGATE_FACTORY_MAP.put(name, instance);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Register a logger factory implementation. Last in wins in case of collision.
     * @param name the name of the logging implementation.
     * @param loggerFactory the logger factory
     */
    public static void register(String name, IDelegateFactory loggerFactory) {
        DELEGATE_FACTORY_MAP.put(name, loggerFactory);
    }

    /**
     * Register a default logger factory implementation that overrides the (bad) default.
     * @param name the name of the logging implementation.
     * @param loggerFactory the logger factory
     */
    public static void registerAsDefault(String name, IDelegateFactory loggerFactory) {
        DELEGATE_FACTORY_MAP.put(name, loggerFactory);
        DEFAULT_FACTORY = loggerFactory;
    }

    /**
     * Get a logger factory by name
     * @param name name of logger factory to get
     * @return the logger factory, or else null.
     */
    static IDelegateFactory getLoggerFactory(String name) {
        return DELEGATE_FACTORY_MAP.get(name);
    }

    /**
     * Get the default logger factory
     * @return the default logger factory.
     */
    static IDelegateFactory getDefaultLoggerFactory() {
        return DEFAULT_FACTORY;
    }
}
