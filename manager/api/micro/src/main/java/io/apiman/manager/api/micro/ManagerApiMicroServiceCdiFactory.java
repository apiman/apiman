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
package io.apiman.manager.api.micro;

import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.UuidApiKeyGenerator;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.core.logging.JsonLoggerImpl;
import io.apiman.manager.api.core.noop.NoOpMetricsAccessor;
import io.apiman.manager.api.es.ESMetricsAccessor;
import io.apiman.manager.api.es.EsStorage;
import io.apiman.manager.api.jpa.JpaStorage;
import io.apiman.manager.api.jpa.roles.JpaIdmStorage;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.impl.DefaultSecurityContext;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

/**
 * Attempt to create producer methods for CDI beans.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ManagerApiMicroServiceCdiFactory {

    private static JestClient sStorageESClient;
    private static JestClient sMetricsESClient;
    private static EsStorage sESStorage;

    @Produces @ApimanLogger
    public static IApimanLogger provideLogger(ManagerApiMicroServiceConfig config, InjectionPoint injectionPoint) {
        ApimanLogger logger = injectionPoint.getAnnotated().getAnnotation(ApimanLogger.class);
        Class<?> requestorKlazz = logger.value();
        return new JsonLoggerImpl().createLogger(requestorKlazz);
    }

    @Produces @ApplicationScoped
    public static IStorage provideStorage(ManagerApiMicroServiceConfig config, @New JpaStorage jpaStorage, @New EsStorage esStorage) {
        if ("jpa".equals(config.getStorageType())) { //$NON-NLS-1$
            return jpaStorage;
        } else if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            return initES(config, esStorage);
        } else {
            throw new RuntimeException("Unknown storage type: " + config.getStorageType()); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped
    public static IStorageQuery provideStorageQuery(ManagerApiMicroServiceConfig config, @New JpaStorage jpaStorage, @New EsStorage esStorage) {
        if ("jpa".equals(config.getStorageType())) { //$NON-NLS-1$
            return jpaStorage;
        } else if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            return initES(config, esStorage);
        } else {
            throw new RuntimeException("Unknown storage type: " + config.getStorageType()); //$NON-NLS-1$
        }
    }
    
    @Produces @ApplicationScoped
    public static ISecurityContext provideSecurityContext(@New DefaultSecurityContext defaultSC) {
        return defaultSC;
    }

    @Produces @ApplicationScoped
    public static IMetricsAccessor provideMetricsAccessor(ManagerApiMicroServiceConfig config,
            @New NoOpMetricsAccessor noopMetrics, @New ESMetricsAccessor esMetrics) {
        IMetricsAccessor metrics = null;
        if ("es".equals(config.getMetricsType())) { //$NON-NLS-1$
            metrics = esMetrics;
        } else {
            System.err.println("Unknown apiman metrics accessor type: " + config.getMetricsType()); //$NON-NLS-1$
            metrics = noopMetrics;
        }
        return metrics;
    }

    @Produces @ApplicationScoped
    public static IApiKeyGenerator provideApiKeyGenerator(@New UuidApiKeyGenerator uuidApiKeyGen) {
        return uuidApiKeyGen;
    }

    @Produces @ApplicationScoped
    public static IIdmStorage provideIdmStorage(ManagerApiMicroServiceConfig config, @New JpaIdmStorage jpaIdmStorage, @New EsStorage esStorage) {
        if ("jpa".equals(config.getStorageType())) { //$NON-NLS-1$
            return jpaIdmStorage;
        } else if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            return initES(config, esStorage);
        } else {
            throw new RuntimeException("Unknown storage type: " + config.getStorageType()); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped @Named("storage")
    public static JestClient provideStorageESClient(ManagerApiMicroServiceConfig config) {
        if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            if (sStorageESClient == null) {
                sStorageESClient = createStorageJestClient(config);
            }
        }
        return sStorageESClient;
    }

    @Produces @ApplicationScoped @Named("metrics")
    public static JestClient provideMetricsESClient(ManagerApiMicroServiceConfig config) {
        if ("es".equals(config.getMetricsType())) { //$NON-NLS-1$
            if (sMetricsESClient == null) {
                sMetricsESClient = createMetricsJestClient(config);
            }
        }
        return sMetricsESClient;
    }

    /**
     * @param config
     * @return create a new test ES client
     */
    private static JestClient createStorageJestClient(ManagerApiMicroServiceConfig config) {
        StringBuilder builder = new StringBuilder();
        builder.append(config.getStorageESProtocol());
        builder.append("://"); //$NON-NLS-1$
        builder.append(config.getStorageESHost());
        builder.append(":"); //$NON-NLS-1$
        builder.append(config.getStorageESPort());
        String connectionUrl = builder.toString();
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(connectionUrl).multiThreaded(true)
                .build());
        return factory.getObject();
    }

    /**
     * @param config
     * @return create a new test ES client
     */
    private static JestClient createMetricsJestClient(ManagerApiMicroServiceConfig config) {
        StringBuilder builder = new StringBuilder();
        builder.append(config.getMetricsESProtocol());
        builder.append("://"); //$NON-NLS-1$
        builder.append(config.getMetricsESHost());
        builder.append(":"); //$NON-NLS-1$
        builder.append(config.getMetricsESPort());
        String connectionUrl = builder.toString();
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(connectionUrl).multiThreaded(true)
                .build());
        return factory.getObject();
    }

    /**
     * Initializes the ES storage (if required).
     * @param config
     * @param esStorage
     */
    private static EsStorage initES(ManagerApiMicroServiceConfig config, EsStorage esStorage) {
        if (sESStorage == null) {
            sESStorage = esStorage;
            if (config.isInitializeStorageES()) {
                sESStorage.initialize();
            }
        }
        return sESStorage;
    }

}
