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
package io.apiman.gateway.test.server;

import io.apiman.gateway.api.rest.impl.ApplicationResourceImpl;
import io.apiman.gateway.api.rest.impl.ServiceResourceImpl;
import io.apiman.gateway.api.rest.impl.SystemResourceImpl;
import io.apiman.gateway.api.rest.impl.mappers.RestExceptionMapper;
import io.apiman.gateway.engine.es.ESClientFactory;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import io.apiman.gateway.platforms.war.servlets.WarGatewayServlet;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

/**
 * Uses Jetty to run the WAR version of the API Management gateway.
 * 
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class GatewayServer {
    
    public static GatewayServer gatewayServer;
    private static final String ES_CLUSTER_NAME = "_apimantest";
    public static Client ES_CLIENT = null;

    private Server server;
    private int port;
    
    private Node node = null;
    private Client client = null;

    /**
     * Constructor.
     * @param port which port to run the test server on
     */
    public GatewayServer(int port) {
        this.port = port;
        gatewayServer = this;
    }
    
    /**
     * @return the server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Start/run the server.
     */
    public void start() throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("**** Starting Server (" + getClass().getSimpleName() + ")");
        preStart();

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        addModulesToJetty(handlers);

        // Create the server.
        server = new Server(port);
        server.setHandler(handlers);
        server.start();
        long endTime = System.currentTimeMillis();
        System.out.println("******* Started in " + (endTime - startTime) + "ms");
    }
    
    /**
     * Does some configuration before starting the server.
     * @throws IOException 
     */
    private void preStart() throws IOException {
        if (GatewayTestUtils.getTestType() == GatewayTestType.es && node == null) {
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

            if (!isPersistent) {
                Node node = NodeBuilder.nodeBuilder().client(true).loadConfigSettings(false)
                        .clusterName(ES_CLUSTER_NAME).local(true)
                        .settings(ImmutableSettings.settingsBuilder().build()).node().start();
                client = node.client();
            } else {
                TransportClient tc = new TransportClient(ImmutableSettings.settingsBuilder()
                        .put("cluster.name", clusterName).build());
                tc.addTransportAddress(new InetSocketTransportAddress("localhost", 6600));
                client = tc;
            }
            
            ES_CLIENT = client;
        }
    }

    /**
     * Stops the server.
     */
    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (node != null) {
            if ("true".equals(System.getProperty("apiman.test.es-delete-index", "true"))) {
                DeleteIndexRequest request = new DeleteIndexRequest("apiman_gateway");
                client.admin().indices().delete(request).actionGet();
                ESClientFactory.clearClientCache();
            }
        }
    }

    /**
     * Configure the web application(s).
     * @param handlers
     * @throws Exception
     */
    protected void addModulesToJetty(ContextHandlerCollection handlers) throws Exception {
        /* *************
         * Gateway
         * ************* */
        ServletContextHandler server = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setContextPath("/");
        server.addEventListener(new WarGatewayBootstrapper());
        ServletHolder servlet = new ServletHolder(new WarGatewayServlet());
        server.addServlet(servlet, "/gateway/*");
        servlet = new ServletHolder(new HttpServletDispatcher());
        servlet.setInitParameter("javax.ws.rs.Application", TestGatewayApplication.class.getName());
        servlet.setInitParameter("resteasy.servlet.mapping.prefix", "/api");
        servlet.setInitOrder(1);
        server.addServlet(servlet, "/api/*");

        // Add the web contexts to jetty
        handlers.addHandler(server);
    }

    @ApplicationPath("/")
    public static class TestGatewayApplication extends Application {
        
        /**
         * Constructor.
         */
        public TestGatewayApplication() {
        }

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<>();
            classes.add(SystemResourceImpl.class);
            classes.add(ServiceResourceImpl.class);
            classes.add(ApplicationResourceImpl.class);
            classes.add(RestExceptionMapper.class);
            return classes;
        }
    }
}
