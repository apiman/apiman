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
package io.apiman.gateway.test.junit.vertx3;

import io.apiman.common.es.util.DefaultEsClientFactory;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.builder.index.EsIndexProperties;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import java.util.Collections;
import java.util.Map;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Reset the ES index simply by deleting it. For testing purposes.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class EsResetter extends AbstractEsComponent implements Resetter {

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
            DefaultEsClientFactory.clearClientCache();

            final RestHighLevelClient client = super.getClient();
            deleteIndices(client, latch);

            client.indices().flush(new FlushRequest(), RequestOptions.DEFAULT);

            latch.await();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete all created es indices
     * @param client
     * @param latch
     * @throws IOException
     */
    private void deleteIndices(RestHighLevelClient client, CountDownLatch latch) throws IOException {
        deleteIndices(client);
        latch.countDown();
    }

    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.GATEWAY_INDEX_NAME;
    }

    @Override
    public Map<String, EsIndexProperties> getEsIndices() {
        return Collections.emptyMap();
    }
}
