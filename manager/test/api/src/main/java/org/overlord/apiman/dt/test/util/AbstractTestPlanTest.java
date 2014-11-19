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
package org.overlord.apiman.dt.test.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.overlord.apiman.dt.api.core.util.PolicyTemplateUtil;
import org.overlord.apiman.dt.api.war.config.Config;
import org.overlord.apiman.dt.test.server.DtApiTestServer;
import org.overlord.apiman.dt.test.server.MockGatewayServlet;
import org.overlord.apiman.test.common.util.TestPlanRunner;

/**
 * Base class for all junit integration tests for dt api.
 * 
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractTestPlanTest {

    private static DtApiTestServer testServer = new DtApiTestServer();
    private static final boolean USE_PROXY = false;

    @BeforeClass
    public static void setup() throws Exception {
        if (!"true".equals(System.getProperty("apiman.junit.no-server", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            testServer.start();
        } else {
            System.out.println("**** APIMan Server suppressed - assuming running tests against a live server. ****"); //$NON-NLS-1$
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
        String baseApiUrl = "http://localhost:" + getTestServerPort() + getBaseApiContext(); //$NON-NLS-1$
        TestPlanRunner runner = new TestPlanRunner(baseApiUrl);
        configureSystemProperties();
        runner.runTestPlan(planPath, classLoader);
    }

    /**
     * @return the base context of the DT API
     */
    protected String getBaseApiContext() {
        return System.getProperty("apiman.junit.server-api-context", "/apiman"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return the port to use when sending requests
     */
    protected int getTestServerPort() {
        String spPort = System.getProperty("apiman.junit.server-port"); //$NON-NLS-1$
        if (spPort != null) {
            return Integer.parseInt(spPort);
        }
        if (USE_PROXY) {
            return 7071;
        } else {
            return testServer.serverPort();
        }
    }

    /**
     * Configure some proeprties.
     */
    private void configureSystemProperties() {
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_AUTH_TYPE, "Basic"); //$NON-NLS-1$
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_REST_ENDPOINT, "http://localhost:" + getTestServerPort() + "/mock-gateway"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_BASIC_AUTH_USER, "admin"); //$NON-NLS-1$
        System.setProperty(Config.APIMAN_DT_API_GATEWAY_BASIC_AUTH_PASS, "admin"); //$NON-NLS-1$
    }

    @AfterClass
    public static void shutdown() throws Exception {
        if (!"true".equals(System.getProperty("apiman.junit.no-server", "false"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            testServer.stop();
        }
    }

}
