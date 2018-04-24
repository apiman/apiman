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
import io.apiman.manager.test.junit.ManagerRestTester.TestInfo;
import io.apiman.manager.test.server.ManagerApiTestServer;
import io.apiman.manager.test.server.MockGatewayServlet;
import io.apiman.test.common.json.JsonCompare;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.resttest.RestTest;
import io.apiman.test.common.util.TestPlanRunner;
import io.apiman.test.common.util.TestUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A junit test runner that fires up apiman and makes it ready for
 * use in the tests.  This runner also loads up the test plan from
 * the required {@link ManagerRestTestPlan} annotation.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class ManagerRestTester extends ParentRunner<TestInfo> {

    private static Logger logger = LoggerFactory.getLogger(TestPlanRunner.class);

    private static ManagerApiTestServer testServer = new ManagerApiTestServer();
    private static final boolean USE_PROXY = false;
    private static final int PROXY_PORT = 7071;

    private List<TestPlanInfo> testPlans = new ArrayList<>();
    private Set<String> resetSysProps = new HashSet<>();

    /**
     * Constructor.
     */
    public ManagerRestTester(Class<?> testClass) throws InitializationError {
        super(testClass);
        configureSystemProperties();
        loadTestPlans(testClass);
    }

    /**
     * Loads the test plans.
     * @param testClass
     * @throws InitializationError
     */
    private void loadTestPlans(Class<?> testClass) throws InitializationError {
        try {
            ManagerRestTestPlan annotation = testClass.getAnnotation(ManagerRestTestPlan.class);
            if (annotation == null) {
                Method[] methods = testClass.getMethods();
                TreeSet<ManagerRestTestPlan> annotations = new TreeSet<>(new Comparator<ManagerRestTestPlan>() {
                    @Override
                    public int compare(ManagerRestTestPlan o1, ManagerRestTestPlan o2) {
                        Integer i1 = o1.order();
                        Integer i2 = o2.order();
                        return i1.compareTo(i2);
                    }
                });
                for (Method method : methods) {
                    annotation = method.getAnnotation(ManagerRestTestPlan.class);
                    if (annotation != null) {
                        annotations.add(annotation);
                    }
                }
                for (ManagerRestTestPlan anno : annotations) {
                    TestPlanInfo planInfo = new TestPlanInfo();
                    planInfo.planPath = anno.value();
                    planInfo.name = new File(planInfo.planPath).getName();
                    planInfo.endpoint = TestUtil.doPropertyReplacement(anno.endpoint());
                    planInfo.plan = TestUtil.loadTestPlan(planInfo.planPath, testClass.getClassLoader());
                    testPlans.add(planInfo);
                }
            } else {
                TestPlanInfo planInfo = new TestPlanInfo();
                planInfo.planPath = annotation.value();
                planInfo.name = new File(planInfo.planPath).getName();
                planInfo.plan = TestUtil.loadTestPlan(planInfo.planPath, testClass.getClassLoader());
                planInfo.endpoint = TestUtil.doPropertyReplacement(annotation.endpoint());
                testPlans.add(planInfo);
            }
        } catch (Throwable e) {
            throw new InitializationError(e);
        }

        if (testPlans.isEmpty()) {
            throw new InitializationError("No @ManagerRestTestPlan annotations found on test class: " + testClass);
        }
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
    protected List<TestInfo> getChildren() {
        List<TestInfo> children = new ArrayList<>();

        TestPlanInfo lastPlan = null;
        for (TestPlanInfo planInfo : testPlans) {
            lastPlan = planInfo;
            planInfo.runner = new TestPlanRunner();

            List<TestGroupType> groups = planInfo.plan.getTestGroup();
            for (TestGroupType group : groups) {
                for (TestType test : group.getTest()) {
                    TestInfo testInfo = new TestInfo();
                    if (testPlans.size() > 1) {
                        testInfo.name = planInfo.name + " / " + test.getName();
                    } else {
                        testInfo.name = test.getName();
                    }
                    testInfo.plan = planInfo;
                    testInfo.group = group;
                    testInfo.test = test;
                    children.add(testInfo);
                }
            }
        }

        ManagerRestTestGatewayLog annotation = getTestClass().getJavaClass().getAnnotation(ManagerRestTestGatewayLog.class);
        if (annotation != null) {
            GatewayAssertionTestInfo gatewayTest = new GatewayAssertionTestInfo();
            gatewayTest.name = "Assert Gateway Log";
            gatewayTest.plan = lastPlan;
            gatewayTest.expectedLog = annotation.value();
            children.add(gatewayTest);
        }
        ManagerRestTestPublishPayload annotation2 = getTestClass().getJavaClass().getAnnotation(ManagerRestTestPublishPayload.class);
        if (annotation2 != null) {
            PublishPayloadTestInfo pubTest = new PublishPayloadTestInfo();
            pubTest.name = "Assert Publishing Payloads";
            pubTest.plan = lastPlan;
            pubTest.expectedPayloads = annotation2.value();
            children.add(pubTest);
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

        log("");
        log("-------------------------------------------------------------------------------");
        log("Executing REST Test");
        log("-------------------------------------------------------------------------------");
        log("");

        try {
            super.run(notifier);
        } finally {
            try { shutdown(); } catch (Throwable e) { e.printStackTrace(); }
            resetSystemProperties();
        }

        log("");
        log("-------------------------------------------------------------------------------");
        log("REST Test complete");
        log("-------------------------------------------------------------------------------");
        log("");
    }

    /**
     * @see org.junit.runners.ParentRunner#runChild(java.lang.Object, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(final TestInfo testInfo, RunNotifier notifier) {
        log("-----------------------------------------------------------");
        log("Starting Test [{0} / {1}]", testInfo.plan.name, testInfo.name);
        log("-----------------------------------------------------------");

            System.out.println("sleeping2");
            try { Thread.sleep(250); } catch (InterruptedException e) { }



        Description description = describeChild(testInfo);
        if (testInfo instanceof GatewayAssertionTestInfo) {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    String actualGatewayLog = MockGatewayServlet.getRequestLog();
                    Assert.assertEquals(((GatewayAssertionTestInfo) testInfo).expectedLog, actualGatewayLog);
                }
            }, description, notifier);
        } else if (testInfo instanceof PublishPayloadTestInfo) {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    String[] expectedPayloads = ((PublishPayloadTestInfo) testInfo).expectedPayloads;
                    int index = 0;
                    for (String expectedPayload : expectedPayloads) {
                        if (MockGatewayServlet.getPayloads().isEmpty()) {
                            Assert.fail("Expected a payload but did not find one.");
                        }
                        String actualPayload = MockGatewayServlet.getPayloads().get(index);
                        if (expectedPayload == null || "".equals(expectedPayload)) {
                            Assert.assertNull(actualPayload);
                        } else {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode expected = mapper.readTree(expectedPayload);
                            JsonNode actual = mapper.readTree(actualPayload.trim());
                            JsonCompare jsonCompare = new JsonCompare();
                            jsonCompare.assertJson(expected, actual);
                        }

                        index++;
                    }
                }
            }, description, notifier);
        } else {
            runLeaf(new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    String rtPath = testInfo.test.getValue();
                    Integer delay = testInfo.test.getDelay();

                    if (delay != null) {
                        System.out.println("sleeping");
                        try { Thread.sleep(1000); } catch (InterruptedException e) { }
                    }
                    if (rtPath != null && !rtPath.trim().isEmpty()) {
                        RestTest restTest = TestUtil.loadRestTest(rtPath, getTestClass().getJavaClass().getClassLoader());
                        String endpoint = testInfo.plan.endpoint;
                        if (StringUtils.isEmpty(endpoint)) {
                            endpoint = TestUtil.doPropertyReplacement(testInfo.test.getEndpoint());
                        }
                        if (StringUtils.isEmpty(endpoint)) {
                            endpoint = TestUtil.doPropertyReplacement(testInfo.group.getEndpoint());
                        }
                        if (StringUtils.isEmpty(endpoint)) {
                            endpoint = TestUtil.doPropertyReplacement(testInfo.plan.plan.getEndpoint());
                        }
                        if (StringUtils.isEmpty(endpoint)) {
                            endpoint = "http://localhost:" + getTestServerPort() + getBaseApiContext();
                        }
                        testInfo.plan.runner.runTest(restTest, endpoint);
                    }
                }
            }, description, notifier);
        }
    }

    /**
     * @see org.junit.runners.ParentRunner#describeChild(java.lang.Object)
     */
    @Override
    protected Description describeChild(TestInfo child) {
        return Description.createTestDescription(getTestClass().getJavaClass(), child.name);
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

        RestTestSystemProperties annotation = getTestClass().getJavaClass().getAnnotation(RestTestSystemProperties.class);
        if (annotation != null) {
            String[] strings = annotation.value();
            for (int idx = 0; idx < strings.length; idx += 2) {
                String pname = strings[idx];
                String pval = strings[idx+1];
                log("Setting system property \"{0}\" to \"{1}\".", pname, pval);
                if (System.getProperty(pname) == null) {
                    resetSysProps.add(pname);
                }
                TestUtil.setProperty(pname, pval);
            }
        }
    }

    /**
     * Resets the system properties that were set at the start of the test.
     */
    private void resetSystemProperties() {
        for (String propName : resetSysProps) {
            System.clearProperty(propName);
        }
        resetSysProps.clear();
    }

    /**
     * Logs a message.
     *
     * @param message
     * @param params
     */
    public void log(String message, Object... params) {
        String outmsg = MessageFormat.format(message, params);
        logger.info("    >> " + outmsg);
    }

    public static class TestPlanInfo {
        TestPlan plan;
        String name;
        String planPath;
        String endpoint;

        TestPlanRunner runner;
    }

    public static class TestInfo {
        TestGroupType group;
        TestType test;
        String name;
        TestPlanInfo plan;
    }

    public static class GatewayAssertionTestInfo extends TestInfo {
        String expectedLog;
    }

    public static class PublishPayloadTestInfo extends TestInfo {
        String[] expectedPayloads;
    }
}
