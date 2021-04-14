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
import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * Base class for client factories.  Provides caching of clients.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractClientFactory {

    private static final IApimanLogger logger = ApimanLoggerFactory.getLogger(AbstractClientFactory.class);

    protected static final Map<String, RestHighLevelClient> clients = new HashMap<>();

    protected static final Set<String> createdIndices = new HashSet<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Clears all the clients from the cache.  Useful for unit testing.
     */
    public static void clearClientCache() {
        clients.clear();
    }

    /**
     * Constructor.
     */
    public AbstractClientFactory() {
    }

    /**
     * Called to initialize the storage.
     * @param client the es client
     * @param indexDefs the index definitions for the component being initialised (can be empty map)
     * @param indexPrefix the index prefix of the ES index to initialize
     */
    protected void initializeIndices(RestHighLevelClient client, Map<String, EsIndexProperties> indexDefs, String indexPrefix) {
        try {
            //Do Health request
            ClusterHealthRequest healthRequest = new ClusterHealthRequest();
            try {
                client.cluster().health(healthRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                logger.error("Health check failed - cannot connect to elasticsearch", e);
            }

            // There was occasions where a race occurred here when multiple threads try to
            // create the index simultaneously. This caused a non-fatal, but annoying, exception.
            synchronized (AbstractClientFactory.class) {
                for (Entry<String, EsIndexProperties> entry : indexDefs.entrySet()) {

                    String indexPostfix = entry.getKey();
                    EsIndexProperties index = entry.getValue();

                    String fullIndexName = EsIndexMapping.getFullIndexName(indexPrefix, indexPostfix);

                    if (!createdIndices.contains(fullIndexName)) {
                        GetIndexRequest indexExistsRequest = new GetIndexRequest(fullIndexName);
                        boolean indexExists = client.indices().exists(indexExistsRequest, RequestOptions.DEFAULT);
                        if (!indexExists) {
                            this.createIndex(client, index, indexPrefix, indexPostfix); //$NON-NLS-1$
                            createdIndices.add(fullIndexName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete all indices used by manager ui
     * @param client the elasticsearch client
     * @throws IOException if problem connecting to or interacting with ES
     */
    public static void deleteIndices(RestHighLevelClient client) throws IOException {
        final Iterator<String> iterator = createdIndices.iterator();
        while (iterator.hasNext()) {
            String fullIndexName = iterator.next();
            GetIndexRequest indexExistsRequest = new GetIndexRequest(fullIndexName);
            boolean indexExists = client.indices().exists(indexExistsRequest, RequestOptions.DEFAULT);
            if (indexExists) {
                boolean success = deleteIndex(client, fullIndexName);
                if (success) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Delete index with given name
     * @param client the elasticsearch client
     * @param indexName the index name
     * @throws IOException if problem connecting to or interacting with ES
     */
    private static boolean deleteIndex(RestHighLevelClient client, String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        if (!response.isAcknowledged()) {
            logger.error("Failed to delete index " + indexName + ": " + "response was not acknowledged", new Exception()); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        } else {
            logger.info("Index deleted: " + indexName); //$NON-NLS-1$
            return true;
        }
    }

    /**
     * Creates an index.
     * @param client the elasticsearch client
     * @param indexDef the index definition for the component being initialised
     * @param indexPrefix the index prefix
     * @param indexPostfix the index postfix
     * @throws IOException if problem connecting to or interacting with ES
     */
    @SuppressWarnings("nls")
    protected void createIndex(RestHighLevelClient client, EsIndexProperties indexDef, String indexPrefix, String indexPostfix) throws Exception {
        String indexToCreate = EsIndexMapping.getFullIndexName(indexPrefix, indexPostfix);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexToCreate);
        String mapping;
        try {
            mapping = objectMapper.writeValueAsString(indexDef);
            // Create index using a full definition.
            createIndexRequest = createIndexRequest.mapping(mapping, XContentType.JSON);
        } catch (JsonProcessingException jpe) {
            logger.warn("The EsIndex definition provided by {} definition cannot be marshalled", this.getClass().getCanonicalName());
            throw jpe;
        }
        CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        // When running in e.g. Wildfly, the Gateway exists as two separate WARs - the API and the
        // runtime Gateway itself.  They both create a registry and thus they both try to initialize
        // the ES index if it doesn't exist.  A race condition could result in both WARs trying to
        // create the index.  So a result of "IndexAlreadyExistsException" should be ignored.
        if (!createIndexResponse.isAcknowledged()) {
            logger.error("Failed to create ES index: '" + indexToCreate + "' Reason: request was not acknowledged.", new Exception());
        } else if (!createIndexResponse.isShardsAcknowledged()) {
            logger.error("Failed to create ES index: '" + indexToCreate + "' Reason: request was not acknowledged by shards.", new Exception());
        } else {
            logger.info("ES index created: " + indexToCreate);
            logger.debug("Creating an index => " + mapping);
        }
    }

}
