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
package io.apiman.manager.test.util;

import io.apiman.manager.api.core.util.PolicyTemplateUtil;
import io.apiman.manager.test.server.ManagerApiTestServer;
import io.apiman.manager.test.server.MockGatewayServlet;
import io.apiman.test.common.util.TestPlanRunner;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Base class for all junit integration tests for dt api.
 * 
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public abstract class AbstractTestPlanTest {

    private static ManagerApiTestServer testServer = new ManagerApiTestServer();
    private static final boolean USE_PROXY = false;
    private static final int PROXY_PORT = 7071;

    @BeforeClass
    public static void setup() throws Exception {
        if (!"true".equals(System.getProperty("apiman.junit.no-server", "false"))) {
            testServer.start();
        } else {
            System.out.println("**** APIMan Server suppressed - assuming running tests against a live server. ****");
        }
    }

    /**
     * Runs the given test plan.
     * @param planPath
     * @param classLoader
     */
    protected void runTestPlan(String planPath, ClassLoader classLoader) {
        PolicyTemplateUtil.clearCache();
        MockGatewayServlet.reset();
        String baseApiUrl = "http://localhost:" + getTestServerPort() + getBaseApiContext();
        TestPlanRunner runner = new TestPlanRunner(baseApiUrl);
        configureSystemProperties();
        runner.runTestPlan(planPath, classLoader);
    }

    /**
     * @return the base context of the DT API
     */
    protected String getBaseApiContext() {
        return System.getProperty("apiman.junit.server-api-context", "/apiman");
    }

    /**
     * @return the port to use when sending requests
     */
    protected int getTestServerPort() {
        String spPort = System.getProperty("apiman.junit.server-port");
        if (spPort != null) {
            return Integer.parseInt(spPort);
        }
        if (USE_PROXY) {
            return PROXY_PORT;
        } else {
            return testServer.serverPort();
        }
    }

    /**
     * Configure some proeprties.
     */
    private void configureSystemProperties() {
        System.setProperty("apiman.test.gateway.endpoint", "http://localhost:" + getTestServerPort() + "/mock-gateway");
        System.setProperty("apiman.test.gateway.username", "admin");
        System.setProperty("apiman.test.gateway.password", "admin");
        System.setProperty("apiman.manager.require-auto-granted-org", "false");
    }

    @AfterClass
    public static void shutdown() throws Exception {
        if (!"true".equals(System.getProperty("apiman.junit.no-server", "false"))) {
            testServer.stop();
        }
    }

}
