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
package io.apiman.gateway.platforms.vertx3.components.jdbc;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.Map.Entry;

import javax.sql.DataSource;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class VertxJdbcClientImpl implements IJdbcClient {
    //TODO cache jdbcClients && json configs? LinkedHashMap + removeEldestEntry?
    //private static Map<>
    private JDBCClient jdbcClient;

    public VertxJdbcClientImpl(Vertx vertx, String dsName, JdbcOptionsBean config) {
        jdbcClient = JDBCClient.createShared(vertx, parseConfig(config), dsName);
    }

    public VertxJdbcClientImpl(Vertx vertx, JdbcOptionsBean config) {
        jdbcClient = JDBCClient.createNonShared(vertx, parseConfig(config));
    }

    public VertxJdbcClientImpl(Vertx vertx, DataSource ds) {
        jdbcClient = JDBCClient.create(vertx, ds);
    }

    @Override
    public void connect(IAsyncResultHandler<IJdbcConnection> handler) {
        jdbcClient.getConnection(result -> {
            if (result.succeeded()) {
                handler.handle(AsyncResultImpl.create(new VertxJdbcConnection(result.result())));
            } else {
                handler.handle(AsyncResultImpl.create(result.cause()));
            }
        });
    }

    /**
     * Translate our abstracted {@link JdbcOptionsBean} into a Vert.x-specific config.
     *
     * We are assuming that the user is using HikariCP.
     */
    @SuppressWarnings("nls")
    protected static JsonObject parseConfig(JdbcOptionsBean config) {
        JsonObject jsonConfig = new JsonObject()
                .put("jdbcUrl", config.getJdbcUrl())
                .put("username", config.getUsername())
                .put("password", config.getPassword())
                .put("autoCommit", config.isAutoCommit())
                .put("connectionTimeout", config.getConnectionTimeout())
                .put("idleTimeout", config.getIdleTimeout())
                .put("maxLifetime", config.getMaxLifetime())
                .put("minimumIdle", config.getMinimumIdle())
                .put("maximumPoolSize", config.getMaximumPoolSize())
                .put("poolName", config.getPoolName());

        JsonObject dsProperties = new JsonObject();

        for (Entry<String, Object> entry : config.getDsProperties().entrySet()) {
            dsProperties.put(entry.getKey(), entry.getValue());
        }

        jsonConfig.put("properties", dsProperties);
        return jsonConfig;
    }
}
