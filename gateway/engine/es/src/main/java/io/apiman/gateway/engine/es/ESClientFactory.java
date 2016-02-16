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

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.config.HttpClientConfig.Builder;
import io.searchbox.cluster.Health;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESClientFactory {

    private static Map<String, JestClient> clients = new HashMap<>();

    /**
     * Clears all the clients from the cache.  Useful for unit testing.
     */
    public static void clearClientCache() {
        clients.clear();
    }

    /**
     * Creates a client from information in the config map.
     * @param config the configuration
     * @param indexName the index to use
     * @return the ES client
     */
    public static JestClient createClient(Map<String, String> config, String indexName) {
        JestClient client;
        String clientType = config.get("client.type"); //$NON-NLS-1$
        if (clientType == null) {
            clientType = "jest"; //$NON-NLS-1$
        }
        if ("jest".equals(clientType)) { //$NON-NLS-1$
            client = createJestClient(config, indexName);
        } else if ("local".equals(clientType)) { //$NON-NLS-1$
            client = createLocalClient(config, indexName);
        } else {
            throw new RuntimeException("Invalid elasticsearch client type: " + clientType); //$NON-NLS-1$
        }
        return client;
    }

    /**
     * Creates a transport client from a configuration map.
     * @param config the configuration
     * @param indexName the name of the index
     * @return the ES client
     */
    public static JestClient createJestClient(Map<String, String> config, String indexName) {
        String host = config.get("client.host"); //$NON-NLS-1$
        String port = config.get("client.port"); //$NON-NLS-1$
        String protocol = config.get("client.protocol"); //$NON-NLS-1$
        String initialize = config.get("client.initialize"); //$NON-NLS-1$
        String timeout = config.get("client.timeout"); //$NON-NLS-1$

        if (initialize == null) {
            initialize = "true"; //$NON-NLS-1$
        }
        String username = config.get("client.username"); //$NON-NLS-1$
        String password = config.get("client.password"); //$NON-NLS-1$

        if (StringUtils.isBlank(host)) {
            throw new RuntimeException("Missing client.host configuration for ESRegistry."); //$NON-NLS-1$
        }
        if (StringUtils.isBlank(port)) {
            throw new RuntimeException("Missing client.port configuration for ESRegistry."); //$NON-NLS-1$
        }
        if (StringUtils.isBlank(protocol)) {
            protocol = "http"; //$NON-NLS-1$
        }
        if (StringUtils.isBlank(timeout)) {
            timeout = "6000"; //$NON-NLS-1$
        }
        return createJestClient(protocol, host, Integer.parseInt(port), indexName, username, password,
                BooleanUtils.toBoolean(initialize), Integer.parseInt(timeout));
    }

    /**
     * Creates and caches a Jest client from host and port.
     * @param protocol http or https
     * @param host the host
     * @param port the port
     * @param indexName the index name
     * @param username the username to authenticate with
     * @param password the password to authenticate with
     * @param initialize true if the index should be initialized
     * @param timeout the connection and read timeouts in ms
     * @return the ES client
     */
    public static JestClient createJestClient(String protocol, String host, int port, String indexName,
            String username, String password, boolean initialize, int timeout) {
        String clientKey = "jest:" + host + ':' + port + '/' + indexName; //$NON-NLS-1$
        synchronized (clients) {
            if (clients.containsKey(clientKey)) {
                return clients.get(clientKey);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append(protocol);
                builder.append("://"); //$NON-NLS-1$
                builder.append(host);
                builder.append(":"); //$NON-NLS-1$
                builder.append(String.valueOf(port));
                String connectionUrl = builder.toString();

                JestClientFactory factory = new JestClientFactory();
                Builder httpClientConfig = new HttpClientConfig.Builder(connectionUrl)
                        .connTimeout(timeout)
                        .readTimeout(timeout)
                        .maxTotalConnection(75)
                        .defaultMaxTotalConnectionPerRoute(75)
                        .multiThreaded(true);
                if (!StringUtils.isBlank(username)) {
                    httpClientConfig.defaultCredentials(username, password);
                }
                factory.setHttpClientConfig(httpClientConfig.build());
                JestClient client = factory.getObject();
                clients.put(clientKey, client);
                if (initialize) {
                    initializeClient(client, indexName);
                }
                return client;
            }
        }
    }

    /**
     * Creates a local client from a configuration map.
     * @param config the config from apiman.properties
     * @param indexName the name of the ES index
     * @return the ES client
     */
    public static JestClient createLocalClient(Map<String, String> config, String indexName) {
        String clientLocClassName = config.get("client.class"); //$NON-NLS-1$
        String clientLocFieldName = config.get("client.field"); //$NON-NLS-1$
        return createLocalClient(clientLocClassName, clientLocFieldName, indexName);
    }

    /**
     * Creates a cache by looking it up in a static field.  Typically used for
     * testing.
     * @param className the class name
     * @param fieldName the field name
     * @param indexName the name of the ES index
     * @return the ES client
     */
    public static JestClient createLocalClient(String className, String fieldName, String indexName) {
        String clientKey = "local:" + className + '/' + fieldName; //$NON-NLS-1$
        synchronized (clients) {
            if (clients.containsKey(clientKey)) {
                return clients.get(clientKey);
            } else {
                try {
                    Class<?> clientLocClass = Class.forName(className);
                    Field field = clientLocClass.getField(fieldName);
                    JestClient client = (JestClient) field.get(null);
                    clients.put(clientKey, client);
                    initializeClient(client, indexName);
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
    public static void initializeClient(JestClient client, String indexName) {
        try {
            client.execute(new Health.Builder().build());
            Action<JestResult> action = new IndicesExists.Builder(indexName).build();
            JestResult result = client.execute(action);
            if (!result.isSucceeded()) {
                createIndex(client, indexName, indexName + "-settings.json"); //$NON-NLS-1$
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
    public static void createIndex(JestClient client, String indexName, String settingsName) throws Exception {
        URL settings = ESClientFactory.class.getResource(settingsName);
        String source = IOUtils.toString(settings);
        JestResult response = client.execute(new CreateIndex.Builder(indexName).settings(source).build());
        if (!response.isSucceeded()) {
            // When running in e.g. Wildfly, the Gateway exists as two separate WARs - the API and the
            // runtime Gateway itself.  They both create a registry and thus they both try to initialize
            // the ES index if it doesn't exist.  A race condition could result in both WARs trying to
            // create the index.  So a result of "IndexAlreadyExistsException" should be ignored.
            if (!response.getErrorMessage().startsWith("IndexAlreadyExistsException")) { //$NON-NLS-1$
                throw new Exception("Failed to create index: '" + indexName + "' Reason: " + response.getErrorMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

}
