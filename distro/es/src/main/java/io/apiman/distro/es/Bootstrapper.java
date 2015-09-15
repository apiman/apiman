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
package io.apiman.distro.es;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * Starts up an embedded elasticsearch cluster.  This is useful when running
 * apiman in ES storage mode.  This takes the place of a standalone
 * elasticsearch cluster installation.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class Bootstrapper implements ServletContextListener {

    private static Node node = null;

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DistroESConfig config = new DistroESConfig();


        System.out.println("------------------------------");
        System.out.println("Starting apiman-es.");
        System.out.println("   HTTP Ports: " + config.getHttpPortRange());
        System.out.println("   Transport Ports: " + config.getTransportPortRange());
        System.out.println("   Bind Host: " + config.getBindHost());
        System.out.println("------------------------------");
        String dataDir = System.getProperty("jboss.server.data.dir");
        if (dataDir == null) {
            System.err.println("\n\n-----Failed to find jboss.server.data.dir - are you trying to run apiman-es on an unsupported platform?\n-----\n\n");
            return;
        }
        File esHome = new File(dataDir, "es");
        System.out.println("ES Home: " + esHome);
        if (!esHome.exists()) {
            esHome.mkdirs();
        }
        Builder settings = NodeBuilder.nodeBuilder().settings();
        settings.put("path.home", esHome.getAbsolutePath());
        settings.put("http.port", config.getHttpPortRange());
        settings.put("transport.tcp.port", config.getTransportPortRange());
        settings.put("discovery.zen.ping.multicast.enabled", false);
        if (config.getBindHost() != null) {
            settings.put("network.bind_host", config.getBindHost());
        }

        String clusterName = "apiman";
        node = NodeBuilder.nodeBuilder().client(false).clusterName(clusterName).data(true).local(false).settings(settings).build();
        node.start();
        System.out.println("-----------------------------");
        System.out.println("apiman-es started!");
        System.out.println("-----------------------------");
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        node.stop();
        System.out.println("-----------------------------");
        System.out.println("apiman-es stopped!");
        System.out.println("-----------------------------");
    }

}
