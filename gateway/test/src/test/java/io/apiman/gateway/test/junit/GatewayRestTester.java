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
package io.apiman.gateway.test.junit;

import io.apiman.gateway.test.junit.GatewayRestTester.TestInfo;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.resttest.IGatewayTestServer;
import io.apiman.test.common.resttest.IGatewayTestServerFactory;
import io.apiman.test.common.resttest.RestTest;
import io.apiman.test.common.util.TestPlanRunner;
import io.apiman.test.common.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A junit test runner that fires up an API Gateway and makes it ready for use
 * in the tests. This runner also loads up the test plan from the required
 * {@link GatewayRestTestPlan} annotation.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class GatewayRestTester extends ParentRunner<TestInfo> {

    private static Logger logger = LoggerFactory.getLogger(TestPlanRunner.class);

    private static IGatewayTestServer gatewayServer;

    static {
        createAndConfigureGateway();
    }

    /**
     * Creates a gateway from a gateway test config file.
     */
    protected static void createAndConfigureGateway() {
        String testConfig = System.getProperty("apiman.gateway-test.config", null);
        if (testConfig == null) {
            testConfig = "default";
        }
        URL configUrl = GatewayRestTester.class.getClassLoader().getResource("test-configs/" + testConfig + ".json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode config = mapper.readTree(configUrl);
            String factoryFQN = config.get("factory").asText();
            IGatewayTestServerFactory factory = (IGatewayTestServerFactory) Class.forName(factoryFQN).newInstance();
            gatewayServer = factory.createGatewayTestServer();
            gatewayServer.configure(config);
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TestPlanInfo> testPlans = new ArrayList<>();
    private Set<String> resetSysProps = new HashSet<>();

    /**
     * Constructor.
     * @param testClass the test class
     * @throws InitializationError the initialziation error
     */
    public GatewayRestTester(Class<?> testClass) throws InitializationError {
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
            GatewayRestTestPlan annotation = testClass.getAnnotation(GatewayRestTestPlan.class);
            if (annotation == null) {
                throw new InitializationError("Missing @GatewayRestTestPlan annotation on test class.");
            } else {
                TestPlanInfo planInfo = new TestPlanInfo();
                planInfo.planPath = annotation.value();
                planInfo.name = new File(planInfo.planPath).getName();
                planInfo.plan = TestUtil.loadTestPlan(planInfo.planPath, testClass.getClassLoader());
                testPlans.add(planInfo);
            }
        } catch (Throwable e) {
            throw new InitializationError(e);
        }

        if (testPlans.isEmpty()) {
            throw new InitializationError("No @GatewayRestTestPlan annotations found on test class: " + testClass);
        }
    }

    /**
     * Called to setup the test.
     */
    public void setup() {
        startServer();
    }

    /**
     * Called at the end of the test.
     */
    public void shutdown() {
        stopServer();
    }

    /**
     * @throws Exception
     */
    protected static void startServer() {
        try {
            gatewayServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws Exception
     */
    protected static void stopServer() {
        try {
            gatewayServer.stop();
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

        for (TestPlanInfo planInfo : testPlans) {
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

        return children;
    }

    /**
     * @see org.junit.runners.ParentRunner#run(org.junit.runner.notification.RunNotifier)
     */
    @Override
    public void run(RunNotifier notifier) {
        setup();

        log("");
        log("-------------------------------------------------------------------------------");
        log("Executing REST Test");
        log("-------------------------------------------------------------------------------");
        log("");

        System.setProperty("apiman-gateway-test.endpoints.echo", gatewayServer.getEchoTestEndpoint());

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
        Description description = describeChild(testInfo);
        runLeaf(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String rtPath = testInfo.test.getValue();
                Integer delay = testInfo.test.getDelay();

                if (delay != null) {
                    try { Thread.sleep(delay); } catch (InterruptedException e) { }
                }
                if (rtPath != null && !rtPath.trim().isEmpty()) {
                    RestTest restTest = TestUtil.loadRestTest(testInfo.test.getValue(), getTestClass().getJavaClass().getClassLoader());
                    String endpoint = null;
                    if (endpoint == null) {
                        endpoint = testInfo.test.getEndpoint();
                    }
                    if (endpoint == null) {
                        endpoint = testInfo.group.getEndpoint();
                    }
                    if (endpoint == null) {
                        endpoint = testInfo.plan.plan.getEndpoint();
                    }
                    if (endpoint != null) {
                        endpoint = resolveEndpoint(endpoint);
                    }
                    if (endpoint == null) {
                        endpoint = gatewayServer.getGatewayEndpoint();
                    }
    
                    testInfo.plan.runner.runTest(restTest, endpoint);
                }
            }
        }, description, notifier);
    }

    /**
     * Resolves the logical endpoint into a real endpoint provided by the {@link IGatewayTestServer}.
     * @param endpoint
     */
    protected String resolveEndpoint(String endpoint) {
        if ("api".equals(endpoint)) {
            return gatewayServer.getApiEndpoint();
        } else if ("gateway".equals(endpoint)) {
            return gatewayServer.getGatewayEndpoint();
        } else {
            return TestUtil.doPropertyReplacement(endpoint);
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
        return System.getProperty("apiman.junit.gateway-context", "/");
    }

    /**
     * Configure some proeprties.
     */
    private void configureSystemProperties() {
        GatewayRestTestSystemProperties annotation = getTestClass().getJavaClass().getAnnotation(GatewayRestTestSystemProperties.class);
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
     * @param message the message
     * @param params the params
     */
    public void log(String message, Object... params) {
        String outmsg = MessageFormat.format(message, params);
        logger.info("    >> " + outmsg);
    }

    public static class TestPlanInfo {
        TestPlan plan;
        String name;
        String planPath;

        TestPlanRunner runner;
    }

    public static class TestInfo {
        TestGroupType group;
        TestType test;
        String name;
        TestPlanInfo plan;
    }
}
