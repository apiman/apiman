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

import io.apiman.common.config.options.GenericOptionsParser;
import io.apiman.common.config.options.Predicates;
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

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import static io.apiman.common.config.options.GenericOptionsParser.keys;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultEsClientFactory extends AbstractClientFactory implements IEsClientFactory {

    private static final int POLL_INTERVAL_SECS = 5;
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(DefaultEsClientFactory.class);

    /**
     * Creates a client from information in the config map.
     *
     * @param config             the configuration
     * @param esIndices          the ES index definitions
     * @param defaultIndexPrefix the default index prefix to use if not specified in the config
     * @return the ES client
     */
    @Override
    public RestHighLevelClient createClient(Map<String, String> config,
        Map<String, EsIndexProperties> esIndices,
        String defaultIndexPrefix) {
        ApimanEsClientOptionsParser parser = new ApimanEsClientOptionsParser(config, defaultIndexPrefix);
        LOGGER.debug("ES client factory config: {0}", parser);
        return this.createEsClient(parser, esIndices);
    }

    /**
     * Creates an HTTP/REST client from a configuration map.
     *
     * @return the ES client
     */
    private RestHighLevelClient createEsClient(ApimanEsClientOptionsParser opts,
        Map<String, EsIndexProperties> esIndexes) {

        String protocol = opts.getProtocol();
        String host = opts.getHost();
        int port = opts.getPort();
        String indexNamePrefix = opts.getIndexNamePrefix();
        int timeout = opts.getTimeout();

        LOGGER.info("Building an Elasticsearch client for {0}://{1}:{2} for index prefix {3}",
            protocol, host, port, indexNamePrefix);

        synchronized (clients) {
            String clientKey = "es:" + host + ':' + port + '/' + indexNamePrefix;

            RestHighLevelClient client;

            if (clients.containsKey(clientKey)) {
                client = clients.get(clientKey);
                LOGGER.info("Use cached Elasticsearch client with client key " + clientKey);
            } else {
                RestClientBuilder clientBuilder = RestClient.builder(new HttpHost(host, port, protocol))
                    .setRequestConfigCallback(builder -> builder.setConnectTimeout(timeout)
                    .setSocketTimeout(timeout));

                HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create();

                opts.getUsernameAndPassword().ifPresent(creds -> {
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

                    credentialsProvider.setCredentials(
                        AuthScope.ANY,
                        new UsernamePasswordCredentials(creds.getUsername(), creds.getPasswordAsString())
                    );

                    asyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                });

                if ("https".equalsIgnoreCase(protocol)) {
                    updateSslConfig(asyncClientBuilder, opts);
                }

                clientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder -> asyncClientBuilder);
                client = new RestHighLevelClient(clientBuilder);

                EsConnectionPoller esConnectionPoller = new EsConnectionPoller(
                    client, 0, POLL_INTERVAL_SECS, Math.toIntExact(opts.getPollingTime())
                );

                // Block and wait for Elasticsearch. Exception will be raised if not successful.
                esConnectionPoller.blockUntilReady();

                // Put client to list if polling is successful
                clients.put(clientKey, client);
                LOGGER.info("Created new Elasticsearch client for {0}://{1}:{2} for index prefix {3}",
                            protocol, host, port, indexNamePrefix);
            }

            if (opts.isInitialize()) {
                this.initializeIndices(client, esIndexes, indexNamePrefix);
            }

            return client;
        }
    }

    /**
     * Configures the SSL connection to use certificates by setting the keystores.
     *
     * @param asyncClientBuilder the client builder
     * @param config             the configuration
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/_encrypted_communication.html">Elasticsearch-Docs</a>
     */
    @SuppressWarnings("nls")
    private void updateSslConfig(HttpAsyncClientBuilder asyncClientBuilder, GenericOptionsParser config) {
        try {
            // TODO(msavy): merge together with TLSOptions?
            final boolean allowSelfSigned = config.getBool(keys("client.allowSelfSigned"), false);
            final boolean allowAnyHost = config.getBool(keys("client.allowAnyHost"), false);

            String clientKeystorePath = config.getRequiredString(
                keys("client.keystore.path", "client.keystore"),
                Predicates.fileExists(),
                Predicates.fileExistsMsg("key store")
            );

            String clientKeystorePassword = config.getString(
                keys("client.keystore.password"),
                null,
                Predicates.anyOk(), ""
            );

            String clientKeystoreFormat = config.getString(
                keys("client.keystore.format"),
                "jks",
                Predicates.matchesAny("pkcs12", "jks"),
                "format must be jks or pkcs12"
            );

            String trustStorePath = config.getRequiredString(
                keys("client.truststore.path", "client.truststore"),
                Predicates.fileExists(),
                Predicates.fileExistsMsg("trust store")
            );

            String trustStorePassword = config.getString(
                keys("client.truststore.password"),
                null,
                Predicates.anyOk(), ""
            );

            String trustStoreFormat = config.getString(
                keys("client.truststore.format"),
                "jks",
                Predicates.matchesAny("pkcs12", "jks"),
                "format must be jks or pkcs12"
            );

            Path trustStorePathObject = Paths.get(trustStorePath);
            Path keyStorePathObject = Paths.get(clientKeystorePath);
            KeyStore truststore = KeyStore.getInstance(trustStoreFormat);
            KeyStore keyStore = KeyStore.getInstance(clientKeystoreFormat);

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

            HostnameVerifier hostnameVerifier =
                allowAnyHost ? NoopHostnameVerifier.INSTANCE : new DefaultHostnameVerifier();

            asyncClientBuilder.setSSLStrategy(
                new SSLIOSessionStrategy(sslContext, hostnameVerifier)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
