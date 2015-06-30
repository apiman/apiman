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
package io.apiman.distro.db;

import io.apiman.manager.test.server.ManagerApiTestServer;
import io.apiman.manager.test.util.ManagerTestUtils;
import io.apiman.manager.test.util.ManagerTestUtils.TestType;
import io.apiman.test.common.util.TestPlanRunner;
import io.apiman.test.common.util.TestUtil;

import java.io.File;

/**
 * Unit test that creates an H2 database file by firing up the API Manager and sending a
 * bunch of REST requests to configure it.  When this test is complete there should be a
 * valid H2 database located in target/classes (and thus be included in the JAR).
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class CreateH2Database {

    protected ManagerApiTestServer testServer = new ManagerApiTestServer();
    private final boolean USE_PROXY = false;
    private final int PROXY_PORT = 7071;

    /**
     * The test suite main entry point.
     * @param args
     * @throws Exception
     */
    public static void main(String [] args) throws Exception {
        CreateH2Database ch2d = new CreateH2Database();
        ch2d.setup();
        ch2d.startServer();
        try {
            ch2d.create();
        } finally {
            ch2d.stopServer();
        }
    }

    /**
     * @throws Exception
     */
    protected void startServer() {
        try {
            testServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws Exception
     */
    protected void stopServer() {
        try {
            testServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the given test plan.
     * @param planPath
     * @param classLoader
     */
    protected void runTestPlan(String planPath, ClassLoader classLoader) {
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

    public void setup() throws Exception {
        String outputDir = System.getProperty("apiman.test.h2-output-dir");
        if (outputDir == null) {
            File targetClassesDir = new File("target/classes").getAbsoluteFile();
            if (!targetClassesDir.exists()) {
                targetClassesDir.mkdirs();
            }
            outputDir = targetClassesDir.toString();
        }

        System.out.println("------------------------------------------------");
        System.out.println("Setting H2 db output path: " + outputDir);
        System.out.println("------------------------------------------------");

        System.setProperty("apiman.test.h2-output-dir", outputDir);
        System.setProperty("apiman.test.admin-user-only", "true");
        setTestType();
    }

    /**
     * Sets the type of 'test' to run.
     */
    protected void setTestType() {
        ManagerTestUtils.setTestType(TestType.jpa);
    }

    public void create() {
        try {
            TestUtil.setProperty("apiman.suite.api-username", "admin");
            TestUtil.setProperty("apiman.suite.api-password", "admin");
            TestUtil.setProperty("apiman.suite.gateway-config-endpoint", "https://localhost:8443/apiman-gateway-api");
            TestUtil.setProperty("apiman.suite.gateway-config-username", "apimanager");
            TestUtil.setProperty("apiman.suite.gateway-config-password", "apiman123!");

            runTestPlan("scripts/api-manager-init-testPlan.xml", CreateH2Database.class.getClassLoader());
        } finally {
            System.clearProperty("apiman.test.admin-user-only");
            System.clearProperty("apiman.test.h2-output-dir");
        }
    }

}
