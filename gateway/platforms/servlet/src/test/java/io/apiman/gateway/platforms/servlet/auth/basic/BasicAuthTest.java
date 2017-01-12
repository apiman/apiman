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
package io.apiman.gateway.platforms.servlet.auth.basic;

import io.apiman.common.config.options.BasicAuthOptions;
import io.apiman.common.config.options.TLSOptions;
import io.apiman.common.servlet.AuthenticationFilter;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.test.common.mock.EchoServlet;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Basic auth test for the API connector (tests connecting to a back-end API
 * that is protected by basic auth).
 */
@SuppressWarnings("nls")
public class BasicAuthTest {

    private Server server;
    private Map<String, String> globalConfig = new HashMap<>();
    private Map<String, String> endpointProperties = new HashMap<>();
    public ExpectedException exception = ExpectedException.none();

    /**
     * With thanks to assistance of http://stackoverflow.com/b/20056601/2766538
     * @throws Exception any exception
     */
    @Before
    public void setupJetty() throws Exception {
        ContextHandlerCollection handlers = new ContextHandlerCollection();

        ServletContextHandler sch = new ServletContextHandler(ServletContextHandler.SESSIONS);
        sch.setSecurityHandler(createSecurityHandler());
        sch.setContextPath("/echo");
        ServletHolder mockEchoServlet = new ServletHolder(new EchoServlet());
        sch.addServlet(mockEchoServlet, "/*");
        sch.addFilter(AuthenticationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        handlers.addHandler(sch);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustStorePath(getResourcePath("common_ts.jks"));
        sslContextFactory.setTrustStorePassword("password");
        sslContextFactory.setKeyStorePath(getResourcePath("service_ks.jks"));
        sslContextFactory.setKeyStorePassword("password");
        sslContextFactory.setKeyManagerPassword("password");
        sslContextFactory.setNeedClientAuth(false);
        sslContextFactory.setWantClientAuth(false);

        // Create the server.
        int serverPort = 8008;
        server = new Server(serverPort);
        server.setStopAtShutdown(true);

        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https_config));
        sslConnector.setPort(8009);
        server.addConnector(sslConnector);

        server.setHandler(handlers);
        server.start();

        globalConfig.put(TLSOptions.TLS_DEVMODE, "true");
    }

    /**
     * Creates a basic auth security handler.
     */
    private static SecurityHandler createSecurityHandler() {
        HashLoginService l = new HashLoginService();
        String user = "user";
        String pwd = "user123!";
        String[] roles = new String[] { "user" };
        l.putUser(user, Credential.getCredential(pwd), roles);
        l.setName("apimanrealm");

        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("apimanrealm");
        csh.setLoginService(l);

        return csh;
    }

    @After
    public void destroyJetty() throws Exception {
        server.stop();
        server.destroy();
        endpointProperties.clear();
    }

    ApiRequest request = new ApiRequest();
    Api api = new Api();
    {
        request.setApiKey("12345");
        request.setDestination("/");
        request.getHeaders().put("test", "it-worked");
        request.setTransportSecure(true);
        request.setRemoteAddr("http://localhost:8008/echo");
        request.setType("GET");

        api.setEndpoint("http://localhost:8008/echo");
        api.getEndpointProperties().put(RequiredAuthType.ENDPOINT_AUTHORIZATION_TYPE, RequiredAuthType.BASIC.name());
    }

    /**
     * Scenario successful connection to the back end API via basic auth.
     */
    @Test
    public void shouldSucceedWithBasicAuth() {
        endpointProperties.put(BasicAuthOptions.BASIC_USERNAME, "user");
        endpointProperties.put(BasicAuthOptions.BASIC_PASSWORD, "user123!");
        endpointProperties.put(BasicAuthOptions.BASIC_REQUIRE_SSL, "false");
        api.setEndpointProperties(endpointProperties);
        api.setEndpoint("http://localhost:8008/echo");

        HttpConnectorFactory factory = new HttpConnectorFactory(globalConfig);
        IApiConnector connector = factory.createConnector(request, api, RequiredAuthType.BASIC, false);
        IApiConnection connection = connector.connect(request,
                new IAsyncResultHandler<IApiConnectionResponse>() {
                    @Override
                    public void handle(IAsyncResult<IApiConnectionResponse> result) {
                        Assert.assertTrue("Expected a successful connection response.", result.isSuccess());
                        IApiConnectionResponse scr = result.getResult();
                        Assert.assertEquals("Expected a 200 response from the echo server (valid creds).", 200, scr.getHead().getCode());
                    }
                });

        if (connection.isConnected()) {
            connection.end();
        }
    }

    /**
     * Scenario successful connection to the back end API via basic auth.
     */
    @Test
    public void shouldSucceedWithBasicAuthAndSSL() {
        endpointProperties.put(BasicAuthOptions.BASIC_USERNAME, "user");
        endpointProperties.put(BasicAuthOptions.BASIC_PASSWORD, "user123!");
        endpointProperties.put(BasicAuthOptions.BASIC_REQUIRE_SSL, "true");
        api.setEndpointProperties(endpointProperties);
        api.setEndpoint("https://localhost:8009/echo");

        HttpConnectorFactory factory = new HttpConnectorFactory(globalConfig);
        IApiConnector connector = factory.createConnector(request, api, RequiredAuthType.BASIC, false);
        IApiConnection connection = connector.connect(request,
                new IAsyncResultHandler<IApiConnectionResponse>() {
                    @Override
                    public void handle(IAsyncResult<IApiConnectionResponse> result) {
                        Assert.assertTrue("Expected a successful connection response.", result.isSuccess());
                        IApiConnectionResponse scr = result.getResult();
                        Assert.assertEquals("Expected a 200 response from the echo server (valid creds).", 200, scr.getHead().getCode());
                    }
                });

        if (connection.isConnected()) {
            connection.end();
        }
    }

    /**
     * Scenario successful connection to the back end API via basic auth.
     */
    @Test
    public void shouldFailWithNoSSL() {
        endpointProperties.put(BasicAuthOptions.BASIC_USERNAME, "user");
        endpointProperties.put(BasicAuthOptions.BASIC_PASSWORD, "user123!");
        endpointProperties.put(BasicAuthOptions.BASIC_REQUIRE_SSL, "true");
        api.setEndpointProperties(endpointProperties);
        api.setEndpoint("http://localhost:8008/echo");

        HttpConnectorFactory factory = new HttpConnectorFactory(globalConfig);
        IApiConnector connector = factory.createConnector(request, api, RequiredAuthType.BASIC, false);
        IApiConnection connection = connector.connect(request,
                new IAsyncResultHandler<IApiConnectionResponse>() {
                    @Override
                    public void handle(IAsyncResult<IApiConnectionResponse> result) {
                        Assert.assertTrue("Expected an error due to not using SSL.", result.isError());
                        Assert.assertTrue("Expected a ConnectorException due to not using SSL.", result.getError() instanceof ConnectorException);
                        Assert.assertEquals("Endpoint security requested (BASIC auth) but endpoint is not secure (SSL).", result.getError().getMessage());
                    }
                });

        if (connection.isConnected()) {
            connection.end();
        }
    }

    /**
     * Should fail because the credentials provided are not valid/
     */
    @Test
    public void shouldFailWithBadCredentials() {
        endpointProperties.put(BasicAuthOptions.BASIC_USERNAME, "user");
        endpointProperties.put(BasicAuthOptions.BASIC_PASSWORD, "bad-password");
        endpointProperties.put(BasicAuthOptions.BASIC_REQUIRE_SSL, "false");
        api.setEndpointProperties(endpointProperties);
        api.setEndpoint("http://localhost:8008/echo");

        HttpConnectorFactory factory = new HttpConnectorFactory(globalConfig);
        IApiConnector connector = factory.createConnector(request, api, RequiredAuthType.BASIC, false);
        IApiConnection connection = connector.connect(request,
                new IAsyncResultHandler<IApiConnectionResponse>() {
                    @Override
                    public void handle(IAsyncResult<IApiConnectionResponse> result) {
                        Assert.assertTrue("Expected a successful connection response.", result.isSuccess());
                        IApiConnectionResponse scr = result.getResult();
                        Assert.assertEquals("Expected a 401 response from the echo server (invalid creds).", 401, scr.getHead().getCode());
                    }
                });

        if (connection.isConnected()) {
            connection.end();
        }
    }

    /**
     * Should fail because no credentials were provided.
     */
    @Test
    public void shouldFailWithNoCredentials() {
        endpointProperties.remove(BasicAuthOptions.BASIC_USERNAME);
        endpointProperties.remove(BasicAuthOptions.BASIC_PASSWORD);
        endpointProperties.put(BasicAuthOptions.BASIC_REQUIRE_SSL, "false");
        api.setEndpointProperties(endpointProperties);
        api.setEndpoint("http://localhost:8008/echo");

        HttpConnectorFactory factory = new HttpConnectorFactory(globalConfig);
        IApiConnector connector = factory.createConnector(request, api, RequiredAuthType.BASIC, false);
        IApiConnection connection = connector.connect(request,
                new IAsyncResultHandler<IApiConnectionResponse>() {
                    @Override
                    public void handle(IAsyncResult<IApiConnectionResponse> result) {
                        Assert.assertTrue("Expected a successful connection response.", result.isSuccess());
                        IApiConnectionResponse scr = result.getResult();
                        Assert.assertEquals("Expected a 401 response from the echo server (invalid creds).", 401, scr.getHead().getCode());
                    }
                });

        if (connection.isConnected()) {
            connection.end();
        }
    }

    private String getResourcePath(String res) {
        URL resource = BasicAuthTest.class.getResource(res);
        try {
            return Paths.get(resource.toURI()).toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
