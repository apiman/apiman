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

import org.overlord.apiman.rt.engine.EngineConfig;
import org.overlord.apiman.rt.engine.mem.InMemoryRegistry;
import org.overlord.apiman.rt.test.server.EchoServer;
import org.overlord.apiman.rt.test.server.GatewayServer;
import org.overlord.apiman.rt.war.connectors.HttpConnectorFactory;


/**
 * A dev server for APIMan.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayDevServer {

    private static final int GATEWAY_PORT  = 6666;
    private static final int ECHO_PORT     = 9001;

    /**
     * Main entry point.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        System.setProperty(EngineConfig.APIMAN_RT_REGISTRY_CLASS, InMemoryRegistry.class.getName());
        System.setProperty(EngineConfig.APIMAN_RT_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        System.setProperty(EngineConfig.APIMAN_RT_GATEWAY_SERVER_PORT, String.valueOf(GATEWAY_PORT));

        GatewayServer server = new GatewayServer(GATEWAY_PORT);
        server.start();
        EchoServer echo = new EchoServer(ECHO_PORT);
        echo.start();
        while (true) {
            Thread.sleep(5000);
        }
    }
}
