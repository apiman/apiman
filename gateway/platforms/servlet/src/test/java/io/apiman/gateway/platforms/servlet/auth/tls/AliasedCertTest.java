package io.apiman.gateway.platforms.servlet.auth.tls;

import io.apiman.common.config.options.TLSOptions;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.platforms.servlet.connectors.ConnectorConfigImpl;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
import org.junit.Test;

public class AliasedCertTest {

    private Server server;
    private HttpConfiguration http_config;
    private Map<String, String> config = new HashMap<>();
    //private java.security.cert.X509Certificate clientCertUsed;

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

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(getResourcePath("2waytest/aliased_keys/service_ks.jks"));

        sslContextFactory.setKeyStorePassword("changeme");
        sslContextFactory.setKeyManagerPassword("changeme");
        sslContextFactory.setTrustStorePath(getResourcePath("2waytest/aliased_keys/service_ts.jks"));
        sslContextFactory.setTrustStorePassword("changeme");
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

    ApiRequest request = new ApiRequest();
    Api api = new Api();
    {
        request.setApiKey("12345");
        request.setDestination("/");
        request.getHeaders().put("test", "it-worked");
        request.setTransportSecure(true);
        request.setRemoteAddr("https://localhost:8008/");
        request.setType("GET");

        api.setEndpoint("https://localhost:8008/");
        api.getEndpointProperties().put(RequiredAuthType.ENDPOINT_AUTHORIZATION_TYPE, "mtls");
    }

    /**
     * Scenario:
     *   - Select client key alias `gateway2`.
     *   - Mutual trust exists between gateway and API
     *   - We must use the `gateway2` cert NOT `gateway`.
     * @throws CertificateException the certificate exception
     * @throws IOException the IO exception
     */
    @Test
    public void shouldSucceedWhenValidKeyAlias() throws CertificateException, IOException  {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/aliased_keys/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "changeme");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/aliased_keys/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "changeme");
        config.put(TLSOptions.TLS_KEYPASSWORD, "changeme");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "true");
        config.put(TLSOptions.TLS_KEYALIASES, "gatewayalias");

        X509Certificate expectedCert;
        try(InputStream inStream = new FileInputStream(getResourcePath("2waytest/aliased_keys/gatewayalias.cer"))) {
            expectedCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inStream);
        }

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IApiConnector connector = factory.createConnector(request, api, RequiredAuthType.MTLS, false, new ConnectorConfigImpl());
        IApiConnection connection = connector.connect(request,
            (IAsyncResult<IApiConnectionResponse> result) -> {
                if (result.isError())
                    throw new RuntimeException(result.getError());

                Assert.assertTrue(result.isSuccess());
                // Assert that the expected certificate (associated with the private key by virtue)
                // was the one used.
                Assert.assertEquals(expectedCert.getSerialNumber(), clientSerial);
            });

        connection.end();
    }

    /**
     * Scenario:
     *   - First alias invalid, second valid.
     *   - Mutual trust exists between gateway and API.
     *   - We must fall back to the valid alias.
     * @throws CertificateException the certificate exception
     * @throws IOException the IO exception
     */
    @Test
    public void shouldFallbackWhenMultipleAliasesAvailable() throws CertificateException, IOException  {
        config.put(TLSOptions.TLS_TRUSTSTORE, getResourcePath("2waytest/aliased_keys/gateway_ts.jks"));
        config.put(TLSOptions.TLS_TRUSTSTOREPASSWORD, "changeme");
        config.put(TLSOptions.TLS_KEYSTORE, getResourcePath("2waytest/aliased_keys/gateway_ks.jks"));
        config.put(TLSOptions.TLS_KEYSTOREPASSWORD, "changeme");
        config.put(TLSOptions.TLS_KEYPASSWORD, "changeme");
        config.put(TLSOptions.TLS_ALLOWANYHOST, "true");
        config.put(TLSOptions.TLS_ALLOWSELFSIGNED, "true");
        // Only gateway2 is valid. `unrelated` is real but not trusted by API. others don't exist.
        config.put(TLSOptions.TLS_KEYALIASES, "unrelated, owt, or, nowt, gateway, sonorous, unrelated");

        X509Certificate expectedCert;
        try(InputStream inStream = new FileInputStream(getResourcePath("2waytest/aliased_keys/gateway.cer"))) {
            expectedCert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inStream);
        }

        HttpConnectorFactory factory = new HttpConnectorFactory(config);
        IApiConnector connector = factory.createConnector(request, api, RequiredAuthType.MTLS, false, new ConnectorConfigImpl());
        IApiConnection connection = connector.connect(request,
            (IAsyncResult<IApiConnectionResponse> result) -> {
                if (result.isError())
                    throw new RuntimeException(result.getError());

                Assert.assertTrue(result.isSuccess());
                // Assert that the expected certificate (associated with the private key by virtue)
                // was the one used.
                Assert.assertEquals(expectedCert.getSerialNumber(), clientSerial);
            });

        connection.end();
    }

    private String getResourcePath(String res) {
        URL resource = CAMutualAuthTest.class.getResource(res);
        try {
            System.out.println(res);
            return Paths.get(resource.toURI()).toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
