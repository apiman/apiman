package io.apiman.gateway.vertx.integration.java;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import io.apiman.gateway.test.server.EchoServer;
import io.apiman.gateway.vertx.verticles.InitializerVerticle;
import io.apiman.test.common.util.TestPlanRunner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.testtools.TestVerticle;

@SuppressWarnings("nls")
public abstract class RestTestBase extends TestVerticle {

    protected static final int ECHO_PORT = 7654;
    protected static final int GATEWAY_PORT = 8200;
    protected static final int API_PORT = 8202;

    private EchoServer echoServer = new EchoServer(ECHO_PORT);
    private String deploymentId;
    private Logger logger;

    private abstract class DeploymentAsyncResultHandler implements AsyncResultHandler<String> {

        @Override
        public final void handle(AsyncResult<String> result) {
            if(result.succeeded()) {
                deploymentId = result.result();
            }

            handleDeployment(result);
        }

        public abstract void handleDeployment(AsyncResult<String> result);
    }

    private JsonObject getConfig() {

        URL url = this.getClass().getResource("/testConfig.json");
        byte[] bytes;

        try {
            bytes = Files.readAllBytes(Paths.get(url.toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new JsonObject(new String(bytes));
    }

    @Override
    public void start() {
        super.start();
        this.logger = container.logger();

        try {
            echoServer.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public void stop() {
        echoServer.stop();
    }

    public void before(JsonObject config, AsyncResultHandler<String> resultHandler) {
        container.deployVerticle(InitializerVerticle.class.getCanonicalName(),
                config,
                resultHandler);
    }

    public void after() {
        container.undeployVerticle(deploymentId, new Handler<AsyncResult<Void>>() {

            @Override
            public void handle(AsyncResult<Void> flag) {
                testComplete();
            }
        });
    }

    /**
     * Runs the given test plan.
     * @param planPath the plan path
     */
    public void runTestPlan(final String planPath) {

        // Start up the echo server
        before(getConfig(), new DeploymentAsyncResultHandler() {

            @Override
            public void handleDeployment(AsyncResult<String> result) {
                if(result.failed()) {
                    logger.error("Vert.x failed to deploy:");
                    logger.error(result.cause().getMessage());
                    result.cause().printStackTrace();
                    
                    throw new RuntimeException(result.cause());
                }
                
                assertTrue(result.succeeded());
                
                
                
                System.setProperty("apiman-gateway-test.endpoints.echo", getEchoEndpoint()); //$NON-NLS-1$

                runTestPlan(planPath, getClass().getClassLoader());

                logger.info("Finished running the test plan");

                after();
            }
        });
    }

    /**
     * Runs the given test plan.
     * @param planPath
     * @param classLoader
     */
    protected void runTestPlan(String planPath, ClassLoader classLoader) {
        String baseApiUrl = getApiEndpoint();
        TestPlanRunner runner = new TestPlanRunner(baseApiUrl);
        runner.runTestPlan(planPath, classLoader);
    }

    /**
     * @return the api endpoint
     */
    public static String getApiEndpoint() {
        return "http://localhost:" + GATEWAY_PORT; //$NON-NLS-1$
    }

    /**
     * @return the gateway endpoint
     */
    protected static String getGatewayEndpoint() {
        return "http://localhost:" + GATEWAY_PORT; //$NON-NLS-1$
    }

    /**
     * @return the echo server endpoint
     */
    public static String getEchoEndpoint() {
        return "http://localhost:" + ECHO_PORT; //$NON-NLS-1$
    }
}
