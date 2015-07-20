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

import io.apiman.test.common.mock.EchoServlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


/**
 * A very simple echo server used during testing as the back-end service
 * for all published managed services.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("javadoc")
public class EchoServer {
    
    private Server server;
    private int port;
    
    /**
     * Constructor.
     * 
     * @param port the port to listen on
     */
    public EchoServer(int port) {
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
         * Echo Server
         * ************* */
        ServletContextHandler server = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        server.setSecurityHandler(createSecurityHandler());
        server.setContextPath("/"); //$NON-NLS-1$
        ServletHolder servlet = new ServletHolder(new EchoServlet());
        server.addServlet(servlet, "/"); //$NON-NLS-1$

        // Add the web contexts to jetty
        handlers.addHandler(server);
    }
    
    public static void main(String [] args) throws Exception {
        new EchoServer(9999).start();
        while (Boolean.TRUE) {
            Thread.sleep(1000);
        }
    }

}
