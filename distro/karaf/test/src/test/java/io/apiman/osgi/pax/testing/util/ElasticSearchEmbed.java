package io.apiman.osgi.pax.testing.util;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import java.io.File;
import java.io.IOException;

public class ElasticSearchEmbed {

    private static final String ES_CLUSTER_NAME = "_apimantest";

    private Node node = null;

    public ElasticSearchEmbed() {}

    public void launch() throws IOException {
        System.out.println("******* Creating the ES node for gateway testing.");
        File esHome = new File("target/es");
        String esHomeSP = System.getProperty("apiman.test.es-home", null);
        if (esHomeSP != null) {
            esHome = new File(esHomeSP);
        }
        if (esHome.isDirectory()) {
            FileUtils.deleteDirectory(esHome);
        }
        ImmutableSettings.Builder settings = NodeBuilder.nodeBuilder().settings();
        settings.classLoader(ImmutableSettings.class.getClassLoader());
        settings.put("path.home", esHome.getAbsolutePath());
        settings.put("http.port", "6500-6600");
        settings.put("transport.tcp.port", "6600-6700");
        settings.put("script.disable_dynamic", "false");

        String clusterName = System.getProperty("apiman.test.es-cluster-name", ES_CLUSTER_NAME);

        boolean isPersistent = "true".equals(System.getProperty("apiman.test.es-persistence", "false"));
        if (!isPersistent) {
            settings.put("index.store.type", "memory").put("gateway.type", "none")
                    .put("index.number_of_shards", 1).put("index.number_of_replicas", 1);
            node = NodeBuilder.nodeBuilder().client(false).clusterName(clusterName).data(true).local(true)
                    .settings(settings).build();
        } else {
            node = NodeBuilder.nodeBuilder().client(false).clusterName(clusterName).data(true).local(false)
                    .settings(settings).build();
        }

        System.out.println("Starting the ES node.");
        node.start();
        System.out.println("ES node was successfully started.");
    }


}
