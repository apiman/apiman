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
package org.overlord.apiman.rt.test.server;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.overlord.apiman.rt.api.rest.impl.ApplicationResourceImpl;
import org.overlord.apiman.rt.api.rest.impl.RtApiApplication;
import org.overlord.apiman.rt.api.rest.impl.ServiceResourceImpl;
import org.overlord.apiman.rt.api.rest.impl.SystemResourceImpl;
import org.overlord.apiman.rt.api.rest.impl.mappers.RestExceptionMapper;
import org.overlord.apiman.rt.war.listeners.GatewayBootstrapper;
import org.overlord.apiman.rt.war.servlets.GatewayServlet;

/**
 * Uses Jetty to run the WAR version of the API Management gateway.
 * 
 * @author eric.wittmann@redhat.com
 */
public class GatewayServer {

    private Server server;
    private int port;

    /**
     * Constructor.
     * @param port which port to run the test server on
     */
    public GatewayServer(int port) {
        this.port = port;
    }

    /**
     * Start/run the server.
     */
    public void start() throws Exception {
        long startTime = System.currentTimeMillis();
        System.out.println("**** Starting Server (" + getClass().getSimpleName() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        preStart();

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        addModulesToJetty(handlers);

        // Create the server.
        server = new Server(port);
        server.setHandler(handlers);
        server.start();
        long endTime = System.currentTimeMillis();
        System.out.println("******* Started in " + (endTime - startTime) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Does some configuration before starting the server.
     */
    private void preStart() {
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
        server.setContextPath("/"); //$NON-NLS-1$
        server.addEventListener(new GatewayBootstrapper());
        ServletHolder servlet = new ServletHolder(new GatewayServlet());
        server.addServlet(servlet, "/gateway/*"); //$NON-NLS-1$
        servlet = new ServletHolder(new HttpServletDispatcher());
        servlet.setInitParameter("javax.ws.rs.Application", GatewayApplication.class.getName()); //$NON-NLS-1$
        servlet.setInitOrder(1);
        server.addServlet(servlet, "/api/*"); //$NON-NLS-1$

        // Add the web contexts to jetty
        handlers.addHandler(server);
    }

    public static class GatewayApplication extends RtApiApplication {
        
        /**
         * Constructor.
         */
        public GatewayApplication() {
        }

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(SystemResourceImpl.class);
            classes.add(ServiceResourceImpl.class);
            classes.add(ApplicationResourceImpl.class);
            classes.add(RestExceptionMapper.class);
            return classes;
        }
    }
}
