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
package io.apiman.gateway.engine.es;

import io.searchbox.client.JestClient;

import java.util.Map;

/**
 * Base class for the elasticsearch component impls.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractESComponent {

    private final Map<String, String> config;
    private JestClient esClient;

    /**
     * Constructor.
     * @param config the config
     */
    public AbstractESComponent(Map<String, String> config) {
        this.config = config;
    }

    /**
     * @return the esClient
     */
    public synchronized JestClient getClient() {
        if (esClient == null) {
            esClient = ESClientFactory.createClient(config);
        }
        return esClient;
    }

    /**
     * Gets the configured index name.
     */
    protected String getIndexName() {
        if (config.containsKey("index")) { //$NON-NLS-1$
            return config.get("index"); //$NON-NLS-1$
        } else {
            return getDefaultIndexName();
        }
    }

    /**
     * @return the default index name
     */
    protected String getDefaultIndexName() {
        return ESConstants.INDEX_NAME;
    }

}
