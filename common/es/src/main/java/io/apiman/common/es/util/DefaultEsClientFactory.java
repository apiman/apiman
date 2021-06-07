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

import io.apiman.common.es.util.builder.index.EsIndexProperties;
import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultEsClientFactory extends AbstractClientFactory implements IEsClientFactory {

    private static final int POLL_INTERVAL_SECS = 5;
    private final IApimanLogger logger = ApimanLoggerFactory.getLogger(DefaultEsClientFactory.class);

    /**
     * Creates a client from information in the config map.
     * @param config the configuration
     * @param esIndices the ES index definitions
     * @param defaultIndexPrefix the default index prefix to use if not specified in the config
     * @return the ES client
     */
    @Override
    public RestHighLevelClient createClient(Map<String, String> config,
        Map<String, EsIndexProperties> esIndices,
        String defaultIndexPrefix) {

        RestHighLevelClient client;
        String indexNamePrefix = config.getOrDefault("client.indexPrefix", defaultIndexPrefix); //$NON-NLS-1$
        client = this.createEsClient(config, esIndices, indexNamePrefix);
        return client;
    }

    /**
     * Creates a transport client from a configuration map.
     * @param config the configuration
     * @param esIndexes the ES index definitions
     * @param indexNamePrefix the prefix of the index
     * @return the ES client
     */
    private RestHighLevelClient createEsClient(Map<String, String> config,
        Map<String, EsIndexProperties> esIndexes,
        String indexNamePrefix) {

        String host = config.get("client.host"); //$NON-NLS-1$
        int port = NumberUtils.toInt(config.get("client.port"), 9200); //$NON-NLS-1$
        String protocol = config.get("client.protocol"); //$NON-NLS-1$
        String initialize = config.get("client.initialize"); //$NON-NLS-1$
        String username = config.get("client.username"); //$NON-NLS-1$
        String password = config.get("client.password"); //$NON-NLS-1$
        int timeout = NumberUtils.toInt(config.get("client.timeout"), 10000); //$NON-NLS-1$
        long pollingTime = NumberUtils.toLong(config.get("client.polling.time"), 600); //$NON-NLS-1$

        if (StringUtils.isBlank(protocol)) {
            protocol = "http"; //$NON-NLS-1$
        }

        if (StringUtils.isBlank(initialize)) {
            initialize = "true"; //$NON-NLS-1$
        }

        if (StringUtils.isBlank(host)) {
            throw new RuntimeException("Missing client.host configuration for EsRegistry."); //$NON-NLS-1$
        }

        logger.info(String.format("Demand elasticsearch-client for %s://%s:%d for index prefix %s", protocol, host, port, indexNamePrefix));

        synchronized (clients) {
            String clientKey = StringUtils.isNotBlank(indexNamePrefix) ? "es:" + host + ':' + port + '/' + indexNamePrefix : null;

            RestHighLevelClient client;

            if (clientKey != null && clients.containsKey(clientKey)) {
                client = clients.get(clientKey);
                logger.info("Use cached elasticsearch-client with client key " + clientKey);
            } else {
                RestClientBuilder clientBuilder = RestClient.builder(new HttpHost(host, port, protocol));
                clientBuilder.setRequestConfigCallback(builder -> builder.setConnectTimeout(timeout)
                        .setSocketTimeout(timeout));

                HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create();

                if (username != null && password != null) {
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                    asyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }

                if ("https".equals(protocol)) { //$NON-NLS-1$ //$NON-NLS-2$
                    updateSslConfig(asyncClientBuilder, config);
                }

                clientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> asyncClientBuilder);
                client = new RestHighLevelClient(clientBuilder);

                try {
                    this.waitForElasticsearch(client, pollingTime);
                    // put client to list if polling is successful
                    if(clientKey != null) {
                        clients.put(clientKey, client);
                        logger.info(String.format("Created new elasticsearch-client for %s://%s:%d for index prefix %s", protocol, host, port, indexNamePrefix));
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }

            if ("true".equals(initialize)) { //$NON-NLS-1$
                this.initializeIndices(client, esIndexes, indexNamePrefix);
            }

            return client;
        }
    }

    private void waitForElasticsearch(RestHighLevelClient client, long pollingTime) {
        EsConnectionPoller poller = new EsConnectionPoller(client, 0, POLL_INTERVAL_SECS,
            Math.toIntExact(pollingTime));
        poller.blockUntilReady();
    }

    /**
     * Configures the SSL connection to use certificates by setting the keystores
     * @param asyncClientBuilder the client builder
     * @param config the configuration
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_encrypted_communication.html">Elasticsearch-Docs</>
     */
    @SuppressWarnings("nls")
    private void updateSslConfig(HttpAsyncClientBuilder asyncClientBuilder, Map<String, String> config) {
        boolean allowSelfSigned = Boolean.parseBoolean(config.get("client.allowSelfSigned"));
        boolean allowAnyHost = Boolean.parseBoolean(config.get("client.allowAnyHost"));

        try {
            String clientKeystorePath = config.get("client.keystore");
            String clientKeystorePassword = config.get("client.keystore.password");
            String trustStorePath = config.get("client.truststore");
            String trustStorePassword = config.get("client.truststore.password");

            Path trustStorePathObject = Paths.get(trustStorePath);
            Path keyStorePathObject = Paths.get(clientKeystorePath);
            KeyStore truststore = KeyStore.getInstance("pkcs12");
            KeyStore keyStore = KeyStore.getInstance("pkcs12");

            try (InputStream is = Files.newInputStream(trustStorePathObject)) {
                truststore.load(is, trustStorePassword.toCharArray());
            }
            try (InputStream is = Files.newInputStream(keyStorePathObject)) {
                keyStore.load(is, clientKeystorePassword.toCharArray());
            }

            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();
            if (allowSelfSigned) {
                sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
            } else {
                sslContextBuilder.loadTrustMaterial(truststore, null);
                sslContextBuilder.loadKeyMaterial(keyStore, clientKeystorePassword.toCharArray());
            }
            SSLContext sslContext = sslContextBuilder.build();

            HostnameVerifier hostnameVerifier = allowAnyHost ? NoopHostnameVerifier.INSTANCE : new DefaultHostnameVerifier();

            SchemeIOSessionStrategy httpsIOSessionStrategy = new SSLIOSessionStrategy(sslContext, hostnameVerifier);
            asyncClientBuilder.setSSLStrategy(httpsIOSessionStrategy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
