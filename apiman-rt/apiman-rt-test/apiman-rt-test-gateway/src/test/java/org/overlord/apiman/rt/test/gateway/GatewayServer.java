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

import javax.servlet.ServletException;

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
     */
    public GatewayServer(int port) {
        try {
            DeploymentInfo servletBuilder = Servlets
                    .deployment()
                    .setClassLoader(GatewayServer.class.getClassLoader())
                    .setContextPath("/gateway")
                    .setDeploymentName("gateway.war")
                    .addListener(Servlets.listener(GatewayBootstrapper.class))
                    .addServlets(
                            Servlets.servlet("GatewayServlet", GatewayServlet.class).addMapping("/*"));

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            server = Undertow.builder().addHttpListener(port, "localhost").setHandler(manager.start()).build();
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

}
