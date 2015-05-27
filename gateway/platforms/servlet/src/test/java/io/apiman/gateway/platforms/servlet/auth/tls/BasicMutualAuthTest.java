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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
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
 * Important note from {@link SSLSocket#getNeedClientAuth()} about requiring client auth:
 * <p>
 * <q>... if this option is set and the client chooses not to provide authentication information about itself,
 * the negotiations will stop and the engine will begin its closure procedure.</q>
 * <p>
 * Hence we often capture an {@link ConnectorException} in tests when 2-way auth is failed.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class BasicMutualAuthTest {

    private Server server;
    private HttpConfiguration http_config;
    private Map<String, String> config = new HashMap<>();
    //private java.security.cert.X509Certificate clientCertUsed;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    protected BigInteger clientSerial;

    /**
     * With thanks to assistance of http://stackoverflow.com/b/20056601/2766538
     * @throws Exception any exception
     */
    @Before
    public void setupJetty() throws Exception {
        server = new Server();
        server.setStopAtShutdown(true);

        http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(getResourcePath("2waytest/basic_mutual_auth/service_ks.jks"));

        sslContextFactory.setKeyStorePassword("password");
        sslContextFactory.setKeyManagerPassword("password");
        sslContextFactory.setTrustStorePath(getResourcePath("2waytest/basic_mutual_auth/service_ts.jks"));
        sslContextFactory.setTrustStorePassword("password");
        sslContextFactory.setNeedClientAuth(true);

        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        ServerConnector sslConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory,"http/1.1"),
            new HttpConnectionFactory(https_config));
        sslConnector.setPort(8008);

        server.addConnector(sslConnector);
        // Thanks to Jetty getting started guide.
        server.setHandler(new AbstractHandler() {

            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request,
                    HttpServletResponse response) throws IOException, ServletException {

                Enumeration<String> z = request.getAttributeNames();

                while (z.hasMoreElements()) {
                    String elem = z.nextElement();
                    System.out.println(elem + " - " + request.getAttribute(elem));
                }

                if (request.getAttribute("javax.servlet.request.X509Certificate") != null) {
                    clientSerial = ((java.security.cert.X509Certificate[]) request
                            .getAttribute("javax.servlet.request.X509Certificate"))[0].getSerialNumber();
                }

                response.setStatus(HttpServletResponse.SC_OK);
                baseRequest.setHandled(true);
                response.getWriter().println("apiman");
            }
        });
        server.start();
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
     *   - no CA inherited trust
     *   - gateway trusts service certificate directly
     *   - service trusts gateway certificate directly
     */
    @Test
    public void shouldSucceedWithValidMTLS() {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");

       HttpConnectorFactory factory = new HttpConnectorFactory(config);
       IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.MTLS);
       IServiceConnection connection = connector.connect(request,
               new IAsyncResultHandler<IServiceConnectionResponse>() {

        @Override
        public void handle(IAsyncResult<IServiceConnectionResponse> result) {
            if (result.isError())
                throw new RuntimeException(result.getError());

          Assert.assertTrue(result.isSuccess());
        }
       });

       connection.end();
    }

    /**
     * Scenario:
     *   - no CA inherited trust
     *   - gateway does <em>not</em> trust the service
     *   - service trusts gateway certificate
     */
    @Test
    public void shouldFailWhenGatewayDoesNotTrustService() {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/basic_mutual_auth/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/basic_mutual_auth/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");

       HttpConnectorFactory factory = new HttpConnectorFactory(config);
       IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.MTLS);
       IServiceConnection connection = connector.connect(request,
               new IAsyncResultHandler<IServiceConnectionResponse>() {

        @Override
        public void handle(IAsyncResult<IServiceConnectionResponse> result) {
            Assert.assertTrue(result.isError());

            System.out.println(result.getError());
            Assert.assertTrue(result.getError() instanceof ConnectorException);
            // Would like to assert on SSL error, but is sun specific info
            // TODO improve connector to handle this situation better
        }
       });

       exception.expect(RuntimeException.class);
       connection.end();
    }

    /**
     * Scenario:
     *   - no CA inherited trust
     *   - gateway does trust the service
     *   - service does <em>not</em> trust gateway
     */
    @Test
    public void shouldFailWhenServiceDoesNotTrustGateway() {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/service_not_trust_gw/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/service_not_trust_gw/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.MTLS);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

         @Override
         public void handle(IAsyncResult<IServiceConnectionResponse> result) {
             Assert.assertTrue(result.isError());

             System.out.println(result.getError());
             Assert.assertTrue(result.getError() instanceof ConnectorException);
             // Would like to assert on SSL error, but is sun specific info
             // TODO improve connector to handle this situation better
         }
        });

        exception.expect(RuntimeException.class);
        connection.end();
    }

    /**
     * Scenario:
     *   - no CA inherited trust
     *   - gateway does not explicitly trust the service, but automatically validates against self-signed
     *   - service trusts gateway certificate
     */
    @Test
    public void shouldSucceedWhenAllowedSelfSigned() {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/basic_mutual_auth/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/basic_mutual_auth/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "true");

       HttpConnectorFactory factory = new HttpConnectorFactory(config);
       IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.MTLS);
       IServiceConnection connection = connector.connect(request,
               new IAsyncResultHandler<IServiceConnectionResponse>() {

        @Override
        public void handle(IAsyncResult<IServiceConnectionResponse> result) {
            Assert.assertTrue(result.isSuccess());
        }
       });

       connection.end();
    }

    /**
     * Scenario:
     *   - Select client key alias `gateway2`.
     *   - Mutual trust exists between gateway and service
     *   - We must use the `gateway2` cert NOT `gateway`.
     * @throws CertificateException the certificate exception
     * @throws IOException the IO exception
     */
    @Test
    public void shouldSucceedWhenValidKeyAlias() throws CertificateException, IOException  {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");

        config.put(TLSOptions.TLS_KEYALIASES, "gateway2");

        InputStream inStream = new FileInputStream(getResourcePath("2waytest/basic_mutual_auth_2/gateway2.cer"));
        final X509Certificate expectedCert = X509Certificate.getInstance(inStream);
        inStream.close();

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.MTLS);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

                    @Override
                    public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                        if (result.isError())
                            throw new RuntimeException(result.getError());

                        Assert.assertTrue(result.isSuccess());
                        // Assert that the expected certificate (associated with the private key by virtue)
                        // was the one used.
                        Assert.assertEquals(expectedCert.getSerialNumber(), clientSerial);
                    }
                });

        connection.end();
    }

    /**
     * Scenario:
     *   - First alias invalid, second valid.
     *   - Mutual trust exists between gateway and service.
     *   - We must fall back to the valid alias.
     * @throws CertificateException the certificate exception
     * @throws IOException the IO exception
     */
    @Test
    public void shouldFallbackWhenMultipleAliasesAvailable() throws CertificateException, IOException  {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        // Only gateway2 is valid. `unrelated` is real but not trusted by service. others don't exist.
        config.put(TLSOptions.TLS_KEYALIASES, "unrelated, owt, or, nowt, gateway2, sonorous, unrelated");

        InputStream inStream = new FileInputStream(getResourcePath("2waytest/basic_mutual_auth_2/gateway2.cer"));
        final X509Certificate expectedCert = X509Certificate.getInstance(inStream);
        inStream.close();

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.MTLS);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

                    @Override
                    public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                        if (result.isError())
                            throw new RuntimeException(result.getError());

                        Assert.assertTrue(result.isSuccess());
                        // Assert that the expected certificate (associated with the private key by virtue)
                        // was the one used.
                        Assert.assertEquals(expectedCert.getSerialNumber(), clientSerial);
                    }
                });

        connection.end();
    }

    /**
     * Scenario:
     *   - Select invalid key alias (no such key).
     *   - Negotiation will fail
     * @throws CertificateException the certificate exception
     * @throws IOException the IO exception
     */
    @Test
    public void shouldFailWithInValidKeyAlias() throws CertificateException, IOException  {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/basic_mutual_auth_2/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "password");
        config.put(TLSOptions.TLS_KEYPASSWORD, "password");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "false");
        // No such key exists in the keystore
        config.put(TLSOptions.TLS_KEYALIASES, "xxx");

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.MTLS);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

                    @Override
                    public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                        Assert.assertTrue(result.isError());
                    }
                });

        exception.expect(RuntimeException.class);
        connection.end();
    }

    /**
     * Scenario:
     *   - Development mode TLS pass-through. Gateway accepts anything.
     *   - Server should still refuse on basis of requiring client auth.
     */
    @Test
    public void shouldFailWithDevModeAndNoClientKeys() {
        config.put(TLSOptions.TLS_DEVMODE, "true");

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IServiceConnector connector = factory.createConnector(request, service, RequiredAuthType.DEFAULT);
        IServiceConnection connection = connector.connect(request,
                new IAsyncResultHandler<IServiceConnectionResponse>() {

         @Override
         public void handle(IAsyncResult<IServiceConnectionResponse> result) {
                 Assert.assertTrue(result.isError());
                 System.out.println(result.getError());
             }
        });

        exception.expect(RuntimeException.class);
        connection.end();
    }

    private String getResourcePath(String res) {
        URL resource = CAMutualAuthTest.class.getResource(res);
        try {
            return Paths.get(resource.toURI()).toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
