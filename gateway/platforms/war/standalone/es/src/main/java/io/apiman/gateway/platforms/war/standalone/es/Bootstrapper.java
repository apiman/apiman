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
package io.apiman.gateway.platforms.war.standalone.es;

import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;

/**
 * Starts up an embedded elasticsearch cluster.  This is useful when running
 * apiman in ES storage mode.  This takes the place of a standalone
 * elasticsearch cluster installation.
 *
 * @author eric.wittmann@redhat.com
 * @author pcornish
 */
@SuppressWarnings("nls")
public class Bootstrapper implements ServletContextListener {

    private static Node node = null;

    /**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        StandaloneESConfig config = new StandaloneESConfig();


        System.out.println("------------------------------");
        System.out.println("Starting apiman-es.");
        System.out.println("   HTTP Ports: " + config.getHttpPortRange());
        System.out.println("   Transport Ports: " + config.getTransportPortRange());
        System.out.println("   Bind Host: " + config.getBindHost());
        System.out.println("------------------------------");

        File esHome = getDataDir();
        if (esHome == null) return;

        Builder settings = NodeBuilder.nodeBuilder().settings();
        settings.put("path.home", esHome.getAbsolutePath());
        settings.put("http.port", config.getHttpPortRange());
        settings.put("transport.tcp.port", config.getTransportPortRange());
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
     * @return a File pointing to the ES data directory
     */
    private File getDataDir() {
        String dataDir = System.getProperty("apiman.distro-es.data_dir");
        if (dataDir == null) {
            // use a temporary directory for ES storage
            final File tempFile;
            try {
                tempFile = File.createTempFile("apiman-es", "data");

                tempFile.delete();
                tempFile.mkdir();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            dataDir = tempFile.getAbsolutePath();
        }

        File esHome = new File(dataDir, "es");
        System.out.println("ES Home: " + esHome);
        if (!esHome.exists()) {
            esHome.mkdirs();
        }
        return esHome;
    }

    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        node.stop();
        System.out.println("-----------------------------");
        System.out.println("apiman-es stopped!");
        System.out.println("-----------------------------");
    }

}
