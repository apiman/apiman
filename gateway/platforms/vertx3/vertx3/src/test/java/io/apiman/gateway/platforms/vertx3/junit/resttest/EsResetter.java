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
package io.apiman.gateway.platforms.vertx3.junit.resttest;

import java.util.concurrent.CountDownLatch;

import io.apiman.gateway.engine.es.AbstractESComponent;
import io.apiman.gateway.engine.es.ESClientFactory;
import io.apiman.gateway.engine.es.ESConstants;
import io.apiman.gateway.platforms.vertx3.config.VertxEngineConfig;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Delete;

/**
 * Reset the ES index simply by deleting it. For testing purposes.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class EsResetter extends AbstractESComponent implements Resetter {

    public EsResetter(VertxEngineConfig vertxConf) {
        super(vertxConf.getRegistryConfig());
        System.out.println("Registry Config:");
        System.out.println(vertxConf.getRegistryConfig().toString());
    }

    @Override
    public void reset() {
        try {
            CountDownLatch latch = new CountDownLatch(1);

            // Important! Or will get cached client that assumes the DB schema has already been created
            // and subtly horrible things will happen, and you'll waste a whole day debugging it! :-)
            ESClientFactory.clearClientCache();

            getClient().executeAsync(new Delete.Builder(ESConstants.INDEX_NAME).build(),
                    new JestResultHandler<JestResult>() {

                @Override
                public void completed(JestResult result) {
                    latch.countDown();
                    System.out.println("=== Deleted index: " + result.getJsonString());
                }

                @Override
                public void failed(Exception ex) {
                    latch.countDown();
                    System.err.println("=== Failed to delete index: " + ex.getMessage());
                    throw new RuntimeException(ex);
                }
            });

            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
