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

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.config.HttpClientConfig.Builder;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleJestClientFactory extends AbstractClientFactory implements IESClientFactory {

    /**
     * Clears all the clients from the cache.  Useful for unit testing.
     */
    public static void clearClientCache() {
        clients.clear();
    }
    
    /**
     * Constructor.
     */
    public SimpleJestClientFactory() {
    }

    /**
     * Creates a client from information in the config map.
     * @param config the configuration
     * @param defaultIndexName the default index to use if not specified in the config
     * @return the ES client
     */
    public JestClient createClient(Map<String, String> config, String defaultIndexName) {
        JestClient client;
        String indexName = config.get("client.index"); //$NON-NLS-1$
        if (indexName == null) {
            indexName = defaultIndexName;
        }
        client = createJestClient(config, indexName, defaultIndexName);
        return client;
    }

    /**
     * Creates a transport client from a configuration map.
     * @param config the configuration
     * @param indexName the name of the index
     * @param defaultIndexName the default index name
     * @return the ES client
     */
    protected JestClient createJestClient(Map<String, String> config, String indexName, String defaultIndexName) {
        String host = config.get("client.host"); //$NON-NLS-1$
        String port = config.get("client.port"); //$NON-NLS-1$
        String protocol = config.get("client.protocol"); //$NON-NLS-1$
        String initialize = config.get("client.initialize"); //$NON-NLS-1$

        if (initialize == null) {
            initialize = "true"; //$NON-NLS-1$
        }

        if (StringUtils.isBlank(host)) {
            throw new RuntimeException("Missing client.host configuration for ESRegistry."); //$NON-NLS-1$
        }
        if (StringUtils.isBlank(port)) {
            throw new RuntimeException("Missing client.port configuration for ESRegistry."); //$NON-NLS-1$
        }
        if (StringUtils.isBlank(protocol)) {
            protocol = "http"; //$NON-NLS-1$
        }
        
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
                Builder httpClientConfig = new HttpClientConfig.Builder(connectionUrl);
                updateHttpConfig(httpClientConfig, config);
                factory.setHttpClientConfig(httpClientConfig.build());
                updateJestClientFactory(factory, config);
                
                JestClient client = factory.getObject();
                clients.put(clientKey, client);
                if ("true".equals(initialize)) { //$NON-NLS-1$
                    initializeClient(client, indexName, defaultIndexName);
                }
                return client;
            }
        }
    }

    /**
     * Update the http client config.
     * @param httpClientConfig
     * @param config 
     */
    protected void updateHttpConfig(Builder httpClientConfig, Map<String, String> config) {
        String username = config.get("client.username"); //$NON-NLS-1$
        String password = config.get("client.password"); //$NON-NLS-1$
        String timeout = config.get("client.timeout"); //$NON-NLS-1$
        if (StringUtils.isBlank(timeout)) {
            timeout = "10000"; //$NON-NLS-1$
        }

        httpClientConfig
            .connTimeout(new Integer(timeout))
            .readTimeout(new Integer(timeout))
            .maxTotalConnection(75)
            .defaultMaxTotalConnectionPerRoute(75)
            .multiThreaded(true);
        if (!StringUtils.isBlank(username)) {
            httpClientConfig.defaultCredentials(username, password);
        }
    }
    
    /**
     * Update the jest client factory with any settings.
     * @param factory
     * @param config 
     */
    protected void updateJestClientFactory(JestClientFactory factory, Map<String, String> config) {
    }

}
