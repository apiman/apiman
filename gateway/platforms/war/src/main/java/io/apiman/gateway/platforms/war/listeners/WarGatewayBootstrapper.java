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
package io.apiman.gateway.platforms.war.listeners;

import io.apiman.gateway.platforms.war.WarGateway;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Used to bootstrap the API Management Gateway when it is running in a simple
 * web app container such as Jetty or Tomcat.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarGatewayBootstrapper implements ServletContextListener {

    /**
     * Constructor.
     */
    public WarGatewayBootstrapper() {
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        WarGateway.init();
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        WarGateway.shutdown();
    }

}
