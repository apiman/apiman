package io.apiman.manager.osgi;

import io.apiman.manager.api.core.IApiKeyGenerator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.UuidApiKeyGenerator;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.core.logging.JsonLoggerImpl;
import io.apiman.manager.api.es.EsStorage;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.impl.DefaultSecurityContext;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig.Builder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;

/**
 * Create producer methods for CDI beans.
 */
@ApplicationScoped public class CdiFactory {

    private static JestClient sStorageESClient;
    private static EsStorage sESStorage;

    @Produces @ApplicationScoped public static IStorage provideStorage(@New EsStorage esStorage) {
        IStorage storage = null;
        storage = initES(esStorage);
        return storage;
    }

    @Produces @ApplicationScoped @Named("storage") public static JestClient provideStorageESClient() {
        if (sStorageESClient == null) {
            sStorageESClient = createStorageJestClient();
        }
        return sStorageESClient;
    }

    @Produces @ApimanLogger public static IApimanLogger provideLogger(InjectionPoint injectionPoint) {
        ApimanLogger logger = injectionPoint.getAnnotated().getAnnotation(ApimanLogger.class);
        Class<?> requestorKlazz = logger.value();
        return new JsonLoggerImpl().createLogger(requestorKlazz);
    }

    @Produces @ApplicationScoped
    public static IStorageQuery provideStorageQuery(@New EsStorage esStorage) {
        return initES(esStorage);
    }


    /**
     * Initializes the ES storage (if required).
     *
     * @param esStorage
     */
    private static EsStorage initES(EsStorage esStorage) {
        if (sESStorage == null) {
            sESStorage = esStorage;
            sESStorage.initialize();
        }
        return sESStorage;
    }

    @Produces @ApplicationScoped public static ISecurityContext provideSecurityContext(
            @New DefaultSecurityContext defaultSC) {
        return defaultSC;
    }

    @Produces @ApplicationScoped public static IApiKeyGenerator provideApiKeyGenerator(
            @New UuidApiKeyGenerator uuidApiKeyGen) {
        return uuidApiKeyGen;
    }


    /**
     * @return create a new test ES client
     */
    private static JestClient createStorageJestClient() {
        StringBuilder builder = new StringBuilder();
        builder.append("http");
        builder.append("://"); //$NON-NLS-1$
        builder.append("localhost");
        builder.append(":"); //$NON-NLS-1$
        builder.append("19200");
        String connectionUrl = builder.toString();
        JestClientFactory factory = new JestClientFactory();
        Builder httpConfig = new Builder(connectionUrl).multiThreaded(true);
        String username = null;
        String password = null;
        if (username != null) {
            httpConfig.defaultCredentials(username, password);
        }
        httpConfig.connTimeout(6000);
        httpConfig.readTimeout(6000);
        factory.setHttpClientConfig(httpConfig.build());
        return factory.getObject();
    }

}
