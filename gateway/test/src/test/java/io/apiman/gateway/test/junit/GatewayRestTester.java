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

import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.ICacheStoreComponent;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.components.ISharedStateComponent;
import io.apiman.gateway.engine.es.ESRateLimiterComponent;
import io.apiman.gateway.engine.es.ESRegistry;
import io.apiman.gateway.engine.es.ESSharedStateComponent;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.impl.InMemoryCacheStoreComponent;
import io.apiman.gateway.engine.impl.InMemoryRateLimiterComponent;
import io.apiman.gateway.engine.impl.InMemoryRegistry;
import io.apiman.gateway.engine.impl.InMemorySharedStateComponent;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.components.HttpClientComponentImpl;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.WarEngineConfig;
import io.apiman.gateway.test.junit.GatewayRestTester.TestInfo;
import io.apiman.gateway.test.server.EchoServer;
import io.apiman.gateway.test.server.GatewayServer;
import io.apiman.gateway.test.server.GatewayTestType;
import io.apiman.gateway.test.server.GatewayTestUtils;
import io.apiman.gateway.test.server.TestMetrics;
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

    protected static final int ECHO_PORT = 7654;
    protected static final int GATEWAY_PORT = 8080;
    protected static final int GATEWAY_PROXY_PORT = 8081;
    protected static final boolean USE_PROXY = false; // if you set this to true you must start a tcp proxy on 8081

    private static EchoServer echoServer = new EchoServer(ECHO_PORT);
    private static GatewayServer gatewayServer = new GatewayServer(GATEWAY_PORT);

    private List<TestPlanInfo> testPlans = new ArrayList<>();
    private Set<String> resetSysProps = new HashSet<>();

    /**
     * Constructor.
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
                Method[] methods = testClass.getMethods();
                TreeSet<GatewayRestTestPlan> annotations = new TreeSet<>(new Comparator<GatewayRestTestPlan>() {
                    @Override
                    public int compare(GatewayRestTestPlan o1, GatewayRestTestPlan o2) {
                        Integer i1 = o1.order();
                        Integer i2 = o2.order();
                        return i1.compareTo(i2);
                    }
                });
                for (Method method : methods) {
                    annotation = method.getAnnotation(GatewayRestTestPlan.class);
                    if (annotation != null) {
                        annotations.add(annotation);
                    }
                }
                for (GatewayRestTestPlan anno : annotations) {
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
            throw new InitializationError("No @GatewayRestTestPlan annotations found on test class: " + testClass);
        }
    }

    /**
     * Called to setup the test.
     * @throws InitializationError
     */
    public static void setup() {
        if (!"true".equals(System.getProperty("apiman.junit.no-server", "false"))) {
            configureGateway();
            startServer();
        } else {
            System.out.println("**** API Gateway Server suppressed - assuming running tests against a live server. ****");
        }
    }

    /**
     * Configures the gateway by settings system properties.
     */
    protected static void configureGateway() {
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS + ".pluginsDir", new File("target/plugintmp").getAbsolutePath());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(),
                PolicyFailureFactoryComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IHttpClientComponent.class.getSimpleName(),
                HttpClientComponentImpl.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IBufferFactoryComponent.class.getSimpleName(),
                ByteBufferFactoryComponent.class.getName());
        System.setProperty(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS, TestMetrics.class.getName());

        if (GatewayTestUtils.getTestType() == GatewayTestType.memory) {
            // Configure to run with in-memory components
            /////////////////////////////////////////////
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS, InMemoryRegistry.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(),
                    InMemorySharedStateComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName(),
                    InMemoryRateLimiterComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ICacheStoreComponent.class.getSimpleName(),
                    InMemoryCacheStoreComponent.class.getName());
        } else if (GatewayTestUtils.getTestType() == GatewayTestType.es) {
            // Configure to run with elasticsearch components
            /////////////////////////////////////////////////
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS, ESRegistry.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS + ".client.type", "local");
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS + ".client.class", GatewayServer.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_REGISTRY_CLASS + ".client.field", "ES_CLIENT");

            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName(),
                    ESSharedStateComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName() + ".client.type",
                    "local");
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName() + ".client.class",
                    GatewayServer.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + ISharedStateComponent.class.getSimpleName() + ".client.field",
                    "ES_CLIENT");

            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName(),
                    ESRateLimiterComponent.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName() + ".client.type",
                    "local");
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName() + ".client.class",
                    GatewayServer.class.getName());
            System.setProperty(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IRateLimiterComponent.class.getSimpleName() + ".client.field",
                    "ES_CLIENT");
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
            echoServer.start();
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
            echoServer.stop();
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
            if (planInfo.endpoint != null && planInfo.endpoint.length() > 0) {
                log("Test Endpoint: {0}", planInfo.endpoint);
                planInfo.runner = new TestPlanRunner(planInfo.endpoint);
            } else {
                String baseApiUrl = "http://localhost:" + getTestServerPort() + getBaseApiContext();
                log("Test Endpoint: {0}", baseApiUrl);
                planInfo.runner = new TestPlanRunner(baseApiUrl);
            }

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

        System.setProperty("apiman-gateway-test.endpoints.echo", getEchoEndpoint());

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
                RestTest restTest = TestUtil.loadRestTest(testInfo.test.getValue(), getTestClass().getJavaClass().getClassLoader());
                testInfo.plan.runner.runTest(restTest);
            }
        }, description, notifier);
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
     * @return the port to use when sending requests
     */
    protected int getTestServerPort() {
        String spPort = System.getProperty("apiman.junit.server-port");
        if (spPort != null) {
            return Integer.parseInt(spPort);
        }
        if (USE_PROXY) {
            return GATEWAY_PROXY_PORT;
        } else {
            return GATEWAY_PORT;
        }
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
        TestType test;
        String name;
        TestPlanInfo plan;
    }

    /**
     * @return the echo server endpoint
     */
    protected String getEchoEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }
}
