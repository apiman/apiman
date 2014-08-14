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

package org.overlord.apiman.tools.devsvr.gateway;

import org.overlord.apiman.rt.engine.IPolicyFailureFactoryComponent;
import org.overlord.apiman.rt.engine.components.ISharedStateComponent;
import org.overlord.apiman.rt.engine.mem.InMemoryRegistry;
import org.overlord.apiman.rt.engine.mem.InMemorySharedStateComponent;
import org.overlord.apiman.rt.engine.policy.PolicyFactoryImpl;
import org.overlord.apiman.rt.test.server.EchoServer;
import org.overlord.apiman.rt.test.server.GatewayServer;
import org.overlord.apiman.rt.war.WarEngineConfig;
import org.overlord.apiman.rt.war.WarPolicyFailureFactoryComponent;
import org.overlord.apiman.rt.war.connectors.HttpConnectorFactory;


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
        
        System.setProperty(WarEngineConfig.APIMAN_RT_REGISTRY_CLASS, InMemoryRegistry.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_RT_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_RT_POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_RT_GATEWAY_SERVER_PORT, String.valueOf(gatewayPort));

        // Register test components
        System.setProperty(WarEngineConfig.APIMAN_RT_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(), 
                InMemorySharedStateComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_RT_COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(), 
                WarPolicyFailureFactoryComponent.class.getName());

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
