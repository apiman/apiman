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
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.IJdbcResultSet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.SQLConnection;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class VertxJdbcConnection implements IJdbcConnection {

    private SQLConnection connection;

    public VertxJdbcConnection(SQLConnection connection) {
        this.connection = connection;
    }

    @Override
    public void query(String query, IAsyncResultHandler<IJdbcResultSet> handler) {
        connection.query(query, result -> {
            if (result.succeeded()) {
                VertxJdbcResultSet jdbcResultSet = new VertxJdbcResultSet(result.result());
                handler.handle(AsyncResultImpl.create(jdbcResultSet));
            } else {
                handler.handle(AsyncResultImpl.create(result.cause()));
            }
        });
    }

    @Override
    public void execute(String query, IAsyncResultHandler<Void> handler) {
        connection.execute(query, translateVoidHandlers(handler));
    }

    @Override
    public void setAutoCommit(boolean autoCommit, IAsyncResultHandler<Void> result) {
        connection.setAutoCommit(autoCommit, translateVoidHandlers(result));
    }

    @Override
    public void commit(IAsyncResultHandler<Void> result) {
        connection.commit(translateVoidHandlers(result));
    }

    @Override
    public void rollback(IAsyncResultHandler<Void> result) {
        connection.rollback(translateVoidHandlers(result));
    }

    @Override
    public void close(IAsyncResultHandler<Void> result) {
        connection.close(translateVoidHandlers(result));
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    private Handler<AsyncResult<Void>> translateVoidHandlers(IAsyncResultHandler<Void> apimanResult) {
        return new Handler<AsyncResult<Void>>() {

            @Override
            public void handle(AsyncResult<Void> vertxResult) {
                if (vertxResult.succeeded()) {
                    apimanResult.handle(AsyncResultImpl.create((Void) null));
                } else {
                    apimanResult.handle(AsyncResultImpl.create(vertxResult.cause()));
                }
            }
        };
    }
}
