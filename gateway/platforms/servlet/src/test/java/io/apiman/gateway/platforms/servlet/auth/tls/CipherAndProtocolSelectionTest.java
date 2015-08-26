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
package io.apiman.gateway.platforms.servlet.auth.tls;

import io.apiman.common.config.options.TLSOptions;
import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests various standard TLS scenarios, in which the client is *not* authenticated. Some of these include
 * authentication of the server certificate, whilst others are all trusting (devmode).
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class CipherAndProtocolSelectionTest {

    private Server server;
    private HttpConfiguration http_config;
    private Map<String, String> config = new HashMap<>();
    private Map<String, String> jettyRequestAttributes;


    @Rule
    public ExpectedException exception = ExpectedException.none();
    private SslContextFactory jettySslContextFactory;

    @Before
    public void setupJetty() throws Exception {
        server = new Server();
        server.setStopAtShutdown(true);

        http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");

        jettySslContextFactory = new SslContextFactory();
        jettySslContextFactory.setTrustStorePath(getResourcePath("2waytest/mutual_trust_via_ca/common_ts.jks"));
        jettySslContextFactory.setTrustStorePassword("password");
        jettySslContextFactory.setKeyStorePath(getResourcePath("2waytest/mutual_trust_via_ca/service_ks.jks"));
        jettySslContextFactory.setKeyStorePassword("password");
        jettySslContextFactory.setKeyManagerPassword("password");
        // Use default trust store
        // No client auth
        jettySslContextFactory.setNeedClientAuth(false);
        jettySslContextFactory.setWantClientAuth(false);

        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(jettySslContextFactory,"http/1.1"),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(8008);

        server.addConnector(sslConnector);
        // Thanks to Jetty getting started guide.
        server.setHandler(new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {

                jettyRequestAttributes = new HashMap<>();
                Enumeration<String> requestAttrNames = request.getAttributeNames();

                while (requestAttrNames.hasMoreElements()) {
                    String elem = requestAttrNames.nextElement();
                    jettyRequestAttributes.put(elem, request.getAttribute(elem).toString());
                }

                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                response.getWriter().println("apiman");
            }
        });
    }

    @After
    public void destroyJetty() throws Exception {
        server.stop();
        server.destroy();
        config.clear();
    }

    ServiceRequest request = new ServiceRequest();
    Service service = new Service();
    {
        request.setApiKey("12345");
        request.setDestination("/");
        request.getHeaders().put("test", "it-worked");
        request.setTransportSecure(true);
        request.setRemoteAddr("https://localhost:8008/");
        request.setType("GET");

        service.setEndpoint("https://localhost:8008/");
        service.getEndpointProperties().put(RequiredAuthType.ENDPOINT_AUTHORIZATION_TYPE, "mtls");
    }


    /**
     * Scenario:
     *   - Should not use a disallowed cipher in the exchange
     * @throws Exception any exception
     */
    @Test
    public void shouldNotUseDisallowedCipher() throws Exception {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/mutual_trust_via_ca/common_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        config.put(TLSOptions.TLS_DISALLOWEDCIPHERS, "TLS_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");

        server.start();

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.DEFAULT);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

            @Override
            public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                if (result.isError())
                    throw new RuntimeException(result.getError());

                Assert.assertTrue(result.isSuccess());
                Assert.assertFalse(jettyRequestAttributes.get("javax.servlet.request.cipher_suite").equals(""));
                Assert.assertFalse(jettyRequestAttributes.get("javax.servlet.request.cipher_suite").equals("TLS_RSA_WITH_AES_128_GCM_SHA256"));
                Assert.assertFalse(jettyRequestAttributes.get("javax.servlet.request.cipher_suite").equals("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"));
            }
        });

        connection.end();
    }

    /**
     * Scenario:
     *   - Only allowed protocol is one that is disallowed by remote end
     * @throws Exception any exception
     */
    @Test
    public void shouldFailWhenNoValidCipherAllowed() throws Exception {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/mutual_trust_via_ca/common_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        config.put(TLSOptions.TLS_ALLOWEDCIPHERS, "TLS_RSA_WITH_AES_128_GCM_SHA256");

        jettySslContextFactory.setExcludeCipherSuites("TLS_RSA_WITH_AES_128_GCM_SHA256");
        server.start();


        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.DEFAULT);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

            @Override
            public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                Assert.assertTrue(result.isError());
                System.out.println(result.getError());
                //result.getError().printStackTrace();
                Assert.assertTrue(result.getError().getCause() instanceof javax.net.ssl.SSLHandshakeException);
            }
           });

           exception.expect(RuntimeException.class);
           connection.end();
    }

    /**
     * Scenario:
     *   - Only allowed cipher is one that is disallowed by remote end
     * @throws Exception any exception
     */
    @Test
    public void shouldFailWhenNoValidProtocolAllowed() throws Exception {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/mutual_trust_via_ca/common_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        config.put(TLSOptions.TLS_ALLOWEDPROTOCOLS, "SSLv3");

        jettySslContextFactory.setExcludeProtocols("SSLv3");
        server.start();


        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.DEFAULT);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

            @Override
            public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                Assert.assertTrue(result.isError());
                System.out.println(result.getError());
                //result.getError().printStackTrace();
                Assert.assertTrue(result.getError().getCause() instanceof java.net.UnknownServiceException);
            }
           });

           exception.expect(RuntimeException.class);
           connection.end();
    }

    /**
     * Scenario:
     *   - Only allowed protocol is one that is disallowed by remote end
     * @throws Exception any exception
     */
    @Test
    public void shouldFailWhenAllAvailableProtocolsExcluded() throws Exception {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/mutual_trust_via_ca/common_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        config.put(TLSOptions.TLS_ALLOWEDPROTOCOLS, "SSLv3");

        jettySslContextFactory.setExcludeProtocols("SSLv3");
        server.start();


        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.DEFAULT);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

            @Override
            public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                Assert.assertTrue(result.isError());
                System.out.println(result.getError());
                Assert.assertTrue(result.getError().getCause() instanceof java.net.UnknownServiceException);
            }
           });

           exception.expect(RuntimeException.class);
           connection.end();
    }

    /**
     * Scenario:
     *   - Only allowed protocol is one that is disallowed by remote end
     * @throws Exception any exception
     */
    @Test
    public void shouldFailWhenRemoteProtocolsAreExcluded() throws Exception {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/mutual_trust_via_ca/common_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        config.put(TLSOptions.TLS_DISALLOWEDPROTOCOLS, "SSLv3");

        jettySslContextFactory.setIncludeProtocols("SSLv3");
        jettySslContextFactory.setExcludeProtocols("SSLv1", "SSLv2", "TLSv1", "TLSv2");

        server.start();

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.DEFAULT);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

            @Override
            public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                Assert.assertTrue(result.isError());
                System.out.println(result.getError());
                Assert.assertTrue(result.getError() instanceof ConnectorException);
            }
           });

           exception.expect(RuntimeException.class);
           connection.end();
    }

    /**
     * Scenario:
     *   - Only allowed protocol is one that is disallowed by remote end
     * @throws Exception any exception
     */
    @Test
    public void shouldFailWhenRemoteCiphersAreExcluded() throws Exception {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/mutual_trust_via_ca/common_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        config.put(TLSOptions.TLS_DISALLOWEDCIPHERS, "TLS_RSA_WITH_AES_128_GCM_SHA256");

        jettySslContextFactory.setIncludeCipherSuites("TLS_RSA_WITH_AES_128_GCM_SHA256");

        server.start();

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.DEFAULT);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

            @Override
            public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                Assert.assertTrue(result.isError());
                System.out.println(result.getError());
                Assert.assertTrue(result.getError().getCause() instanceof javax.net.ssl.SSLHandshakeException);
            }
           });

           exception.expect(RuntimeException.class);
           connection.end();
    }

    private String getResourcePath(String res) {
        URL resource = CipherAndProtocolSelectionTest.class.getResource(res);
        try {
            return Paths.get(resource.toURI()).toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
