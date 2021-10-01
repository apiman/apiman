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
package io.apiman.manager.api.gateway.rest;

import io.apiman.common.util.AesEncrypter;
import io.apiman.common.util.ApimanStrLookup;
import io.apiman.common.util.crypt.CurrentDataEncrypter;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.GatewayEndpoint;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.RestGatewayConfigBean;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.i18n.Messages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * An implementation of a Gateway Link that uses the Gateway's simple REST
 * API to publish APIs.
 *
 * @author eric.wittmann@redhat.com
 */
public class RestGatewayLink implements IGatewayLink {

    private static final StrLookup LOOKUP = new ApimanStrLookup();
    private static final StrSubstitutor PROPERTY_SUBSTITUTOR = new StrSubstitutor(LOOKUP);
    static {
        PROPERTY_SUBSTITUTOR.setValueDelimiter(':');
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final SSLConnectionSocketFactory sslConnectionFactory;
    static {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            sslConnectionFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    private GatewayBean gateway;
    private final CloseableHttpClient httpClient;
    private GatewayClient gatewayClient;
    private RestGatewayConfigBean config;

    /**
     * Constructor.
     * @param gateway the gateway
     */
    public RestGatewayLink(final GatewayBean gateway) {
        try {
            this.gateway = gateway;
            String cfg = gateway.getConfiguration();
            cfg = CurrentDataEncrypter.instance.decrypt(cfg, new DataEncryptionContext());
            cfg = PROPERTY_SUBSTITUTOR.replace(cfg);
            setConfig((RestGatewayConfigBean) mapper.reader(RestGatewayConfigBean.class).readValue(cfg));
            getConfig().setPassword(AesEncrypter.decrypt(getConfig().getPassword()));
            httpClient = HttpClientBuilder.create()
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setSSLSocketFactory(sslConnectionFactory)
                    .addInterceptorFirst(new HttpRequestInterceptor() {
                        @Override
                        public void process(HttpRequest request, HttpContext context) throws HttpException,
                                IOException {
                            configureBasicAuth(request);
                        }
                    }).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#close()
     */
    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            // TODO log the error?
        }
    }

    /**
     * Checks that the gateway is up.
     */
    private boolean isGatewayUp() throws GatewayAuthenticationException {
        SystemStatus status = getClient().getStatus();
        return status.isUp();
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#getStatus()
     */
    @Override
    public SystemStatus getStatus() throws GatewayAuthenticationException {
        return getClient().getStatus();
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#getGatewayEndpoint()
     */
    public GatewayEndpoint getGatewayEndpoint() throws GatewayAuthenticationException {
        return getClient().getGatewayEndpoint();
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#getApiEndpoint(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version)
            throws GatewayAuthenticationException {
        return getClient().getApiEndpoint(organizationId, apiId, version);
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#publishApi(io.apiman.gateway.engine.beans.Api)
     */
    @Override
    public void publishApi(Api api) throws PublishingException, GatewayAuthenticationException {
        if (!isGatewayUp()) {
            throw new PublishingException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().publish(api);
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#retireApi(io.apiman.gateway.engine.beans.Api)
     */
    @Override
    public void retireApi(Api api) throws PublishingException, GatewayAuthenticationException {
        if (!isGatewayUp()) {
            throw new PublishingException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().retire(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#registerClient(io.apiman.gateway.engine.beans.Client)
     */
    @Override
    public void registerClient(Client client) throws RegistrationException, GatewayAuthenticationException {
        if (!isGatewayUp()) {
            throw new RegistrationException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().register(client);
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#unregisterClient(io.apiman.gateway.engine.beans.Client)
     */
    @Override
    public void unregisterClient(Client client) throws RegistrationException, GatewayAuthenticationException {
        if (!isGatewayUp()) {
            throw new RegistrationException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().unregister(client.getOrganizationId(), client.getClientId(), client.getVersion());
    }

    @Override
    public IPolicyProbeResponse probe(String orgId, String apiId, String apiVersion, int idx) throws RegistrationException, GatewayAuthenticationException {
        if (!isGatewayUp()) {
            throw new RegistrationException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        return getClient().probePolicy(orgId, apiId, apiVersion, idx);
    }

    @Override
    public IPolicyProbeResponse probe(String orgId, String apiId, String apiVersion, int idx, String apiKey) throws RegistrationException, GatewayAuthenticationException {
        if (!isGatewayUp()) {
            throw new RegistrationException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        return getClient().probePolicy(orgId, apiId, apiVersion, idx, apiKey);
    }

    /**
     * Configures BASIC authentication for the request.
     * @param request
     */
    protected void configureBasicAuth(HttpRequest request) {
        String username = getConfig().getUsername();
        String password = getConfig().getPassword();
        String up = username + ":" + password; //$NON-NLS-1$
        String base64 = new String(Base64.encodeBase64(up.getBytes(StandardCharsets.UTF_8))); //$NON-NLS-1$
        String authHeader = "Basic " + base64; //$NON-NLS-1$
        request.setHeader("Authorization", authHeader); //$NON-NLS-1$
    }

    /**
     * @return the gateway client
     */
    protected GatewayClient getClient() {
        if (gatewayClient == null) {
            gatewayClient = createClient();
        }
        return gatewayClient;
    }

    /**
     * @return a newly created rest gateway client
     */
    private GatewayClient createClient() {
        String gatewayEndpoint = getConfig().getEndpoint();
        return new GatewayClient(gatewayEndpoint, httpClient);
    }

    /**
     * @return the config
     */
    public RestGatewayConfigBean getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(RestGatewayConfigBean config) {
        this.config = config;
    }

}
