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

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.Health;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Base class for client factories.  Provides caching of clients.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractClientFactory {

    protected static Map<String, JestClient> clients = new HashMap<>();

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
     * @param client the jest client
     * @param indexName the name of the ES index to initialize
     * @param defaultIndexName the default ES index - used to determine which -settings.json file to use
     */
    protected void initializeClient(JestClient client, String indexName, String defaultIndexName) {
        try {
            client.execute(new Health.Builder().build());
            Action<JestResult> action = new IndicesExists.Builder(indexName).build();
            // There was occasions where a race occurred here when multiple threads try to
            // create the index simultaneously. This caused a non-fatal, but annoying, exception.
            synchronized(AbstractClientFactory.class) {
                JestResult result = client.execute(action);
                if (!result.isSucceeded()) {
                    createIndex(client, indexName, defaultIndexName + "-settings.json"); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an index.
     * @param indexName
     * @throws Exception
     */
    @SuppressWarnings("nls")
    protected void createIndex(JestClient client, String indexName, String settingsName) throws Exception {
        URL settings = AbstractClientFactory.class.getResource(settingsName);
        String source = IOUtils.toString(settings);
        JestResult response = client.execute(new CreateIndex.Builder(indexName).settings(source).build());
        if (!response.isSucceeded()) {
            // When running in e.g. Wildfly, the Gateway exists as two separate WARs - the API and the
            // runtime Gateway itself.  They both create a registry and thus they both try to initialize
            // the ES index if it doesn't exist.  A race condition could result in both WARs trying to
            // create the index.  So a result of "IndexAlreadyExistsException" should be ignored.
            if (!indexAlreadyExistsException(response)) {
                throw new Exception("Failed to create index: '" + indexName + "' Reason: " + response.getErrorMessage());
            }
        }
    }



    @SuppressWarnings("nls")
    private  boolean indexAlreadyExistsException(JestResult response) {
        System.out.println("Json Object");
        System.out.println(response.getJsonObject());

        System.out.println("Json String");
        System.out.println(response.getJsonObject());

        System.out.println("Message");
        System.out.println(response.getErrorMessage());

        // ES 1.x
        if (response.getErrorMessage().startsWith("IndexAlreadyExistsException")) {
            return true;
        }

        // ES 5.x
        // {"error": {"root_cause":[{"type":"index_already_exists_exception","reason": "..."}]}}
        if (response.getJsonObject() == null || !response.getJsonObject().has("error")) {
            return false;
        }

        // Error must be a JSON object.
        JsonObject error = response.getJsonObject().getAsJsonObject("error");
        if (!(error.has("root_cause") && error.get("root_cause").isJsonArray())) {
            return false;
        }

        JsonArray causes = error.getAsJsonArray("root_cause");

        for (JsonElement elem : causes) {
            if (elem.isJsonObject()) {
                JsonElement type = elem.getAsJsonObject().get("type");
                if (type != null && type.getAsString().equals("index_already_exists_exception")) {
                    return true;
                }
            }
        }
        return false;
    }

}
