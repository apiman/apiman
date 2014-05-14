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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
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
public class EchoServer {
    
    private Server server;
    private ObjectMapper mapper = new ObjectMapper();
    private int port;

    /**
     * Constructor.
     */
    public EchoServer(int port) {
        this.port = port;
        mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
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
        ServletHolder servlet = new ServletHolder(new HttpServlet() {
            private static final long serialVersionUID = -5519107324541106467L;

            /**
             * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
             */
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                    IOException {
                doEchoResponse(req, resp);
            }
            
            /**
             * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
             */
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                    IOException {
                doEchoResponse(req, resp);
            }
            
            /**
             * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
             */
            @Override
            protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
                    IOException {
                doEchoResponse(req, resp);
            }
            
            /**
             * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
             */
            @Override
            protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
                doEchoResponse(req, resp);
            }
        });
        server.addServlet(servlet, "/"); //$NON-NLS-1$

        // Add the web contexts to jetty
        handlers.addHandler(server);
    }

    /**
     * Responds with a comprehensive echo.  This means bundling up all the
     * information about the inbound request into a java bean and responding
     * with that data as a JSON response.
     * @param exchange
     */
    protected void doEchoResponse(HttpServletRequest req, HttpServletResponse resp) {
        EchoResponse response = EchoResponse.from(req);
        
        resp.setContentType("application/json"); //$NON-NLS-1$
        try {
            mapper.writeValue(resp.getOutputStream(), response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
