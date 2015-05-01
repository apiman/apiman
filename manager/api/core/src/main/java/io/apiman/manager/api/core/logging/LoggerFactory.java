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

import io.apiman.manager.api.core.i18n.Messages;

import org.apache.commons.lang.StringUtils;

/**
 * Create an {@link IApimanLogger} instance. Determines which logger implementation to load based upon the
 * configuration provided in apiman.properties.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class LoggerFactory {

    public static final Class<? extends IApimanLogger> DEFAULT_LOGGER = StandardLoggerImpl.class;

    /**
     * Create a logger
     *
     * @param klazz the class instantiating logger
     * @return a logger instance
     */
    public static IApimanLogger createLogger(Class<?> klazz) {
        try {
            return getDelegate().newInstance().createLogger(klazz);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format(
                    Messages.i18n.format("LoggerFactory.InstantiationFailed"), klazz), e); //$NON-NLS-1$
        }
    }

    /**
     * Create a logger
     *
     * @param name a logger name
     * @return a logger instance
     */
    public static IApimanLogger createLogger(String name) {
        try {
            return getDelegate().newInstance().createLogger(name);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format(
                    Messages.i18n.format("LoggerFactory.InstantiationFailed"), name), e); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends IApimanDelegateLogger> getDelegate() {
        String fqdn = JsonLoggerImpl.class.getCanonicalName(); // TODO get this from apiman.properties

        if (StringUtils.isEmpty(fqdn)) {
            System.err.println(Messages.i18n.format("LoggerFactory.NoLoggerSpecified")); //$NON-NLS-1$
        } else {
            try {
                return (Class<? extends IApimanDelegateLogger>) Class.forName(fqdn);
            } catch (ClassNotFoundException e) {
                System.err.println(String.format(
                        Messages.i18n.format("LoggerFactory.LoggerNotFoundOnClasspath"), //$NON-NLS-1$
                        fqdn));
            }
        }

        System.err.println(String.format(Messages.i18n.format("LoggerFactory.FallingBack"), //$NON-NLS-1$
                DEFAULT_LOGGER.getCanonicalName()));

        return StandardLoggerImpl.class;
    }
}
