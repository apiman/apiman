package io.apiman.osgi.pax.testing;

import com.squareup.okhttp.OkHttpClient;
import io.apiman.osgi.pax.testing.GatewayRestOSGITester.TestInfo;
import io.apiman.osgi.pax.testing.util.*;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.plan.TestGroupType;
import io.apiman.test.common.plan.TestPlan;
import io.apiman.test.common.plan.TestType;
import io.apiman.test.common.resttest.RestTest;
import io.apiman.test.common.util.TestPlanRunner;
import io.apiman.test.common.util.TestUtil;
import org.junit.Rule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.junit.PaxExamServer;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.ops4j.pax.logging.PaxContext;
import org.ops4j.pax.logging.slf4j.Slf4jLogger;
import org.ops4j.pax.web.service.spi.WebListener;
import org.osgi.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GatewayRestOSGITester extends ParentRunner<TestInfo> {

    private static Logger logger = LoggerFactory.getLogger(TestPlanRunner.class);

    private List<TestPlanInfo> testPlans = new ArrayList<>();
    private TestContainer testContainer;

    private static final String baseApiUrl = "https://localhost:8444/apiman-gateway-api/";
    protected static final int ECHO_PORT = 7654;
    private final String Plan_To_Test = "test-plans/api/api-testPlan.xml";
    private EchoServer echoServer = new EchoServer(ECHO_PORT);

    private Bundle warBundle;
    private ElasticSearchEmbed es;
    protected WebListener webListener;

    @Inject
    protected BundleContext bundleContext;

    private OkHttpClient httpClient;

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass
     */
    @Inject
    public GatewayRestOSGITester(Class<?> testClass) throws InitializationError {
        super(testClass);
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

    @Override
    protected Description describeChild(TestInfo child) {
        return Description.createTestDescription(getTestClass().getJavaClass(), child.name);
    }

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
                    RestTest restTest = TestUtil
                            .loadRestTest(testInfo.test.getValue(), getTestClass().getJavaClass().getClassLoader());
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
                        // endpoint = resolveEndpoint(endpoint);
                    }
                    if (endpoint == null) {
                        // endpoint = gatewayServer.getGatewayEndpoint();
                    }
                    testInfo.plan.runner.runTest(restTest, endpoint);
                }
            }
        }, description, notifier);
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

        System.setProperty("apiman-gateway-test.endpoints.echo", "" /*gatewayServer.getEchoTestEndpoint()*/);

        try {
            super.run(notifier);
        } finally {
            try {
                shutdown();
            } catch (Throwable e) { e.printStackTrace(); }
               // resetSystemProperties();
        }

        log("");
        log("-------------------------------------------------------------------------------");
        log("REST Test complete");
        log("-------------------------------------------------------------------------------");
        log("");
    }

    private void setup() {
        try {
            es = new ElasticSearchEmbed();
            es.launch();
            // Configure the okHTTPClient
            createHTTPClient();

            // Define the endpoint of the echo server
            System.setProperty("apiman-gateway-test.endpoints.echo", getEchoTestEndpoint());

            // Start Echo Server
            echoServer.start();

            // Start Karaf Container
            startServer();

            // Init OSGI HTTP Listener
            //bundleContext = FrameworkUtil.getBundle(Slf4jLogger.class).getBundleContext();
            initWebListener();
            final String bundlePath = "mvn:io.apiman/apiman-gateway-osgi-api/1.2.2-SNAPSHOT";
            warBundle = installAndStartBundle(bundlePath);
            waitForWebListener();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BundleContext getBundleContext(Class<?> klass, long timeout) throws Exception {
        try {
            BundleReference bundleRef = BundleReference.class.cast(klass.getClassLoader());
            Bundle bundle = bundleRef.getBundle();
            return getBundleContext(bundle, timeout);
        }
        catch (ClassCastException exc) {
            throw new Exception("class " + klass.getName() + " is not loaded from an OSGi bundle");
        }
    }

    /**
     * Retrieve bundle context from given bundle. If the bundle is being restarted the bundle
     * context can be null for some time
     *
     * @param bundle
     * @param timeout TODO
     * @return bundleContext or exception if bundleContext is null after timeout
     */
    private BundleContext getBundleContext(Bundle bundle, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        BundleContext bc = null;
        while (bc == null) {
            bc = bundle.getBundleContext();
            if (bc == null) {
                if (System.currentTimeMillis() >= endTime) {
                    throw new TestContainerException(
                            "Unable to retrieve bundle context from bundle " + bundle);
                }
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
        return bc;
    }

    private void startServer() {

        log("");
        log("-------------------------------------------------------------------------------");
        log("Starting Pax Exam OSGI Container ");
        log("-------------------------------------------------------------------------------");
        log("");

        try {
            Option[] options = KarafConfiguration.config();
            ExamSystem system = DefaultExamSystem.create(options);
            testContainer = PaxExamRuntime.createContainer(system);
            testContainer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        testContainer.stop();
    }

    /**
     * Create OK HTTP Client
     */
    public void createHTTPClient() throws KeyManagementException {

        httpClient = new OkHttpClient();
        httpClient.setFollowRedirects(false);
        httpClient.setFollowSslRedirects(false);

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                        String authType) throws CertificateException {
                }

                @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }
            } };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            httpClient.setSslSocketFactory(sslSocketFactory);
            httpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initWebListener() {
        webListener = new WebListenerImpl();
        bundleContext.registerService(WebListener.class, webListener, null);
    }

    protected void waitForWebListener() throws InterruptedException {
        new WaitCondition("webapp startup") {
            @Override
            protected boolean isFulfilled() {
                return ((WebListenerImpl)webListener).gotEvent();
            }
        }.waitForCondition();
    }

    protected Bundle installAndStartBundle(String bundlePath)
            throws BundleException, InterruptedException {
        final Bundle bundle = bundleContext.installBundle(bundlePath);
        bundle.start();
        new WaitCondition("bundle startup") {
            @Override
            protected boolean isFulfilled() {
                return bundle.getState() == Bundle.ACTIVE;
            }
        }.waitForCondition();
        return bundle;
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

    /**
     * GetEchoTestEndpoint()
     */
    public String getEchoTestEndpoint() {
        return "http://localhost:" + ECHO_PORT;
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
