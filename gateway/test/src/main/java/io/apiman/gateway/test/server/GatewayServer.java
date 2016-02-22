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

import io.apiman.gateway.api.rest.impl.ApiResourceImpl;
import io.apiman.gateway.api.rest.impl.ClientResourceImpl;
import io.apiman.gateway.api.rest.impl.SystemResourceImpl;
import io.apiman.gateway.api.rest.impl.mappers.RestExceptionMapper;
import io.apiman.gateway.platforms.war.listeners.WarGatewayBootstrapper;
import io.apiman.gateway.platforms.war.servlets.WarGatewayServlet;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

/**
 * Uses Jetty to run the WAR version of the API Management gateway.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({"nls", "javadoc"})
public class GatewayServer {

    public static GatewayServer gatewayServer;

    private Server server;
    private int port;

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
     * Stops the server.
     */
    public void stop() throws Exception {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
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
            classes.add(ApiResourceImpl.class);
            classes.add(ClientResourceImpl.class);
            classes.add(RestExceptionMapper.class);
            return classes;
        }
    }
}
