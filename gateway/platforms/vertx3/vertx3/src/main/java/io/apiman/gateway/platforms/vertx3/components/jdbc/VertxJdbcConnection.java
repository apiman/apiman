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
    private boolean closed;

    /**
     * Constructor.
     * @param connection
     */
    public VertxJdbcConnection(SQLConnection connection) {
        this.connection = connection;
        this.closed = false;
    }
    
    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#query(io.apiman.gateway.engine.async.IAsyncResultHandler, java.lang.String, java.lang.Object[])
     */
    @Override
    public void query(IAsyncResultHandler<IJdbcResultSet> handler, String sql, Object... params) {
        String query = format(sql, params);
        connection.query(query, result -> {
            if (result.succeeded()) {
                VertxJdbcResultSet jdbcResultSet = new VertxJdbcResultSet(result.result());
                handler.handle(AsyncResultImpl.create(jdbcResultSet));
            } else {
                handler.handle(AsyncResultImpl.create(result.cause()));
            }
        });
    }
    
    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#execute(io.apiman.gateway.engine.async.IAsyncResultHandler, java.lang.String, java.lang.Object[])
     */
    @Override
    public void execute(IAsyncResultHandler<Void> handler, String sql, Object... params) {
        String statement = format(sql, params);
        connection.execute(statement, translateVoidHandlers(handler));
    }

    @Override
    public void setAutoCommit(boolean autoCommit, IAsyncResultHandler<Void> handler) {
        connection.setAutoCommit(autoCommit, translateVoidHandlers(handler));
    }

    @Override
    public void commit(IAsyncResultHandler<Void> handler) {
        connection.commit(translateVoidHandlers(handler));
    }

    @Override
    public void rollback(IAsyncResultHandler<Void> handler) {
        connection.rollback(translateVoidHandlers(handler));
    }

    @Override
    public void close(IAsyncResultHandler<Void> handler) {
        connection.close(translateVoidHandlers(handler));
        closed = true;
    }

    @Override
    public void close() throws Exception {
        connection.close();
        closed = true;
    }
    
    @Override
    public boolean isClosed() throws Exception {
        return closed;
    }

    private String format(String sql, Object[] params) {
        // TODO format the sql - replace all ?'s with appropriate param values
        return sql;
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
