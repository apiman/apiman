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
package io.apiman.common.es.util;

import io.apiman.common.util.ssl.KeyStoreUtil;
import io.apiman.common.util.ssl.KeyStoreUtil.Info;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.config.HttpClientConfig.Builder;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultEsClientFactory extends AbstractClientFactory implements IEsClientFactory {

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
        Integer port = NumberUtils.toInt(config.get("client.port"), 9200); //$NON-NLS-1$
        String protocol = config.get("client.protocol"); //$NON-NLS-1$
        String initialize = config.get("client.initialize"); //$NON-NLS-1$
        String username = config.get("client.username"); //$NON-NLS-1$
        String password = config.get("client.password"); //$NON-NLS-1$
        Integer timeout = NumberUtils.toInt(config.get("client.timeout"), 10000); //$NON-NLS-1$
        Integer maxConnectionIdleTime = NumberUtils.toInt(config.get("client.maxConnectionIdleTime"), 1000); //$NON-NLS-1$

        if (StringUtils.isBlank(protocol)) {
            protocol = "http"; //$NON-NLS-1$
        }

        if (StringUtils.isBlank(initialize)) {
            initialize = "true"; //$NON-NLS-1$
        }

        if (StringUtils.isBlank(host)) {
            throw new RuntimeException("Missing client.host configuration for ESRegistry."); //$NON-NLS-1$
        }

        synchronized (clients) {
            String clientKey = StringUtils.isNotBlank(indexName) ? "jest:" + host + ':' + port + '/' + indexName : null;
            if (clientKey != null && clients.containsKey(clientKey)) {
                return clients.get(clientKey);
            } else {
                String connectionUrl = protocol + "://" + host + ':' + port;
                Builder httpClientConfig = new HttpClientConfig.Builder(connectionUrl)
                        .connTimeout(timeout)
                        .readTimeout(timeout)
                        .maxConnectionIdleTime(maxConnectionIdleTime,TimeUnit.MILLISECONDS)
                        .maxTotalConnection(75)
                        .defaultMaxTotalConnectionPerRoute(75)
                        .multiThreaded(true);

                if (StringUtils.isNotBlank(username)) {
                    httpClientConfig.defaultCredentials(username, password).setPreemptiveAuth(new HttpHost(connectionUrl, port, protocol));
                }

                if ("https".equals(protocol)) { //$NON-NLS-1$ //$NON-NLS-2$
                    updateSslConfig(httpClientConfig, config);
                }

                JestClientFactory factory = new JestClientFactory();
                factory.setHttpClientConfig(httpClientConfig.build());

                JestClient client = factory.getObject();

                if(clientKey != null) {
                    clients.put(clientKey, client);
                    if("true".equals(initialize)) { //$NON-NLS-1$
                        initializeClient(client, indexName, defaultIndexName);
                    }
                }

                return client;
            }
        }
    }

    /**
     * Configures the SSL connection to use certificates by setting the keystores
     * @param httpConfig the http client configuration
     * @param config the configuration
     */
    @SuppressWarnings("nls")
    private void updateSslConfig(Builder httpConfig, Map<String, String> config) {
        try {
            String clientKeystorePath = config.get("client.keystore");
            String clientKeystorePassword = config.get("client.keystore.password");
            String trustStorePath = config.get("client.truststore");
            String trustStorePassword = config.get("client.truststore.password");

            SSLContext sslContext = SSLContext.getInstance("TLS");
            Info kPathInfo = new Info(clientKeystorePath, clientKeystorePassword);
            Info tPathInfo = new Info(trustStorePath, trustStorePassword);
            sslContext.init(KeyStoreUtil.getKeyManagers(kPathInfo), KeyStoreUtil.getTrustManagers(tPathInfo), new SecureRandom());
            HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            SchemeIOSessionStrategy httpsIOSessionStrategy = new SSLIOSessionStrategy(sslContext, hostnameVerifier);

            httpConfig.defaultSchemeForDiscoveredNodes("https");
            httpConfig.sslSocketFactory(sslSocketFactory); // for sync calls
            httpConfig.httpsIOSessionStrategy(httpsIOSessionStrategy); // for async calls

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
