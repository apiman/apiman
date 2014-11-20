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

package io.apiman.tools.devsvr.manager.api;

import io.apiman.manager.api.war.config.Config;
import io.apiman.manager.test.server.DtApiTestServer;
import io.apiman.manager.test.server.ISeeder;

/**
 * A dev server for APIMan.
 *
 * @author eric.wittmann@redhat.com
 */
public class DtApiDevServer {
    
    private static final int GATEWAY_PORT  = 6666;
    private static final String APIMAN_RT_GATEWAY_SERVER_PORT = "apiman.gateway.server.port"; //$NON-NLS-1$

    /**
     * Main entry point.
     * @param args
     */
    public static void main(String [] args) throws Exception {
        int gatewayPort = getGatewayPort();
        
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_REST_ENDPOINT, "http://localhost:" + gatewayPort); //$NON-NLS-1$
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_AUTH_TYPE, "basic"); //$NON-NLS-1$
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_BASIC_AUTH_USER, "admin"); //$NON-NLS-1$
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_BASIC_AUTH_PASS, "admin"); //$NON-NLS-1$
        
        System.setProperty(ISeeder.SYSTEM_PROPERTY, DtApiDataSeeder.class.getName());
        DtApiTestServer server = new DtApiTestServer();
        server.start();
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

}
