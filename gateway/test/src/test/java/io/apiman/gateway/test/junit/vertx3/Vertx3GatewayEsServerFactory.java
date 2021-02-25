package io.apiman.gateway.test.junit.vertx3;

import com.fasterxml.jackson.databind.JsonNode;
import io.apiman.common.es.util.EsConstants;
import io.apiman.test.common.resttest.IGatewayTestServer;
import io.apiman.test.common.resttest.IGatewayTestServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * Starts and stops an Elasticsearch-based Vert.x 3 test server
 */
public class Vertx3GatewayEsServerFactory implements IGatewayTestServerFactory {

    private static final Logger logger = LoggerFactory.getLogger(Vertx3GatewayEsServerFactory.class);

    @Override
    public IGatewayTestServer createGatewayTestServer() {
        return new Vertx3EsServer();
    }

    private static final class Vertx3EsServer extends Vertx3GatewayTestServer {
        private final ElasticsearchContainer testContainer = new ElasticsearchContainer(
            "docker.elastic.co/elasticsearch/elasticsearch-oss:" + EsConstants.getEsVersion()
        );

        static final String TEST_CONTAINERS_PORT_ENV_VAR = "test.TEST_CONTAINERS_ES_PORT";

        // Start this early to ensure that we've injected the port into the environment before anything
        // interesting happens.
        {
            if (!testContainer.isRunning()) {
                logger.info("Starting testcontainer...");
                testContainer.start();
            }

            logger.info("Setting ES test-containers port sys property {} to: {} ",
                TEST_CONTAINERS_PORT_ENV_VAR, testContainer.getFirstMappedPort());

            // In conf-es.json this will be substituted at run time
            System.setProperty(
                TEST_CONTAINERS_PORT_ENV_VAR,
                String.valueOf(testContainer.getFirstMappedPort())
            );
        }

        public Vertx3EsServer() {
            super( false);
        }

        @Override
        public void configure(JsonNode config) {
            super.configure(config);
        }

        @Override
        public String getApiEndpoint() {
            return super.getApiEndpoint();
        }

        @Override
        public String getGatewayEndpoint() {
            return super.getGatewayEndpoint();
        }

        @Override
        public String getEchoTestEndpoint() {
            return super.getEchoTestEndpoint();
        }

        @Override
        public void start() {
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
            // We could stop the container here, but to avoid extremely slow tests we just flush the indices
            // please refer to ESResetter
        }

        @Override
        public void next(String endpoint) {
            super.next(endpoint);
        }
    }
}
