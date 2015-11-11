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
package io.apiman.gateway.engine.components.jdbc;

import io.apiman.gateway.engine.async.IAsyncResultHandler;

import java.sql.Connection;

/**
 * This is essentially an simple, async subset rendition of the standard SQL {@link Connection} interface.
 *
 * Extending the {@link AutoCloseable} interface means we can use this with the
 * Java 7 try-catch-autoclose feature.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface IJdbcConnection extends AutoCloseable {

    /**
     * Run a query, get the results back to iterate through.
     *
     * @param handler the result handler
     * @param sql the sql query (with ?'s for params)
     * @param params the parameter values
     */
    void query(IAsyncResultHandler<IJdbcResultSet> handler, String sql, Object ... params);

    /**
     * Run a query, but don't need a result-set. For instance, DROP, CREATE.
     *
     * @param handler the handler
     * @param sql the sql statement (with ?'s for params)
     * @param params the parameter values
     */
    void execute(IAsyncResultHandler<Void> handler, String sql, Object ... params);

    /**
     * Set auto-commit status (probably best to explicitly set this?). TODO Should we have a default?
     *
     * @param autoCommit the auto-commit status
     * @param handler the result handler
     */
    void setAutoCommit(boolean autoCommit, IAsyncResultHandler<Void> handler);

    /**
     * Commit transaction
     *
     * @param handler the handler
     */
    void commit(IAsyncResultHandler<Void> handler);

    /**
     * Abort transaction
     *
     * @param handler the handler
     */
    void rollback(IAsyncResultHandler<Void> handler);

    /**
     * In addition to AutoCloseable we can have a version that provides a handler.
     * Closes any associated connection(s).
     *
     * @param handler the handler
     */
    void close(IAsyncResultHandler<Void> handler);

    /**
     * Indicates if the connection is closed.
     *
     * @return true iff the connection has been closed, else false.
     * @throws Exception the exception
     */
    boolean isClosed() throws Exception;
}
