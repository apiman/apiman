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
package org.overlord.apiman.rt.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.overlord.apiman.rt.engine.EngineConfig;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;
import org.overlord.apiman.rt.engine.mem.InMemoryRegistry;
import org.overlord.apiman.rt.test.echo.EchoServer;
import org.overlord.apiman.rt.test.gateway.GatewayServer;
import org.overlord.apiman.rt.war.Gateway;
import org.overlord.apiman.rt.war.connectors.HttpConnectorFactory;
import org.overlord.apiman.test.common.util.TestPlanRunner;

/**
 * Base class for all Gateway tests.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractGatewayTest {
    
    protected static final int ECHO_PORT = 7654;
    protected static final int GATEWAY_PORT = 8080;
    protected static final int GATEWAY_PROXY_PORT = 8081;
    protected static final boolean USE_PROXY = false; // if you set this to true you must start a tcp proxy on 8081
    
    static {
        configureGateway();
    }

    private static EchoServer echoServer = new EchoServer(ECHO_PORT);
    private static GatewayServer gatewayServer = new GatewayServer(GATEWAY_PORT);

    @BeforeClass
    public static void setup() throws Exception {
        echoServer.start();
        gatewayServer.start();
    }

    /**
     * Configures the gateway by settings system properties.
     */
    protected static void configureGateway() {
        System.setProperty(EngineConfig.APIMAN_RT_REGISTRY_CLASS, InMemoryRegistry.class.getName());
        System.setProperty(EngineConfig.APIMAN_RT_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        System.setProperty(EngineConfig.APIMAN_RT_GATEWAY_SERVER_PORT, String.valueOf(GATEWAY_PORT));
    }

    @AfterClass
    public static void shutdown() throws Exception {
        gatewayServer.stop();
        echoServer.stop();
    }

    /**
     * @param service the service to publish
     * @throws PublishingException 
     */
    protected void publishService(Service service) throws PublishingException {
        Gateway.engine.publishService(service);
    }
    
    /**
     * @param application the app to register for the test
     * @throws RegistrationException 
     */
    protected void registerApplication(Application application) throws RegistrationException {
        Gateway.engine.registerApplication(application);
    }

    /**
     * Runs the given test plan.
     * @param planPath
     */
    protected void runTestPlan(String planPath) {
        System.setProperty("apiman-rt-test-gateway.endpoints.echo", getEchoEndpoint());
        runTestPlan(planPath, getClass().getClassLoader());
    }
    
    /**
     * Runs the given test plan.
     * @param planPath
     * @param classLoader
     */
    protected void runTestPlan(String planPath, ClassLoader classLoader) {
        int port = GATEWAY_PORT;
        if (USE_PROXY) {
            port = GATEWAY_PROXY_PORT;
        }
        String baseApiUrl = "http://localhost:" + port;
        TestPlanRunner runner = new TestPlanRunner(baseApiUrl);
        runner.runTestPlan(planPath, classLoader);
    }
    
    /**
     * @return the echo server endpoint
     */
    protected String getEchoEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }

}
