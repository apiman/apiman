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
package io.apiman.common.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Provides simple access to services.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceRegistryUtil {

    private static Map<Class<?>, Set<?>> servicesCache = new HashMap<>();

    /**
     * Gets a single service by its interface.
     * @param serviceInterface
     * @throws IllegalStateException
     */
    public static <T> T getSingleService(Class<T> serviceInterface) throws IllegalStateException {
        // Cached single service values are derived from the values cached when checking
        // for multiple services
        T rval = null;
        Set<T> services = getServices(serviceInterface);
        
        if (services.size() > 1) {
            throw new IllegalStateException("Multiple implementations found of " + serviceInterface); //$NON-NLS-1$
        } else if (!services.isEmpty()) {
            rval = services.iterator().next();
        }
        return rval;
    }

    /**
     * Get a set of service implementations for a given interface.
     * @param serviceInterface
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> getServices(Class<T> serviceInterface) {
        synchronized(servicesCache) {
            if (servicesCache.containsKey(serviceInterface)) {
                return (Set<T>) servicesCache.get(serviceInterface);
            }
    
            Set<T> services = new LinkedHashSet<>();
            try {
                for (T service : ServiceLoader.load(serviceInterface)) {
                    services.add(service);
                }
            } catch (ServiceConfigurationError sce) {
                // No services found - don't check again.
            }
            servicesCache.put(serviceInterface, services);
            return services;
        }
    }

}
