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

import io.apiman.common.es.util.builder.index.EsIndexProperties;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.RestHighLevelClient;

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
        ApimanEsClientOptionsParser opts = new ApimanEsClientOptionsParser(config, getDefaultIndexPrefix());
        this.indexPrefix = opts.getIndexNamePrefix();
    }

    /**
     * Constructor.
     * @param esClient a configured high-level client.
     */
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
        return factory.createClient(config, getEsIndices(), getDefaultIndexPrefix());
    }

    /**
     * @return the client factory to use to create the ES client
     */
    protected IEsClientFactory createEsClientFactory() {
        String factoryClass = config.getOrDefault("client.type", "es");
        // In order to maintain backwards compatibility, we still accept 'jest' for the factory name, even
        // though jest has now been replaced with an official HTTP client from Elastic.
        if (EsUtils.isEsOrJest(factoryClass)) { //$NON-NLS-1$
            factoryClass = DefaultEsClientFactory.class.getName();
        } else if ("local".equalsIgnoreCase(factoryClass)) { //$NON-NLS-1$
            factoryClass = LocalClientFactory.class.getName();
        } else {
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
        // In order to maintain backwards compatibility, we still accept 'jest' for the factory name, even
        // though jest has now been replaced with an official HTTP client from Elastic.
        String factoryClass = config.get("client.type"); //$NON-NLS-1$
        if (EsUtils.isEsOrJest(factoryClass)) {
            DefaultEsClientFactory.deleteIndices(client);
        }
    }

    /**
     * Gets the default index prefix for this component.
     */
    protected abstract String getDefaultIndexPrefix();

    /**
     * Gets the index prefix name with a joiner character <code>'_'</code> (e.g. <code>foo_</code>)
     * Used when reading/writing to ES.
     */
    protected String getIndexPrefixWithJoiner() {
        return getIndexPrefix() + "_";
    }

    /**
     * Get the plain index prefix name <strong>without</strong> any joiner character.
     */
    protected String getIndexPrefix() {
        return indexPrefix;
    }

    /**
     * Get the ES index definitions for this component.
     *
     * It may be used for purposes such as DB initialisation.
     *
     * @return the list of valid Elasticsearch index definitions
     */
    public abstract Map<String, EsIndexProperties> getEsIndices();
}
