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

import io.apiman.common.es.util.ApimanEmbeddedElastic;
import io.apiman.common.es.util.ApimanEmbeddedElastic.Builder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

/**
 * Starts up an embedded elasticsearch cluster.  This is useful when running
 * apiman in ES storage mode.  This takes the place of a standalone
 * elasticsearch cluster installation.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class Bootstrapper implements ServletContextListener {

    private ApimanEmbeddedElastic node;

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DistroESConfig config = new DistroESConfig();

        File esHome = getDataDir();
        if (esHome == null) {
            System.out.println("--------------------------------------------------------------");
            System.err.println("No apiman-es data directory found.  Embedded ES *not* started.");
            System.out.println("--------------------------------------------------------------");
            return;
        }
        if (!esHome.exists()) {
            esHome.mkdirs();
        }

        System.out.println("------------------------------------------------------------");
        System.out.println("Starting apiman-es.");
        System.out.println("   HTTP Ports:      " + config.getHttpPortRange());
        System.out.println("   Transport Ports: " + config.getTransportPortRange());
        System.out.println("   Bind Host:       " + config.getBindHost());
        System.out.println("   ES Data Dir:     " + esHome);
        System.out.println("------------------------------------------------------------");

        String clusterName = "apiman";

        try {

            URL url = resolveEsDistro(sce);

            Builder builder = ApimanEmbeddedElastic.builder()
                .withPort(config.getHttpPortRange())
                .withDownloadUrl(url)
                .withCleanInstallationDirectoryOnStop(false)
                .withInstallationDirectory(esHome)
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, config.getTransportPortRange())
                .withSetting(PopularProperties.CLUSTER_NAME, clusterName)
                .withStartTimeout(1, TimeUnit.MINUTES);

            if (config.getBindHost() != null) {
                builder.withSetting("network.bind_host", config.getBindHost());
            }

            node = builder.build().start();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("-----------------------------");
        System.out.println("apiman-es started!");
        System.out.println("-----------------------------");
    }

    /**
     * @return the ES data directory
     */
    private static File getDataDir() {
        File esHome = null;

        // First check to see if a data directory has been explicitly configured via system property
        String dataDir = System.getProperty("apiman.distro-es.data_dir");
        if (dataDir != null) {
            esHome = new File(dataDir, "es");
        }

        // If that wasn't set, then check to see if we're running in wildfly/eap
        if (esHome == null) {
            dataDir = System.getProperty("jboss.server.data.dir");
            if (dataDir != null) {
                esHome = new File(dataDir, "es");
            }
        }

        // If that didn't work, try to locate a tomcat data directory
        if (esHome == null) {
            dataDir = System.getProperty("catalina.home");
            if (dataDir != null) {
                esHome = new File(dataDir, "data/es");
            }
        }

        // If all else fails, just let it return null

        return esHome;
    }

    private URL resolveEsDistro(ServletContextEvent sce) throws MalformedURLException {
        String systemProp = System.getProperty("apiman.es-distro");
        if (systemProp != null) {
            return new URL(systemProp);
        }

        URL url = Bootstrapper.class.getResource("embedded-elastic.properties");
        if (url == null) {
            throw new RuntimeException("embedded-elastic.properties missing.");
        } else {
            Properties allProperties = new Properties();
            try(InputStream is = url.openStream()){
                allProperties.load(is);
                String dPath = Optional
                        .ofNullable(allProperties.getProperty("apiman.es-distro"))
                        .orElseThrow(() -> new RuntimeException("apiman.es-distro not defined"));

                return sce.getServletContext().getResource(dPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (node != null) {
            try {
                node.stop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("-----------------------------");
            System.out.println("apiman-es stopped!");
            System.out.println("-----------------------------");
        }
    }

}
