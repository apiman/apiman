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

/**
 * Attempt to create producer methods for CDI beans.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class CdiFactory {
    
    private static TransportClient esClient;

    @Produces @ApplicationScoped
    public static IStorage provideStorage(@New JpaStorage jpaStorage, @New EsStorage esStorage) {
        return jpaStorage;
    }

    @Produces @ApplicationScoped
    public static IStorageQuery provideStorageQuery(@New JpaStorage jpaStorage, @New EsStorage esStorage) {
        return jpaStorage;
    }

    @Produces @ApplicationScoped
    public static IApiKeyGenerator provideApiKeyGenerator(@New UuidApiKeyGenerator uuidApiKeyGen) {
        return uuidApiKeyGen;
    }

    @Produces @ApplicationScoped
    public static IIdmStorage provideIdmStorage(@New JpaIdmStorage jpaIdmStorage, @New EsStorage esStorage) {
        return jpaIdmStorage;
    }

    @Produces @ApplicationScoped
    public static TransportClient provideTransportClient() {
        if (esClient == null) {
            esClient = createTransportClient();
        }
        return esClient;
    }

    /**
     * @return create a new test ES transport client
     */
    private static TransportClient createTransportClient() {
//        TransportClient client = new TransportClient(ImmutableSettings.settingsBuilder()
//                .put("cluster.name", ManagerApiTestServer.ES_CLUSTER_NAME).build());
//        client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
//        return client;
        return null;
    }

}
