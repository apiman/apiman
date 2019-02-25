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

import io.apiman.common.es.util.AbstractClientFactory;
import io.apiman.common.es.util.IEsClientFactory;
import io.searchbox.client.JestClient;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Factory for creating elasticsearch clients.
 *
 * @author eric.wittmann@redhat.com
 */
public class LocalClientFactory extends AbstractClientFactory implements IEsClientFactory {

    /**
     * Constructor.
     */
    public LocalClientFactory() {
    }

    /**
     * Creates a client from information in the config map.
     * @param config the configuration
     * @param defaultIndexName the default index to use if not specified in the config
     * @return the ES client
     */
    public JestClient createClient(Map<String, String> config, String defaultIndexName) {
        JestClient client;
        String indexName = config.get("client.index"); //$NON-NLS-1$
        if (indexName == null) {
            indexName = defaultIndexName;
        }
        client = createLocalClient(config, indexName, defaultIndexName);
        return client;
    }

    /**
     * Creates a local client from a configuration map.
     * @param config the config from apiman.properties
     * @param indexName the name of the ES index
     * @param defaultIndexName the default ES index name
     * @return the ES client
     */
    public JestClient createLocalClient(Map<String, String> config, String indexName, String defaultIndexName) {
        String clientLocClassName = config.get("client.class"); //$NON-NLS-1$
        String clientLocFieldName = config.get("client.field"); //$NON-NLS-1$
        return createLocalClient(clientLocClassName, clientLocFieldName, indexName, defaultIndexName);
    }

    /**
     * Creates a cache by looking it up in a static field.  Typically used for
     * testing.
     * @param className the class name
     * @param fieldName the field name
     * @param indexName the name of the ES index
     * @param defaultIndexName the name of the default ES index
     * @return the ES client
     */
    public JestClient createLocalClient(String className, String fieldName, String indexName, String defaultIndexName) {
        String clientKey = "local:" + className + '/' + fieldName; //$NON-NLS-1$
        synchronized (clients) {
            if (clients.containsKey(clientKey)) {
                return clients.get(clientKey);
            } else {
                try {
                    Class<?> clientLocClass = Class.forName(className);
                    Field field = clientLocClass.getField(fieldName);
                    JestClient client = (JestClient) field.get(null);
                    clients.put(clientKey, client);
                    initializeClient(client, indexName, defaultIndexName);
                    return client;
                } catch (ClassNotFoundException | NoSuchFieldException | SecurityException
                        | IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException("Error using local elasticsearch client.", e); //$NON-NLS-1$
                }
            }
        }
    }

}
