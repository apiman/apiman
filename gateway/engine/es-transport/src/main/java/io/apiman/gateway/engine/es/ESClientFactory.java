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
package io.apiman.gateway.engine.es;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESClientFactory {
    
    private static Map<String, Client> clients = new HashMap<>();
    
    /**
     * Clears all the clients from the cache.  Useful for unit testing.
     */
    public static void clearClientCache() {
        clients.clear();
    }

    /**
     * Creates a client from information in the config map.
     * @param config the configuration 
     * @return the ES client
     */
    public static Client createClient(Map<String, String> config) {
        Client client = null;
        String clientType = config.get("client.type"); //$NON-NLS-1$
        if ("local".equals(clientType)) { //$NON-NLS-1$
            client = ESClientFactory.createLocalClient(config);
        } else if ("transport".equals(clientType)) { //$NON-NLS-1$
            client = ESClientFactory.createTransportClient(config);
        } else {
            throw new RuntimeException("Invalid elasticsearch client type: " + clientType); //$NON-NLS-1$
        }
        return client;
    }

    /**
     * Creates a transport client from a configuration map.
     * @param config the configuration 
     * @return the ES client
     */
    public static Client createTransportClient(Map<String, String> config) {
        String clusterName = config.get("client.cluster-name"); //$NON-NLS-1$
        String host = config.get("client.host"); //$NON-NLS-1$
        String port = config.get("client.port"); //$NON-NLS-1$
        if (StringUtils.isBlank(clusterName)) {
            throw new RuntimeException("Missing client.cluster-name configuration for ESRegistry."); //$NON-NLS-1$
        }
        if (StringUtils.isBlank(host)) {
            throw new RuntimeException("Missing client.host configuration for ESRegistry."); //$NON-NLS-1$
        }
        if (StringUtils.isBlank(port)) {
            throw new RuntimeException("Missing client.port configuration for ESRegistry."); //$NON-NLS-1$
        }
        return createTransportClient(host, Integer.parseInt(port), clusterName);
    }
    
    /**
     * Creates a local client from a configuration map.
     * @param config the configuration 
     * @return the ES client
     */
    public static Client createLocalClient(Map<String, String> config) {
        String clientLocClassName = config.get("client.class"); //$NON-NLS-1$
        String clientLocFieldName = config.get("client.field"); //$NON-NLS-1$
        return createLocalClient(clientLocClassName, clientLocFieldName);
    }
    
    /**
     * Creates and caches a transport client from host, port, and cluster name info.
     * @param host the host
     * @param port the port
     * @param clusterName the cluster name
     * @return the ES client
     */
    public static Client createTransportClient(String host, int port, String clusterName) {
        String clientKey = "transport:" + host + ':' + port + '/' + clusterName; //$NON-NLS-1$
        synchronized (clients) {
            if (clients.containsKey(clientKey)) {
                return clients.get(clientKey);
            } else {
                Client client = new TransportClient(ImmutableSettings.settingsBuilder()
                        .put("cluster.name", clusterName).build()); //$NON-NLS-1$
                ((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(host, port));
                clients.put(clientKey, client);
                initializeClient(client);
                return client;
            }
        }
    }
    
    /**
     * Creates a cache by looking it up in a static field.  Typically used for
     * testing.
     * @param className the class name
     * @param fieldName the field name
     * @return the ES client
     */
    public static Client createLocalClient(String className, String fieldName) {
        String clientKey = "local:" + className + '/' + fieldName; //$NON-NLS-1$
        synchronized (clients) {
            if (clients.containsKey(clientKey)) {
                return clients.get(clientKey);
            } else {
                try {
                    Class<?> clientLocClass = Class.forName(className);
                    Field field = clientLocClass.getField(fieldName);
                    Client client = (Client) field.get(null);
                    clients.put(clientKey, client);
                    initializeClient(client);
                    return client;
                } catch (ClassNotFoundException | NoSuchFieldException | SecurityException
                        | IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException("Error using local elasticsearch client.", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Called to initialize the storage.
     */
    private static void initializeClient(Client client) {
        try {
            client.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet(5000);
            IndicesExistsRequest request = new IndicesExistsRequest(ESConstants.INDEX_NAME);
            IndicesExistsResponse response = client.admin().indices().exists(request).get();
            if (!response.isExists()) {
                createIndex(client, ESConstants.INDEX_NAME);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an index.
     * @param indexName
     * @throws Exception 
     */
    private static void createIndex(Client client, String indexName) throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        URL settings = ESClientFactory.class.getResource("index-settings.json"); //$NON-NLS-1$
        String source = IOUtils.toString(settings);
        request.source(source);
        CreateIndexResponse response = client.admin().indices().create(request).get();
        if (!response.isAcknowledged()) {
            throw new Exception("Failed to create index: " + indexName); //$NON-NLS-1$
        }
    }

}
