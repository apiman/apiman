/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.engine.vertx.polling;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.vertx.polling.exceptions.UnsupportedProtocolException;
import io.apiman.gateway.engine.vertx.polling.fetchers.FileResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.HttpResourceFetcher;
import io.apiman.gateway.engine.vertx.polling.fetchers.ResourceFetcher;
import io.apiman.gateway.platforms.vertx3.common.verticles.Json;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.Arguments;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Load apiman policy configuration as JSON from a remote file and unmarshalls them
 * into lists of Clients and APIs.
 *
 * Supports file and HTTP/S, with OAuth2 and BASIC for the latter.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
@SuppressWarnings("nls")
public class PolicyConfigLoader {

    private URI uri;
    private Vertx vertx;
    private Handler<Throwable> exceptionHandler;
    private Buffer rawData;
    private Logger log = LoggerFactory.getLogger(PolicyConfigLoader.class);
    private Map<String, String> config;
    private List<Client> clients;
    private List<Api> apis;
    private Handler<List<Api>> apiResultHandler;
    private Handler<List<Client>> clientResultHandler;

    /**
     * @param vertx the vertx instance
     * @param policyConfigUri the config URI
     * @param config the configuration
     */
    public PolicyConfigLoader(Vertx vertx, URI policyConfigUri, Map<String, String> config) {
        this.vertx = vertx;
        this.uri = policyConfigUri;
        this.config = config;
    }

    /**
     * Set the client result handler, invoked when Clients have been unmarshalled successfully.
     * @param clientResultHandler the handler
     * @return fluent
     */
    public PolicyConfigLoader setClientResultHandler(Handler<List<Client>> clientResultHandler) {
        this.clientResultHandler = clientResultHandler;
        return this;
    }

    /**
     * Set the API result handler, invoked when APIs have been unmarshalled successfully.
     * @param apiResultHandler the API result handler
     * @return fluent
     */
    public PolicyConfigLoader setApiResultHandler(Handler<List<Api>> apiResultHandler) {
        this.apiResultHandler = apiResultHandler;
        return this;
    }

    /**
     * Set the exception handler, invoked if an exception occurs.
     * @param exceptionHandler the exception handler
     * @return fluent
     */
    public PolicyConfigLoader setExceptionHandler(Handler<Throwable> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Excepute the fetcher and load the resources.
     */
    public void load() {
        fetchResources();
    }

    private void fetchResources() {
        getResourceFetcher()
            .exceptionHandler(exceptionHandler)
            .fetch(data -> {
                rawData = data;
                processData();
                if (apiResultHandler != null)
                    apiResultHandler.handle(apis);
                if (clientResultHandler != null)
                    clientResultHandler.handle(clients);
            });
    }

    private ResourceFetcher getResourceFetcher() {
        String scheme = uri.getScheme() == null ? "file" : uri.getScheme().toLowerCase();
        switch (scheme) {
        case "http":
            return new HttpResourceFetcher(vertx, uri, config, false);
        case "https":
            return new HttpResourceFetcher(vertx, uri, config, true);
        case "file":
            return new FileResourceFetcher(vertx, uri, config);
        default:
            throw new UnsupportedProtocolException(String.format("%s is not supported. Available: http, https and file.", uri.getScheme()));
        }
    }

    private void processData() {
        if (rawData.length() == 0) {
            log.warn("Remote file at {0} was empty.", uri);
            return;
        }
        try {
            JsonObject json = new JsonObject(rawData.toString("UTF-8").trim());
            log.trace("Processing JSON: {0}", json);
            if (clientResultHandler != null)
                clients = requireJsonArray("clients", json, Client.class);
            if (apiResultHandler != null)
                apis = requireJsonArray("apis", json, Api.class);
        } catch (DecodeException e) {
            exceptionHandler.handle(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T, K> List<T> requireJsonArray(String keyName, JsonObject json, Class<K> klazz) {
        // Contains key.
        Arguments.require(json.containsKey(keyName),
                String.format("Must provide array of %s objects for key '%s'", StringUtils.capitalize(keyName), keyName));
        // Is of type array.
        Arguments.require(json.getValue(keyName) instanceof JsonArray,
                String.format("'%s' must be a Json array", keyName));
        // Transform into List<T>.
        return Json.decodeValue(json.getJsonArray(keyName).encode(), List.class, klazz);
    }

}
