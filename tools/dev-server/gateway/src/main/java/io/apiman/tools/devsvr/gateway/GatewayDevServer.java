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

package io.apiman.tools.devsvr.gateway;

import io.apiman.gateway.engine.components.IDataStoreComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.impl.InMemoryDataStoreComponent;
import io.apiman.gateway.engine.impl.InMemoryRateLimiterComponent;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.impl.InMemorySharedStateComponent;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.WarEngineConfig;
import io.apiman.gateway.test.server.EchoServer;
import io.apiman.gateway.test.server.GatewayServer;

import java.io.File;


/**
 * A dev server for APIMan.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayDevServer {

    private static final int GATEWAY_PORT  = 6666;
    private static final int ECHO_PORT     = 9001;
    private static final String APIMAN_RT_GATEWAY_SERVER_PORT = "apiman.gateway.server.port"; //$NON-NLS-1$
    private static final String ECHO_PORT_PROPERTY = "apiman.echo.server.port"; //$NON-NLS-1$

    /**
     * Main entry point.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        int gatewayPort = getGatewayPort();
        int echoPort = getEchoPort();
        
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS, InMemoryRegistry.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
        
        if (System.getProperty("apiman.gateway.m2-repository-path") == null) { //$NON-NLS-1$
            System.setProperty("apiman.gateway.m2-repository-path", new File("src/main/resources/plugin").getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Register test components
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(), 
                InMemorySharedStateComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IDataStoreComponent.class.getSimpleName(), 
                InMemoryDataStoreComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName(), 
                InMemoryRateLimiterComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(), 
                PolicyFailureFactoryComponent.class.getName());

        GatewayServer server = new GatewayServer(gatewayPort);
        server.start();
        EchoServer echo = new EchoServer(echoPort);
        echo.start();
        while (true) {
            Thread.sleep(5000);
        }
    }

    /**
     * @return the gateway port to use
     */
    private static int getGatewayPort() {
        int port = GATEWAY_PORT;
        if (System.getProperty(APIMAN_RT_GATEWAY_SERVER_PORT) != null) {
            port = new Integer(System.getProperty(APIMAN_RT_GATEWAY_SERVER_PORT));
        }
        return port;
    }

    /**
     * @return the port to start the echo server on
     */
    private static int getEchoPort() {
        int port = ECHO_PORT;
        if (System.getProperty(ECHO_PORT_PROPERTY) != null) {
            port = new Integer(System.getProperty(ECHO_PORT_PROPERTY));
        }
        return port;
    }
}
