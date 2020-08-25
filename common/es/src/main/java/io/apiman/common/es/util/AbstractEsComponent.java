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
package io.apiman.common.es.util;

import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Base class for the elasticsearch component impls.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractEsComponent {

    private final Map<String, String> config;
    private RestHighLevelClient esClient;
    private String indexPrefix;

    /**
     * Constructor.
     * @param config the config
     */
    public AbstractEsComponent(Map<String, String> config) {
        this.config = config;
        String indexPrefix = config.get("client.indexPrefix"); //$NON-NLS-1$
        if (indexPrefix == null) {
            indexPrefix = getDefaultIndexPrefix();
        }
        this.indexPrefix = indexPrefix;
    }

    public AbstractEsComponent(RestHighLevelClient esClient) {
        this.config = null;
        this.esClient = esClient;
    }

    /**
     * @return the esClient
     */
    public synchronized RestHighLevelClient getClient() {
        if (esClient == null) {
            esClient = createClient();
        }
        return esClient;
    }

    /**
     * @return a new ES client
     */
    protected RestHighLevelClient createClient() {
        IEsClientFactory factory = createEsClientFactory();
        return factory.createClient(config, getDefaultIndexPrefix(), getDefaultIndices());
    }

    /**
     * @return the client factory to use to create the ES client
     */
    protected IEsClientFactory createEsClientFactory() {
        String factoryClass = config.get("client.type"); //$NON-NLS-1$
        if ("es".equals(factoryClass)) { //$NON-NLS-1$
            factoryClass = DefaultEsClientFactory.class.getName();
        } else if ("local".equals(factoryClass)) { //$NON-NLS-1$
            factoryClass = LocalClientFactory.class.getName();
        } else if (factoryClass == null) {
            throw new RuntimeException("Invalid elasticsearch client type: " + factoryClass); //$NON-NLS-1$
        }

        try {
            return (IEsClientFactory) Class.forName(factoryClass).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Error creating elasticsearch client type: " + factoryClass, e); //$NON-NLS-1$
        }
    }

    /**
     * Delete all created es indices
     * @param client
     * @throws IOException
     */
    protected void deleteIndices(RestHighLevelClient client) throws IOException {
        String factoryClass = config.get("client.type"); //$NON-NLS-1$
        if ("es".equals(factoryClass)) { //$NON-NLS-1$
            DefaultEsClientFactory.deleteIndices(client);
        }
    }

    /**
     * Gets the default index prefix for this component.
     */
    protected abstract String getDefaultIndexPrefix();

    /**
     * Get the default indecies for this component.
     */
    protected abstract List<String> getDefaultIndices();

    /**
     * Gets the index name to use when reading/writing to ES.
     */
    protected String getIndexPrefix() {
        return indexPrefix + "_";
    }

}
