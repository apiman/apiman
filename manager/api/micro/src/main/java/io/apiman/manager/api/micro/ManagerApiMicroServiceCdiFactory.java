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

import io.apiman.common.plugin.Plugin;
import io.apiman.common.plugin.PluginClassLoader;
import io.apiman.common.plugin.PluginCoordinates;
import io.apiman.common.util.ReflectionUtils;
import io.apiman.common.util.crypt.CurrentDataEncrypter;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.core.IApiCatalog;
import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IMetricsAccessor;
import io.apiman.manager.api.core.INewUserBootstrapper;
import io.apiman.manager.api.core.IPluginRegistry;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.UuidApiKeyGenerator;
import io.apiman.manager.api.core.crypt.DefaultDataEncrypter;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.i18n.Messages;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.common.logging.IApimanDelegateLogger;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.core.logging.JsonLoggerImpl;
import io.apiman.manager.api.core.logging.StandardLoggerImpl;
import io.apiman.manager.api.core.noop.NoOpMetricsAccessor;
import io.apiman.manager.api.es.ESMetricsAccessor;
import io.apiman.manager.api.es.EsStorage;
import io.apiman.manager.api.jpa.JpaStorage;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.impl.DefaultSecurityContext;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.config.HttpClientConfig.Builder;

import java.lang.reflect.Constructor;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;

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
    public static INewUserBootstrapper provideNewUserBootstrapper(ManagerApiMicroServiceConfig config, IPluginRegistry pluginRegistry) {
        String type = config.getNewUserBootstrapperType();
        if (type == null) {
            return new INewUserBootstrapper() {
                @Override
                public void bootstrapUser(UserBean user, IStorage storage) throws StorageException {
                    // Do nothing special.
                }
            };
        } else {
            try {
                return createCustomComponent(INewUserBootstrapper.class, config.getNewUserBootstrapperType(),
                        config.getNewUserBootstrapperProperties(), pluginRegistry);
            } catch (Throwable t) {
                throw new RuntimeException("Error or unknown user bootstrapper type: " + config.getNewUserBootstrapperType(), t); //$NON-NLS-1$
            }
        }
    }

    @Produces
    @ApplicationScoped
    public static IStorage provideStorage(ManagerApiMicroServiceConfig config, @New JpaStorage jpaStorage,
            @New EsStorage esStorage, IPluginRegistry pluginRegistry) {
        IStorage storage;
        if ("jpa".equals(config.getStorageType())) { //$NON-NLS-1$
            storage = jpaStorage;
        } else if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            storage = initES(config, esStorage);
        } else {
            try {
                storage = createCustomComponent(IStorage.class, config.getStorageType(),
                        config.getStorageProperties(), pluginRegistry);
            } catch (Throwable t) {
                throw new RuntimeException("Error or unknown storage type: " + config.getStorageType(), t); //$NON-NLS-1$
            }
        }
        return storage;
    }


    @Produces @ApplicationScoped
    public static ISecurityContext provideSecurityContext(@New DefaultSecurityContext defaultSC) {
        return defaultSC;
    }

    @Produces @ApplicationScoped
    public static IStorageQuery provideStorageQuery(ManagerApiMicroServiceConfig config, @New JpaStorage jpaStorage,
            @New EsStorage esStorage, IPluginRegistry pluginRegistry) {
        if ("jpa".equals(config.getStorageQueryType())) { //$NON-NLS-1$
            return jpaStorage;
        } else if ("es".equals(config.getStorageQueryType())) { //$NON-NLS-1$
            return initES(config, esStorage);
        } else {
            try {
                return createCustomComponent(IStorageQuery.class, config.getStorageQueryType(),
                        config.getStorageQueryProperties(), pluginRegistry);
            } catch (Throwable t) {
                throw new RuntimeException("Error or unknown storage query type: " + config.getStorageType(), t); //$NON-NLS-1$
            }
        }
    }

    @Produces @ApplicationScoped
    public static IMetricsAccessor provideMetricsAccessor(ManagerApiMicroServiceConfig config,
            @New NoOpMetricsAccessor noopMetrics, @New ESMetricsAccessor esMetrics, IPluginRegistry pluginRegistry) {
        IMetricsAccessor metrics;
        if ("es".equals(config.getMetricsType())) { //$NON-NLS-1$
            metrics = esMetrics;
        } else {
            try {
                metrics = createCustomComponent(IMetricsAccessor.class, config.getMetricsType(),
                        config.getMetricsProperties(), pluginRegistry);
            } catch (Throwable t) {
                System.err.println("Unknown apiman metrics accessor type: " + config.getMetricsType()); //$NON-NLS-1$
                metrics = noopMetrics;
            }
        }
        return metrics;
    }

    @Produces @ApplicationScoped
    public static IApiKeyGenerator provideApiKeyGenerator(ManagerApiMicroServiceConfig config,
            IPluginRegistry pluginRegistry, @New UuidApiKeyGenerator uuidApiKeyGen) {
        IApiKeyGenerator apiKeyGenerator;
        String type = config.getApiKeyGeneratorType();
        if ("uuid".equals(type)) { //$NON-NLS-1$
            apiKeyGenerator = uuidApiKeyGen;
        } else {
            try {
                apiKeyGenerator = createCustomComponent(IApiKeyGenerator.class, type,
                        config.getApiKeyGeneratorProperties(), pluginRegistry);
            } catch (Exception e) {
                System.err.println("Unknown apiman API key generator type: " + type); //$NON-NLS-1$
                System.err.println("Automatically falling back to UUID style API Keys."); //$NON-NLS-1$
                apiKeyGenerator = uuidApiKeyGen;
            }
        }
        return apiKeyGenerator;
    }

    @Produces @ApplicationScoped
    public static IDataEncrypter provideDataEncrypter(ManagerApiMicroServiceConfig config,
            IPluginRegistry pluginRegistry, @New DefaultDataEncrypter defaultEncrypter) {
        try {
            IDataEncrypter encrypter = createCustomComponent(IDataEncrypter.class, config.getDataEncrypterType(),
                    config.getDataEncrypterProperties(), pluginRegistry, defaultEncrypter);
            CurrentDataEncrypter.instance = encrypter;
            return encrypter;
        } catch (Throwable t) {
            throw new RuntimeException("Error or unknown data encrypter type: " + config.getDataEncrypterType(), t); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped
    public static IApiCatalog provideApiCatalog(ManagerApiMicroServiceConfig config, IPluginRegistry pluginRegistry) {
        try {
            return createCustomComponent(IApiCatalog.class, config.getApiCatalogType(),
                    config.getApiCatalogProperties(), pluginRegistry);
        } catch (Throwable t) {
            throw new RuntimeException("Error or unknown API catalog type: " + config.getApiCatalogType(), t); //$NON-NLS-1$
        }
    }

    @Produces @ApplicationScoped @Named("storage")
    public static JestClient provideStorageESClient(ManagerApiMicroServiceConfig config) {
        if ("es".equals(config.getStorageType()) && sStorageESClient == null) { //$NON-NLS-1$
            sStorageESClient = createStorageJestClient(config);
        }
        return sStorageESClient;
    }

    @Produces @ApplicationScoped @Named("metrics")
    public static JestClient provideMetricsESClient(ManagerApiMicroServiceConfig config) {
        if ("es".equals(config.getMetricsType()) && sMetricsESClient == null) { //$NON-NLS-1$
            sMetricsESClient = createMetricsJestClient(config);
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
        Builder httpConfig = new HttpClientConfig.Builder(connectionUrl).multiThreaded(true);
        String username = config.getStorageESUsername();
        String password = config.getStorageESPassword();
        if (username != null) {
            httpConfig.defaultCredentials(username, password);
        }
        httpConfig.connTimeout(config.getStorageESTimeout());
        httpConfig.readTimeout(config.getStorageESTimeout());
        factory.setHttpClientConfig(httpConfig.build());
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
        Builder httpConfig = new HttpClientConfig.Builder(connectionUrl).multiThreaded(true);
        String username = config.getMetricsESUsername();
        String password = config.getMetricsESPassword();
        if (username != null) {
            httpConfig.defaultCredentials(username, password);
        }
        httpConfig.connTimeout(config.getMetricsESTimeout());
        httpConfig.readTimeout(config.getMetricsESTimeout());
        factory.setHttpClientConfig(httpConfig.build());
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
            sESStorage.setIndexName(config.getStorageESIndexName());
            if (config.isInitializeStorageES()) {
                sESStorage.initialize();
            }
        }
        return sESStorage;
    }

    /**
     * Creates a custom component from information found in the properties file.
     * @param componentType
     * @param componentSpec
     * @param configProperties
     * @param pluginRegistry
     * @throws Exception
     */
    private static <T> T createCustomComponent(Class<T> componentType, String componentSpec,
            Map<String, String> configProperties, IPluginRegistry pluginRegistry) throws Exception {
        return createCustomComponent(componentType, componentSpec, configProperties, pluginRegistry, null);
    }

    /**
     * Creates a custom component from information found in the properties file.
     * @param componentType
     * @param componentSpec
     * @param configProperties
     * @param pluginRegistry
     * @param defaultComponent
     * @throws Exception
     */
    private static <T> T createCustomComponent(Class<T> componentType, String componentSpec,
            Map<String, String> configProperties, IPluginRegistry pluginRegistry, T defaultComponent) throws Exception {
        if (componentSpec == null && defaultComponent == null) {
            throw new IllegalArgumentException("Null component type."); //$NON-NLS-1$
        }
        if (componentSpec == null && defaultComponent != null) {
            return defaultComponent;
        }

        if (componentSpec.startsWith("class:")) { //$NON-NLS-1$
            Class<?> c = ReflectionUtils.loadClass(componentSpec.substring("class:".length())); //$NON-NLS-1$
            return createCustomComponent(componentType, c, configProperties);
        } else if (componentSpec.startsWith("plugin:")) { //$NON-NLS-1$
            PluginCoordinates coordinates = PluginCoordinates.fromPolicySpec(componentSpec);
            if (coordinates == null) {
                throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
            }
            int ssidx = componentSpec.indexOf('/');
            if (ssidx == -1) {
                throw new IllegalArgumentException("Invalid plugin component spec: " + componentSpec); //$NON-NLS-1$
            }
            String classname = componentSpec.substring(ssidx + 1);
            Plugin plugin = pluginRegistry.loadPlugin(coordinates);
            PluginClassLoader classLoader = plugin.getLoader();
            Class<?> class1 = classLoader.loadClass(classname);
            return createCustomComponent(componentType, class1, configProperties);
        } else {
            Class<?> c = ReflectionUtils.loadClass(componentSpec);
            return createCustomComponent(componentType, c, configProperties);
        }
    }

    /**
     * Creates a custom component from a loaded class.
     * @param componentType
     * @param componentClass
     * @param configProperties
     */
    @SuppressWarnings("unchecked")
    private static <T> T createCustomComponent(Class<T> componentType, Class<?> componentClass,
            Map<String, String> configProperties) throws Exception {
        if (componentClass == null) {
            throw new IllegalArgumentException("Invalid component spec (class not found)."); //$NON-NLS-1$
        }
        try {
            Constructor<?> constructor = componentClass.getConstructor(Map.class);
            return (T) constructor.newInstance(configProperties);
        } catch (Exception e) {
        }
        return (T) componentClass.getConstructor().newInstance();
    }

    private static Class<? extends IApimanDelegateLogger> getDelegate(ManagerApiMicroServiceConfig config) {
        String loggerName = config.getLoggerName();
        if(loggerName == null || StringUtils.isEmpty(loggerName)) {
            loggerName = "json"; //$NON-NLS-1$
        }

        switch(loggerName.toLowerCase()) {
            case "json": //$NON-NLS-1$
                return JsonLoggerImpl.class;
            case "standard": //$NON-NLS-1$
                return StandardLoggerImpl.class;
            default:
                return loadByFQDN(loggerName);
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
