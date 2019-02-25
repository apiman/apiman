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
package io.apiman.gateway.test.junit.servlet;

import io.apiman.common.es.util.ApimanEmbeddedElastic;
import io.apiman.common.es.util.DefaultEsClientFactory;
import io.apiman.common.util.ddl.DdlParser;
import io.apiman.gateway.engine.GatewayConfigProperties;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.components.HttpClientComponentImpl;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.test.server.GatewayServer;
import io.apiman.gateway.test.server.TestMetrics;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.resttest.IGatewayTestServer;
import io.apiman.test.common.util.TestUtil;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.DeleteIndex;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;

import com.fasterxml.jackson.databind.JsonNode;

import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

/**
 * A servlet version of the gateway test server.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class ServletGatewayTestServer implements IGatewayTestServer {

    protected static final int ECHO_PORT = 7654;
    protected static final int GATEWAY_PORT = 6060;
    protected static final int GATEWAY_PROXY_PORT = 6061;
    protected static final boolean USE_PROXY = false; // if you set this to true you must start a tcp proxy on 8081

    private EchoServer echoServer = new EchoServer(ECHO_PORT);
    private GatewayServer gatewayServer = new GatewayServer(GATEWAY_PORT);

    private boolean withES;
    private boolean withDB;
    private boolean withISPN;

    /*
     * Elasticsearch related.
     */
    private static final String ES_CLUSTER_NAME = "_apimantest";
    private static final int JEST_TIMEOUT = 6000;
    public static JestClient ES_CLIENT = null;
    private JestClient client = null;
    private ApimanEmbeddedElastic node;

    /*
     * Database related.
     */
    private static final String DB_JNDI_LOC = "java:/comp/env/jdbc/ApiGatewayDS";
    private BasicDataSource ds = null;

    /*
     * Infinispan related.
     */
    private static final String ISPN_JNDI_LOC = "java:jboss/infinispan/apiman";
    private CacheContainer cacheContainer = null;

    /**
     * Constructor.
     */
    public ServletGatewayTestServer() {
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#configure(JsonNode)
     */
    @Override
    public void configure(JsonNode config) {
        JsonNode esNode = config.get("es");
        JsonNode dbNode = config.get("db");
        JsonNode ispnNode = config.get("ispn");

        withES = esNode != null && esNode.asBoolean(false);
        withDB = dbNode != null && dbNode.asBoolean(false);
        withISPN = ispnNode != null && ispnNode.asBoolean(false);

        configureGateway(config);
    }

    /**
     * Configures the gateway by settings system properties.
     */
    protected static void configureGateway(JsonNode config) {
        Map<String, String> props = new HashMap<>();

        // Global settings - all tests share but can override
        props.put(GatewayConfigProperties.PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
        props.put(GatewayConfigProperties.PLUGIN_REGISTRY_CLASS + ".pluginsDir", new File("target/plugintmp").getAbsolutePath());
        props.put(GatewayConfigProperties.CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        props.put(GatewayConfigProperties.POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
        props.put(GatewayConfigProperties.COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(), PolicyFailureFactoryComponent.class.getName());
        props.put(GatewayConfigProperties.COMPONENT_PREFIX + IHttpClientComponent.class.getSimpleName(), HttpClientComponentImpl.class.getName());
        props.put(GatewayConfigProperties.COMPONENT_PREFIX + IBufferFactoryComponent.class.getSimpleName(), ByteBufferFactoryComponent.class.getName());
        props.put(GatewayConfigProperties.METRICS_CLASS, TestMetrics.class.getName());

        // First, process the config files.
        if (config.has("config-files")) {
            JsonNode configFilesNode = config.get("config-files");
            for (JsonNode jsonNode : configFilesNode) {
                String configFile = jsonNode.asText();
                Properties loadedProps = loadConfigFile(configFile);
                for (Entry<Object, Object> entry : loadedProps.entrySet()) {
                    props.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }

        // Then layer on top of that, the properties defined in the config itself.
        if (config.has("config-properties")) {
            JsonNode configNode = config.get("config-properties");
            Iterator<String> fieldNames = configNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String value = configNode.get(fieldName).asText();
                props.put(fieldName, value);
            }
        }

        for (Entry<String, String> entry : props.entrySet()) {
            TestUtil.setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Loads the config file.
     * @param configFile
     */
    private static Properties loadConfigFile(String configFile) {
        // Try loading as a URL first.
        try {
            URL url = new URL(configFile);
            try (InputStream is = url.openStream()) {
                Properties props = new Properties();
                props.load(is);
                return props;
            }
        } catch (IOException e) {
            // Move on to the next type.
            System.out.println("Tried to load config file as a URL but failed: " + configFile);
        }

        // Now try loading as a resource.
        ClassLoader cl = ServletGatewayTestServer.class.getClassLoader();
        URL resource = cl.getResource(configFile);
        if (resource == null) {
            resource = cl.getResource("test-configs/" + configFile);
        }
        try {
            try (InputStream is = resource.openStream()) {
                Properties props = new Properties();
                props.load(is);
                return props;
            }
        } catch (Exception e) {
            // Move on to the next type.
            System.out.println("Tried to load config file as a resource but failed: " + configFile);
        }

        throw new RuntimeException("Failed to load referenced config: " + configFile);
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#getApiEndpoint()
     */
    @Override
    public String getApiEndpoint() {
        int port = GATEWAY_PORT;
        if (USE_PROXY) {
            port = GATEWAY_PROXY_PORT;
        }
        String baseApiUrl = "http://localhost:" + port + "/api";
        return baseApiUrl;
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#getGatewayEndpoint()
     */
    @Override
    public String getGatewayEndpoint() {
        int port = GATEWAY_PORT;
        if (USE_PROXY) {
            port = GATEWAY_PROXY_PORT;
        }
        String baseApiUrl = "http://localhost:" + port + "/gateway";
        return baseApiUrl;
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#getEchoTestEndpoint()
     */
    @Override
    public String getEchoTestEndpoint() {
        return "http://localhost:" + ECHO_PORT;
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#start()
     */
    @Override
    public void start() {
        try {
            preStart();
            echoServer.start();
            gatewayServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does some configuration before starting the server.
     * @throws IOException
     */
    private void preStart() throws IOException {
        if (withES) {
            try {
                File esDownloadCache = new File(System.getenv("HOME") + "/.cache/apiman/elasticsearch");
                esDownloadCache.getParentFile().mkdirs();

                node = ApimanEmbeddedElastic.builder()
                            .withPort(19250)
                            .withElasticVersion(ApimanEmbeddedElastic.getEsBuildVersion())
                            .withDownloadDirectory(esDownloadCache)
                            .withSetting(PopularProperties.CLUSTER_NAME, "apiman")
                            .withCleanInstallationDirectoryOnStop(true)
                            .withStartTimeout(1, TimeUnit.MINUTES)
                            .build()
                            .start();

                System.out.println("================ STARTED ES ================ ");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Copy from manager?
            String connectionUrl = "http://localhost:19250";
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(new HttpClientConfig.Builder(connectionUrl)
                    .multiThreaded(true)
                    .connTimeout(JEST_TIMEOUT)
                    .readTimeout(JEST_TIMEOUT)
                    .build());
            client = factory.getObject();
            ES_CLIENT = client;
        }

        if (withDB) {
            TestUtil.setProperty("apiman-gateway", DB_JNDI_LOC);
            try {
                InitialContext ctx = TestUtil.initialContext();
                TestUtil.ensureCtx(ctx, "java:/comp/env");
                TestUtil.ensureCtx(ctx, "java:/comp/env/jdbc");
                ds = createInMemoryDatasource();
                ctx.bind(DB_JNDI_LOC, ds);
                System.out.println("DataSource created and bound to JNDI: " + DB_JNDI_LOC);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (withISPN) {
            cacheContainer = new DefaultCacheManager();
            Cache<Object, Object> registryCache = cacheContainer.getCache("registry");
            if (registryCache == null) {
                throw new RuntimeException("Error with ISPN cache: 'registry'");
            }
            try {
                InitialContext ctx = TestUtil.initialContext();
                TestUtil.ensureCtx(ctx, "java:jboss");
                TestUtil.ensureCtx(ctx, "java:jboss/infinispan");
                ctx.bind(ISPN_JNDI_LOC, cacheContainer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates an in-memory datasource.
     * @throws SQLException
     */
    private static BasicDataSource createInMemoryDatasource() throws Exception {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(Driver.class.getName());
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        Connection connection = ds.getConnection();
        connection.setAutoCommit(true);
        initDB(connection);
        connection.close();
        return ds;
    }

    /**
     * Initialize the DB with the apiman gateway DDL.
     * @param connection
     */
    private static void initDB(Connection connection) throws Exception {
        ClassLoader cl = ServletGatewayTestServer.class.getClassLoader();
        URL resource = cl.getResource("ddls/apiman-gateway_h2.ddl");
        try (InputStream is = resource.openStream()) {
            DdlParser ddlParser = new DdlParser();
            List<String> statements = ddlParser.parse(is);
            for (String sql : statements){
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }
        }
    }

    /**
     * @see io.apiman.test.common.resttest.IGatewayTestServer#stop()
     */
    @Override
    public void stop() {
        try {
            gatewayServer.stop();
            echoServer.stop();
            postStop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Called after stopping the gateway.
     */
    private void postStop() throws Exception {
        if (client != null) {
            client.execute(new DeleteIndex.Builder("apiman_gateway").build());
            DefaultEsClientFactory.clearClientCache();
        }
        if (node != null) {
            System.out.println("======== STOPPING ES ========");
            node.stop();
        }
        if (ds != null) {
            try (Connection connection = ds.getConnection()) {
                connection.prepareStatement("DROP ALL OBJECTS").execute();
            }
            ds.close();
            ds = null;
            InitialContext ctx = TestUtil.initialContext();
            Context pctx = (Context) ctx.lookup("java:/comp/env/jdbc");
            pctx.unbind("ApiGatewayDS");
        }
        if (cacheContainer != null) {
            cacheContainer.stop();
            cacheContainer = null;
            InitialContext ctx = TestUtil.initialContext();
            Context pctx = (Context) ctx.lookup("java:jboss/infinispan");
            pctx.unbind("apiman");
        }
    }

    @Override
    public void next(String endpoint) {
    }

}
