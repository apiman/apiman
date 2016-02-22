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

import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.components.IHttpClientComponent;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.es.ESClientFactory;
import io.apiman.gateway.engine.impl.ByteBufferFactoryComponent;
import io.apiman.gateway.engine.impl.DefaultPluginRegistry;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.platforms.servlet.PolicyFailureFactoryComponent;
import io.apiman.gateway.platforms.servlet.components.HttpClientComponentImpl;
import io.apiman.gateway.platforms.servlet.connectors.HttpConnectorFactory;
import io.apiman.gateway.platforms.war.WarEngineConfig;
import io.apiman.gateway.test.server.GatewayServer;
import io.apiman.gateway.test.server.TestMetrics;
import io.apiman.test.common.echo.EchoServer;
import io.apiman.test.common.resttest.IGatewayTestServer;
import io.apiman.test.common.util.TestUtil;
import io.apiman.tools.ddl.DdlParser;
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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import com.fasterxml.jackson.databind.JsonNode;

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

    /*
     * Elasticsearch related.
     */
    private static final String ES_CLUSTER_NAME = "_apimantest";
    private static final int JEST_TIMEOUT = 6000;
    public static JestClient ES_CLIENT = null;
    private Node node = null;
    private JestClient client = null;
    
    /*
     * Database related.
     */
    private static final String DB_JNDI_LOC = "java:/comp/env/jdbc/ApiGatewayDS";
    private BasicDataSource ds = null;

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
        
        withES = esNode != null && esNode.asBoolean(false);
        withDB = dbNode != null && dbNode.asBoolean(false);
        
        configureGateway(config);
    }

    /**
     * Configures the gateway by settings system properties.
     */
    protected static void configureGateway(JsonNode config) {
        Map<String, String> props = new HashMap<>();
        
        // Global settings - all tests share but can override
        props.put(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS, DefaultPluginRegistry.class.getName());
        props.put(WarEngineConfig.APIMAN_GATEWAY_PLUGIN_REGISTRY_CLASS + ".pluginsDir", new File("target/plugintmp").getAbsolutePath());
        props.put(WarEngineConfig.APIMAN_GATEWAY_CONNECTOR_FACTORY_CLASS, HttpConnectorFactory.class.getName());
        props.put(WarEngineConfig.APIMAN_GATEWAY_POLICY_FACTORY_CLASS, PolicyFactoryImpl.class.getName());
        props.put(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IPolicyFailureFactoryComponent.class.getSimpleName(), PolicyFailureFactoryComponent.class.getName());
        props.put(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IHttpClientComponent.class.getSimpleName(), HttpClientComponentImpl.class.getName());
        props.put(WarEngineConfig.APIMAN_GATEWAY_COMPONENT_PREFIX + IBufferFactoryComponent.class.getSimpleName(), ByteBufferFactoryComponent.class.getName());
        props.put(WarEngineConfig.APIMAN_GATEWAY_METRICS_CLASS, TestMetrics.class.getName());

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
        if (withES && node == null) {
            System.out.println("******* Creating the ES node for gateway testing.");
            File esHome = new File("target/es");
            String esHomeSP = System.getProperty("apiman.test.es-home", null);
            if (esHomeSP != null) {
                esHome = new File(esHomeSP);
            }
            if (esHome.isDirectory()) {
                FileUtils.deleteDirectory(esHome);
            }
            Builder settings = NodeBuilder.nodeBuilder().settings();
            settings.put("path.home", esHome.getAbsolutePath());
            settings.put("http.port", "6500-6600");
            settings.put("transport.tcp.port", "6600-6700");
            settings.put("script.disable_dynamic", "false");

            String clusterName = System.getProperty("apiman.test.es-cluster-name", ES_CLUSTER_NAME);

            boolean isPersistent = "true".equals(System.getProperty("apiman.test.es-persistence", "false"));
            if (!isPersistent) {
                settings.put("index.store.type", "memory").put("gateway.type", "none")
                        .put("index.number_of_shards", 1).put("index.number_of_replicas", 1);
                node = NodeBuilder.nodeBuilder().client(false).clusterName(clusterName).data(true).local(true)
                        .settings(settings).build();
            } else {
                node = NodeBuilder.nodeBuilder().client(false).clusterName(clusterName).data(true).local(false)
                        .settings(settings).build();
            }

            System.out.println("Starting the ES node.");
            node.start();
            System.out.println("ES node was successfully started.");
            String connectionUrl = "http://localhost:6500";
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(new HttpClientConfig.Builder(connectionUrl).multiThreaded(true)
                    .connTimeout(JEST_TIMEOUT).readTimeout(JEST_TIMEOUT).build());
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
        if (node != null) {
            client.execute(new DeleteIndex.Builder("apiman_gateway").build());
            ESClientFactory.clearClientCache();
        }
        if (ds != null) {
            try (Connection connection = ds.getConnection()) {
                connection.prepareStatement("DROP ALL OBJECTS").execute();
            }
            ds.close();
            InitialContext ctx = TestUtil.initialContext();
            Context pctx = (Context) ctx.lookup("java:/comp/env/jdbc");
            pctx.unbind("ApiGatewayDS");
        }
    }

}
