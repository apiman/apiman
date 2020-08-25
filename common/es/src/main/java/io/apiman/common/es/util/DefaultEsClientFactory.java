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

import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.ssl.KeyStoreUtil;
import io.apiman.common.util.ssl.KeyStoreUtil.Info;
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
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultEsClientFactory extends AbstractClientFactory implements IEsClientFactory {

    private IApimanLogger logger = new DefaultDelegateFactory().createLogger(DefaultEsClientFactory.class);

    /**
     * Creates a client from information in the config map.
     * @param config the configuration
     * @param defaultIndexPrefix the default index prefix to use if not specified in the config
     * @param defaultIndices the default indices for the component
     * @return the ES client
     */
    @Override
    public RestHighLevelClient createClient(Map<String, String> config, String defaultIndexPrefix, List<String> defaultIndices) {
        RestHighLevelClient client;
        String indexNamePrefix = config.get("client.indexPrefix"); //$NON-NLS-1$
        if (indexNamePrefix == null) {
            indexNamePrefix = defaultIndexPrefix;
        }
        client = this.createEsClient(config, indexNamePrefix, defaultIndices);
        return client;
    }

    /**
     * Creates a transport client from a configuration map.
     * @param config the configuration
     * @param indexNamePrefix the prefix of the index
     * @param defaultIndices the default indices for the component
     * @return the ES client
     */
    private RestHighLevelClient createEsClient(Map<String, String> config, String indexNamePrefix, List<String> defaultIndices) {
        String host = config.get("client.host"); //$NON-NLS-1$
        Integer port = NumberUtils.toInt(config.get("client.port"), 9200); //$NON-NLS-1$
        String protocol = config.get("client.protocol"); //$NON-NLS-1$
        String initialize = config.get("client.initialize"); //$NON-NLS-1$
        String username = config.get("client.username"); //$NON-NLS-1$
        String password = config.get("client.password"); //$NON-NLS-1$
        Integer timeout = NumberUtils.toInt(config.get("client.timeout"), 10000); //$NON-NLS-1$

        if (StringUtils.isBlank(protocol)) {
            protocol = "http"; //$NON-NLS-1$
        }

        if (StringUtils.isBlank(initialize)) {
            initialize = "true"; //$NON-NLS-1$
        }

        if (StringUtils.isBlank(host)) {
            throw new RuntimeException("Missing client.host configuration for EsRegistry."); //$NON-NLS-1$
        }

        logger.info(String.format("Create elasticsearch client for %s:%d over %s", host, port, protocol));

        synchronized (clients) {
            String clientKey = StringUtils.isNotBlank(indexNamePrefix) ? "es:" + host + ':' + port + '/' + indexNamePrefix : null;

            RestHighLevelClient client;

            if (clientKey != null && clients.containsKey(clientKey)) {
                client = clients.get(clientKey);
            } else {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                if(username != null && password != null) {
                    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                }

                RestClientBuilder clientBuilder = RestClient.builder(
                        new HttpHost(host, port, protocol)
                );

                clientBuilder.setRequestConfigCallback(builder -> builder.setConnectTimeout(timeout)
                        .setSocketTimeout(timeout));

                if(username != null && password != null) {
                    clientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder ->
                            httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
                }

                if ("https".equals(protocol)) { //$NON-NLS-1$ //$NON-NLS-2$
                    updateSslConfig(clientBuilder, config);
                }

                client = new RestHighLevelClient(clientBuilder);

                if(clientKey != null) {
                    clients.put(clientKey, client);
                }
            }

            if ("true".equals(initialize)) { //$NON-NLS-1$
                this.initializeIndices(client, indexNamePrefix, defaultIndices);
            }

            return client;
        }
    }

    /**
     * Configures the SSL connection to use certificates by setting the keystores
     * @param clientBuilder the client builder
     * @param config the configuration
     */
    @SuppressWarnings("nls")
    private void updateSslConfig(RestClientBuilder clientBuilder, Map<String, String> config) {
        try {
            String clientKeystorePath = config.get("client.keystore");
            String clientKeystorePassword = config.get("client.keystore.password");
            String trustStorePath = config.get("client.truststore");
            String trustStorePassword = config.get("client.truststore.password");

            Path trustStorePathObject = Paths.get(trustStorePath);
            KeyStore truststore = KeyStore.getInstance("pkcs12");
            try (InputStream is = Files.newInputStream(trustStorePathObject)) {
                truststore.load(is, trustStorePassword.toCharArray());
            }

            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();

            String trustCertificate = config.get("client.trust.certificate");
            if (!StringUtils.isBlank(trustCertificate) && trustCertificate.equals("true")) {
                sslContextBuilder = sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
            }

            SSLContext sslContext = sslContextBuilder.build();
            Info kPathInfo = new Info(clientKeystorePath, clientKeystorePassword);
            Info tPathInfo = new Info(trustStorePath, trustStorePassword);
            sslContext.init(KeyStoreUtil.getKeyManagers(kPathInfo), KeyStoreUtil.getTrustManagers(tPathInfo), new SecureRandom());

            String trustHost = config.get("client.trust.host");
            HostnameVerifier hostnameVerifier = !StringUtils.isBlank(trustHost) && trustHost.equals("true") ? NoopHostnameVerifier.INSTANCE : new DefaultHostnameVerifier();

            SchemeIOSessionStrategy httpsIOSessionStrategy = new SSLIOSessionStrategy(sslContext, hostnameVerifier);

            //set the ssl context
            clientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
                return httpClientBuilder.setSSLContext(sslContext)
                        .setSSLHostnameVerifier(hostnameVerifier)
                        .setSSLStrategy(httpsIOSessionStrategy);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
