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
package io.apiman.gateway.test.junit.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IJdbcComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.es.ESRateLimiterComponent;
import io.apiman.gateway.engine.es.ESRegistry;
import io.apiman.gateway.engine.es.ESSharedStateComponent;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultJdbcComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.impl.InMemoryCacheStoreComponent;
import io.apiman.gateway.engine.impl.InMemoryRateLimiterComponent;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.impl.InMemorySharedStateComponent;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.components.HttpClientComponentImpl;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.WarEngineConfig;
import io.apiman.gateway.test.server.GatewayServer;
import io.apiman.gateway.test.server.GatewayTestType;
import io.apiman.gateway.test.server.GatewayTestUtils;
import io.apiman.gateway.test.server.TestMetrics;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.resttest.IGatewayTestServer;

import java.io.File;
import java.util.Iterator;

/**
 * A servlet version of the gateway test server.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class ServletGatewayTestServer implements IGatewayTestServer {

    protected static final int ECHO_PORT = 7654;
    protected static final int GATEWAY_PORT = 6060;
    protected static final int GATEWAY_PROXY_PORT = 6061;
    protected static final boolean USE_PROXY = false; // if you set this to true you must start a tcp proxy on 8081

    private EchoServer echoServer = new EchoServer(ECHO_PORT);
    private GatewayServer gatewayServer = new GatewayServer(GATEWAY_PORT);

    /**
     * Constructor.
     */
    public ServletGatewayTestServer() {
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#configure(JsonNode)
     */
    @Override
    public void configure(JsonNode config) {
        String testType = config.get("type").asText();
        System.setProperty("apiman.test.type", testType);
        configureGateway();
        if (config.has("config-properties")) {
            JsonNode configNode = config.get("config-properties");
            Iterator<String> fieldNames = configNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String value = configNode.get(fieldName).asText();
                System.setProperty(fieldName, value);
            }
        }
    }

    /**
     * Configures the gateway by settings system properties.
     */
    protected static void configureGateway() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS + ".pluginsDir", new File("target/plugintmp").getAbsolutePath());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(),
                PolicyFailureFactoryComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IHttpClientComponent.class.getSimpleName(),
                HttpClientComponentImpl.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IBufferFactoryComponent.class.getSimpleName(),
                ByteBufferFactoryComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS, TestMetrics.class.getName());

        if (GatewayTestUtils.getTestType() == GatewayTestType.memory) {
            // Configure to run with in-memory components
            /////////////////////////////////////////////
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS, InMemoryRegistry.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(),
                    InMemorySharedStateComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName(),
                    InMemoryRateLimiterComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ICacheStoreComponent.class.getSimpleName(),
                    InMemoryCacheStoreComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IJdbcComponent.class.getSimpleName(),
                    DefaultJdbcComponent.class.getName());
        } else if (GatewayTestUtils.getTestType() == GatewayTestType.es) {
            // Configure to run with elasticsearch components
            /////////////////////////////////////////////////
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS, ESRegistry.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS + ".client.type", "local");
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS + ".client.class", GatewayServer.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS + ".client.field", "ES_CLIENT");

            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(),
                    ESSharedStateComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName() + ".client.type",
                    "local");
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName() + ".client.class",
                    GatewayServer.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName() + ".client.field",
                    "ES_CLIENT");

            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName(),
                    ESRateLimiterComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName() + ".client.type",
                    "local");
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName() + ".client.class",
                    GatewayServer.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName() + ".client.field",
                    "ES_CLIENT");
        }
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#getApiEndpoint()
     */
    @Override
    public String getApiEndpoint() {
        int port = GATEWAY_PORT;
        if (USE_PROXY) {
            port = GATEWAY_PROXY_PORT;
        }
        String baseApiUrl = "http://localhost:" + port + "/api";
        return baseApiUrl;
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#getGatewayEndpoint()
     */
    @Override
    public String getGatewayEndpoint() {
        int port = GATEWAY_PORT;
        if (USE_PROXY) {
            port = GATEWAY_PROXY_PORT;
        }
        String baseApiUrl = "http://localhost:" + port + "/gateway";
        return baseApiUrl;
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#getEchoTestEndpoint()
     */
    @Override
    public String getEchoTestEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#start()
     */
    @Override
    public void start() {
        try {
            echoServer.start();
            gatewayServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#stop()
     */
    @Override
    public void stop() {
        try {
            gatewayServer.stop();
            echoServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
