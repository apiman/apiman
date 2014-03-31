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
package org.overlord.apiman.rt.test.gateway;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.overlord.apiman.rt.api.rest.impl.ApplicationResourceImpl;
import org.overlord.apiman.rt.api.rest.impl.RtApiApplication;
import org.overlord.apiman.rt.api.rest.impl.ServiceResourceImpl;
import org.overlord.apiman.rt.api.rest.impl.mappers.RestExceptionMapper;
import org.overlord.apiman.rt.war.listeners.GatewayBootstrapper;
import org.overlord.apiman.rt.war.servlets.GatewayServlet;

/**
 * Uses Undertow to run the WAR version of the API Management gateway.
 * 
 * @author eric.wittmann@redhat.com
 */
public class GatewayServer {

    private Undertow server;

    /**
     * Constructor.
     * @param port which port to run the test server on
     */
    public GatewayServer(int port) {
        try {
            DeploymentInfo servletBuilder = Servlets
                    .deployment()
                    .setClassLoader(GatewayServer.class.getClassLoader())
                    .setContextPath("/apiman-rt") //$NON-NLS-1$
                    .setDeploymentName("apiman-rt.war") //$NON-NLS-1$
                    .addListener(Servlets.listener(GatewayBootstrapper.class))
                    .addServlets(
                            Servlets.servlet("GatewayServlet", GatewayServlet.class).addMapping("/gateway/*")) //$NON-NLS-1$ //$NON-NLS-2$
                    .addServlets(
                            Servlets.servlet("ResteasyServlet", HttpServletDispatcher.class) //$NON-NLS-1$
                                    .addInitParam("javax.ws.rs.Application", GatewayApplication.class.getName()) //$NON-NLS-1$
                                    .setLoadOnStartup(1)
                                    .addMapping("/api/*")); //$NON-NLS-1$

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            server = Undertow.builder().addHttpListener(port, "localhost").setHandler(manager.start()).build(); //$NON-NLS-1$
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts the server.
     */
    public void start() {
        server.start();
    }

    /**
     * Stops the server.
     */
    public void stop() {
        server.stop();
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
            classes.add(ServiceResourceImpl.class);
            classes.add(ApplicationResourceImpl.class);
            classes.add(RestExceptionMapper.class);
            return classes;
        }
    }
}
