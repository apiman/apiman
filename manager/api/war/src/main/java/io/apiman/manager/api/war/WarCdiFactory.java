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
import io.apiman.manager.api.es.EsStorage;
import io.apiman.manager.api.jpa.JpaStorage;
import io.apiman.manager.api.jpa.roles.JpaIdmStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Attempt to create producer methods for CDI beans.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class WarCdiFactory {
    
    private static TransportClient sESClient;
    private static EsStorage sESStorage;

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
    public static TransportClient provideTransportClient(WarApiManagerConfig config) {
        if ("es".equals(config.getStorageType())) { //$NON-NLS-1$
            if (sESClient == null) {
                sESClient = createTransportClient(config);
            }
        }
        return sESClient;
    }

    /**
     * @param config 
     * @return create a new test ES transport client
     */
    private static TransportClient createTransportClient(WarApiManagerConfig config) {
        TransportClient client = new TransportClient(ImmutableSettings.settingsBuilder()
                .put("cluster.name", config.getESClusterName()).build()); //$NON-NLS-1$
        client.addTransportAddress(new InetSocketTransportAddress(config.getESHost(), config.getESPort()));
        return client;
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

}
