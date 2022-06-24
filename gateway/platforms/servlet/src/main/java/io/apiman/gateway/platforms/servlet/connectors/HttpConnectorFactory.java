/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.platforms.servlet.connectors;

import io.apiman.common.config.options.HttpConnectorOptions;
import io.apiman.common.config.options.TLSOptions;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.IConnectorConfig;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.platforms.servlet.connectors.ssl.SSLSessionStrategy;
import io.apiman.gateway.platforms.servlet.connectors.ssl.SSLSessionStrategyFactory;

import java.net.CookieHandler;
import java.net.ProxySelector;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;

import com.squareup.okhttp.CertificatePinner;
import com.squareup.okhttp.ConnectionPool;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.Dns;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.http.AuthenticatorAdapter;

/**
 * Connector factory that uses HTTP to invoke back end systems.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpConnectorFactory implements IConnectorFactory {

    private static final List<ConnectionSpec> DEFAULT_CONNECTION_SPECS = Util.immutableList(
        ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT);

    private final OkHttpClient okClient;

    // Standard auth
    private SSLSessionStrategy standardSslStrategy;
    // 2WAY auth (i.e. mutual auth)
    private SSLSessionStrategy mutualAuthSslStrategy;
    private final TLSOptions tlsOptions;
    private final HttpConnectorOptions connectorOptions;

    /**
     * Constructor.
     *
     * @param config map of configuration options
     */
    public HttpConnectorFactory(Map<String, String> config) {
        this.tlsOptions = new TLSOptions(config);
        this.connectorOptions = new HttpConnectorOptions(config);
        this.okClient = createHttpClient();
    }

    /**
     * @return a new http client
     */
    private OkHttpClient createHttpClient() {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(connectorOptions.getReadTimeout(), TimeUnit.SECONDS);
        client.setWriteTimeout(connectorOptions.getWriteTimeout(), TimeUnit.SECONDS);
        client.setConnectTimeout(connectorOptions.getConnectTimeout(), TimeUnit.SECONDS);
        client.setFollowRedirects(connectorOptions.isFollowRedirects());
        client.setFollowSslRedirects(connectorOptions.isFollowRedirects());
        client.setProxySelector(ProxySelector.getDefault());
        client.setCookieHandler(CookieHandler.getDefault());
        client.setCertificatePinner(CertificatePinner.DEFAULT);
        client.setAuthenticator(AuthenticatorAdapter.INSTANCE);
        client.setConnectionPool(ConnectionPool.getDefault());
        client.setProtocols(Util.immutableList(Protocol.HTTP_1_1));
        client.setConnectionSpecs(DEFAULT_CONNECTION_SPECS);
        client.setSocketFactory(SocketFactory.getDefault());
        client.setDns(Dns.SYSTEM);

        return client;
    }

    /**
     * @see io.apiman.gateway.engine.IConnectorFactory#createConnector(ApiRequest, Api, RequiredAuthType,
     * boolean, IConnectorConfig)
     */
    @Override
    public IApiConnector createConnector(ApiRequest request, final Api api,
        final RequiredAuthType requiredAuthType, boolean hasDataPolicy,
        final IConnectorConfig connectorConfig) {
        return new IApiConnector() {
            /**
             * @see io.apiman.gateway.engine.IApiConnector#connect(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
             */
            @Override
            public IApiConnection connect(ApiRequest request,
                IAsyncResultHandler<IApiConnectionResponse> handler) throws ConnectorException {

                return new HttpApiConnection(okClient, request, api,
                    requiredAuthType, getSslStrategy(requiredAuthType), hasDataPolicy,
                    connectorConfig, handler);
            }
        };
    }

    /**
     * Creates the SSL strategy based on configured TLS options.
     *
     * @param authType
     * @return an appropriate SSL strategy
     */
    protected SSLSessionStrategy getSslStrategy(RequiredAuthType authType) {
        try {
            if (authType == RequiredAuthType.MTLS) {
                if (mutualAuthSslStrategy == null) {
                    mutualAuthSslStrategy = SSLSessionStrategyFactory.buildMutual(tlsOptions);
                }
                return mutualAuthSslStrategy;
            } else {
                if (standardSslStrategy == null) {
                    if (tlsOptions.isDevMode()) {
                        standardSslStrategy = SSLSessionStrategyFactory.buildUnsafe();
                    } else {
                        standardSslStrategy = SSLSessionStrategyFactory.buildStandard(tlsOptions);
                    }
                }
                return standardSslStrategy;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IConnectorConfig createConnectorConfig(ApiRequest request, Api api) {
        return new ConnectorConfigImpl();
    }
}
