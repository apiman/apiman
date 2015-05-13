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
package io.apiman.manager.api.war;

import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.UuidApiKeyGenerator;
import io.apiman.manager.api.core.i18n.Messages;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanDelegateLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.core.logging.JsonLoggerImpl;
import io.apiman.manager.api.core.logging.StandardLoggerImpl;
import io.apiman.manager.api.es.EsStorage;
import io.apiman.manager.api.jpa.JpaStorage;
import io.apiman.manager.api.jpa.roles.JpaIdmStorage;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.impl.DefaultSecurityContext;
import io.apiman.manager.api.security.impl.KeycloakSecurityContext;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.commons.lang.StringUtils;

/**
 * Attempt to create producer methods for CDI beans.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class WarCdiFactory {

    private static JestClient sESClient;
    private static EsStorage sESStorage;

    @Produces @ApimanLogger
    public static IApimanLogger provideLogger(WarApiManagerConfig config, InjectionPoint injectionPoint) {
        try {
            ApimanLogger logger = injectionPoint.getAnnotated().getAnnotation(ApimanLogger.class);
            Class<?> klazz = logger.value();
            return getDelegate(config).newInstance().createLogger(klazz);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format(
                    Messages.i18n.format("LoggerFactory.InstantiationFailed")), e); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped
    public static ISecurityContext provideSecurityContext(WarApiManagerConfig config,
            @New DefaultSecurityContext defaultSC, @New KeycloakSecurityContext keycloakSC) {
        if ("default".equals(config.getSecurityContextType())) { //$NON-NLS-1$
            return defaultSC;
        } else if ("keycloak".equals(config.getSecurityContextType())) { //$NON-NLS-1$
            return keycloakSC;
        } else {
            throw new RuntimeException("Unknown security context type: " + config.getSecurityContextType()); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped
    public static IStorage provideStorage(WarApiManagerConfig config, @New JpaStorage jpaStorage, @New EsStorage esStorage) {
        if ("jpa".equals(config.getStorageType())) { //$NON-NLS-1$
            return jpaStorage;
        } else if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            return initES(config, esStorage);
        } else {
            throw new RuntimeException("Unknown storage type: " + config.getStorageType()); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped
    public static IStorageQuery provideStorageQuery(WarApiManagerConfig config, @New JpaStorage jpaStorage, @New EsStorage esStorage) {
        if ("jpa".equals(config.getStorageType())) { //$NON-NLS-1$
            return jpaStorage;
        } else if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            return initES(config, esStorage);
        } else {
            throw new RuntimeException("Unknown storage type: " + config.getStorageType()); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped
    public static IApiKeyGenerator provideApiKeyGenerator(@New UuidApiKeyGenerator uuidApiKeyGen) {
        return uuidApiKeyGen;
    }

    @Produces @ApplicationScoped
    public static IIdmStorage provideIdmStorage(WarApiManagerConfig config, @New JpaIdmStorage jpaIdmStorage, @New EsStorage esStorage) {
        if ("jpa".equals(config.getStorageType())) { //$NON-NLS-1$
            return jpaIdmStorage;
        } else if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            return initES(config, esStorage);
        } else {
            throw new RuntimeException("Unknown storage type: " + config.getStorageType()); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped
    public static JestClient provideTransportClient(WarApiManagerConfig config) {
        if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            if (sESClient == null) {
                sESClient = createJestClient(config);
            }
        }
        return sESClient;
    }

    /**
     * @param config
     * @return create a new test ES transport client
     */
    private static JestClient createJestClient(WarApiManagerConfig config) {
        StringBuilder builder = new StringBuilder();
        builder.append("http://"); //$NON-NLS-1$
        builder.append(config.getESHost());
        builder.append(":"); //$NON-NLS-1$
        builder.append(config.getESPort());
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
    private static EsStorage initES(WarApiManagerConfig config, EsStorage esStorage) {
        if (sESStorage == null) {
            sESStorage = esStorage;
            if (config.isInitializeES()) {
                sESStorage.initialize();
            }
        }
        return sESStorage;
    }

    private static Class<? extends IApimanDelegateLogger> getDelegate(WarApiManagerConfig config) {
        if(config.getLoggerName() == null || StringUtils.isEmpty(config.getLoggerName())) {
            System.err.println(Messages.i18n.format("LoggerFactory.NoLoggerSpecified")); //$NON-NLS-1$
            return StandardLoggerImpl.class;
        }

        switch(config.getLoggerName().toLowerCase()) {
            case "json": //$NON-NLS-1$
                return JsonLoggerImpl.class;
            case "standard": //$NON-NLS-1$
                return StandardLoggerImpl.class;
            default:
                return loadByFQDN(config.getLoggerName());
        }

    }

    @SuppressWarnings("unchecked")
    private static Class<? extends IApimanDelegateLogger> loadByFQDN(String fqdn) {
        try {
            return (Class<? extends IApimanDelegateLogger>) Class.forName(fqdn);
        } catch (ClassNotFoundException e) {
            System.err.println(String.format(Messages.i18n.format("LoggerFactory.LoggerNotFoundOnClasspath"), //$NON-NLS-1$
                    fqdn));
            return StandardLoggerImpl.class;
        }
    }
}
