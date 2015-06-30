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
package io.apiman.manager.test.junit;

import io.apiman.manager.api.core.util.PolicyTemplateUtil;
import io.apiman.manager.test.server.ManagerApiTestServer;
import io.apiman.manager.test.server.MockGatewayServlet;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.resttest.RestTest;
import io.apiman.test.common.util.TestPlanRunner;
import io.apiman.test.common.util.TestUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A junit test runner that fires up apiman and makes it ready for
 * use in the tests.  This runner also loads up the test plan from
 * the required {@link RestTestPlan} annotation.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class RestTester extends ParentRunner<TestType> {

    private static Logger logger = LoggerFactory.getLogger(TestPlanRunner.class);

    private static ManagerApiTestServer testServer = new ManagerApiTestServer();
    private static final boolean USE_PROXY = false;
    private static final int PROXY_PORT = 7071;

    private TestPlan testPlan;
    private String testPlanPath;
    private TestPlanRunner runner;

    private TestType gatewayLogTest = new TestType();
    private TestType publishPayloadTest = new TestType();

    /**
     * Constructor.
     */
    public RestTester(Class<?> testClass) throws InitializationError {
        super(testClass);
        testPlan = loadTestPlan(testClass);
        gatewayLogTest.setName("Assert Gateway Log");
        publishPayloadTest.setName("Assert Publishing Payloads");
    }

    /**
     * Loads a test plan.
     * @param testClass
     * @throws InitializationError
     */
    private TestPlan loadTestPlan(Class<?> testClass) throws InitializationError {
        RestTestPlan annotation = testClass.getAnnotation(RestTestPlan.class);
        if (annotation == null) {
            throw new InitializationError("Missing test annotation: @RestTestPlan");
        }
        testPlanPath = annotation.value();
        TestPlan plan = TestUtil.loadTestPlan(testPlanPath, testClass.getClassLoader());
        return plan;
    }

    /**
     * Called to setup the test.
     * @throws InitializationError
     */
    public static void setup() {
        if (!"true".equals(System.getProperty("apiman.junit.no-server", "false"))) {
            startServer();
        } else {
            System.out.println("**** APIMan Server suppressed - assuming running tests against a live server. ****");
        }
    }

    /**
     * Called at the end of the test.
     */
    public static void shutdown() {
        if (!"true".equals(System.getProperty("apiman.junit.no-server", "false"))) {
            stopServer();
        }
    }

    /**
     * @throws Exception
     */
    protected static void startServer() {
        try {
            testServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws Exception
     */
    protected static void stopServer() {
        try {
            testServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.junit.runners.ParentRunner#getChildren()
     */
    @Override
    protected List<TestType> getChildren() {
        List<TestType> children = new ArrayList<>();
        List<TestGroupType> groups = testPlan.getTestGroup();
        for (TestGroupType group : groups) {
            children.addAll(group.getTest());
        }

        RestTestGatewayLog annotation = getTestClass().getJavaClass().getAnnotation(RestTestGatewayLog.class);
        if (annotation != null) {
            gatewayLogTest.setValue(annotation.value());
            children.add(gatewayLogTest);
        }
        RestTestPublishPayload annotation2 = getTestClass().getJavaClass().getAnnotation(RestTestPublishPayload.class);
        if (annotation2 != null) {
            children.add(publishPayloadTest);
        }

        return children;
    }

    /**
     * @see org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(RunNotifier notifier) {
        setup();

        PolicyTemplateUtil.clearCache();
        MockGatewayServlet.reset();

        String baseApiUrl = "http://localhost:" + getTestServerPort() + getBaseApiContext();
        log("");
        log("-------------------------------------------------------------------------------");
        log("Executing Test Plan: " + testPlanPath);
        log("   Base API URL: " + baseApiUrl);
        log("-------------------------------------------------------------------------------");
        log("");
        runner = new TestPlanRunner(baseApiUrl);
        configureSystemProperties();

        try {
            super.run(notifier);
        } finally {
            shutdown();
        }

        log("");
        log("-------------------------------------------------------------------------------");
        log("Test Plan complete: " + testPlanPath);
        log("-------------------------------------------------------------------------------");
        log("");
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final TestType test, RunNotifier notifier) {
        log("-----------------------------------------------------------");
        log("Starting Test [{0}]", test.getName());
        log("-----------------------------------------------------------");
        Description description = describeChild(test);
        if (test == this.gatewayLogTest) {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    String actualGatewayLog = MockGatewayServlet.getRequestLog();
                    Assert.assertEquals(test.getValue(), actualGatewayLog);
                }
            }, description, notifier);
        } else if (test == this.publishPayloadTest) {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    RestTestPublishPayload annotation = getTestClass().getJavaClass().getAnnotation(RestTestPublishPayload.class);
                    String[] expectedPayloads = annotation.value();
                    int index = 0;
                    for (String expectedPayload : expectedPayloads) {
                        String actualPayload = MockGatewayServlet.getPayloads().get(index);
                        if (expectedPayload == null || "".equals(expectedPayload)) {
                            Assert.assertNull(actualPayload);
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode expected = mapper.readTree(expectedPayload);
                            JsonNode actual = mapper.readTree(actualPayload.trim());
                            RestTest mockRT = new RestTest();
                            runner.assertJson(mockRT, expected, actual);
                        }

                        index++;
                    }
                }
            }, description, notifier);
        } else {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    RestTest restTest = TestUtil.loadRestTest(test.getValue(), getTestClass().getJavaClass().getClassLoader());
                    runner.runTest(restTest);
                }
            }, description, notifier);
        }
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(TestType child) {
        return Description.createTestDescription(getTestClass().getJavaClass(), child.getName());
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
        TestUtil.setProperty("apiman.test.gateway.endpoint", "http://localhost:" + getTestServerPort() + "/mock-gateway");
        TestUtil.setProperty("apiman.test.gateway.username", "admin");
        TestUtil.setProperty("apiman.test.gateway.password", "admin");
        TestUtil.setProperty("apiman.manager.require-auto-granted-org", "false");

        // TODO reset all these properties back to their previous versions when the test is complete
        RestTestSystemProperties annotation = getTestClass().getJavaClass().getAnnotation(RestTestSystemProperties.class);
        if (annotation != null) {
            String[] strings = annotation.value();
            for (int idx = 0; idx < strings.length; idx += 2) {
                String pname = strings[idx];
                String pval = strings[idx+1];
                log("Setting system property \"{0}\" to \"{1}\".", pname, pval);
                TestUtil.setProperty(pname, pval);
            }
        }
    }

    /**
     * Logs a message.
     *
     * @param message
     * @param params
     */
    private void log(String message, Object... params) {
        String outmsg = MessageFormat.format(message, params);
        logger.info("    >> " + outmsg);
    }

}
