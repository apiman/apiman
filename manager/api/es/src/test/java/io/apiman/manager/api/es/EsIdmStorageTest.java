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
package io.apiman.manager.api.es;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A unit test.
 * 
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class EsIdmStorageTest {
    
    private static final String ES_CLUSTER_NAME = "_apimantest";

    private Node node;
    private TransportClient client;

    @Before
    public void setUp() throws Exception {
        System.out.println("Creating the ES node.");
        File esHome = new File("target/es");
        if (esHome.isDirectory()) {
            FileUtils.deleteDirectory(esHome);
        }
        Builder settings = NodeBuilder.nodeBuilder().settings();
        settings.put("path.home", esHome.getAbsolutePath());
        node = NodeBuilder.nodeBuilder().client(false).clusterName(ES_CLUSTER_NAME).data(true).local(false).settings(settings).build();
        System.out.println("Starting the ES node.");
        node.start();
        System.out.println("ES node was successfully started.");

        client = new TransportClient(ImmutableSettings.settingsBuilder().put("cluster.name", ES_CLUSTER_NAME)
                .build());
        client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Stopping the ES node.");
        client.close();
        node.stop();
        System.out.println("ES node stopped.");
    }

    @Test
    public void testCreateUser() throws Exception {
        IndexRequest request = new IndexRequest("test", "employee", "1");
        request.create(true);
        request.source("{\r\n" + 
                "    \"first_name\" : \"Will\",\r\n" + 
                "    \"last_name\" :  \"Smith\",\r\n" + 
                "    \"age\" :        39,\r\n" + 
                "    \"about\" :      \"I love to go rock climbing\",\r\n" + 
                "    \"interests\": [ \"sports\", \"nepotism\" ]\r\n" + 
                "}");
        ActionFuture<IndexResponse> future = client.index(request);
        IndexResponse response = future.get();
        if (response.isCreated()) {
            System.out.println("Employee 1 created!");
        }
        
        System.out.println("Hit ENTER to stop.");
        System.in.read();
    }

}
