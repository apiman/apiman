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
public interface JdbcConnection extends AutoCloseable {

    // TODO: there are no begintx/endtx statements seemingly?
    /**
     * Run a query, but don't need a result-set. For instance, DROP, CREATE.
     *
     * @param query the query
     * @param result the result
     */
    void query(String query, IAsyncResultHandler<Void> result);

    /**
     * Run a query, get the results back to iterate through.
     *
     * @param query the query
     * @param result the result set
     */
    void execute(String query, IAsyncResultHandler<IJdbcResultSet> result);

    /**
     * Set auto-commit status (probably best to explicitly set this?). TODO Should we have a default?
     *
     * @param autoCommit the auto-commit status
     * @param result the result
     */
    void setAutoCommit(boolean autoCommit, IAsyncResultHandler<Void> result);

    /**
     * Commit transaction
     *
     * @param result the result
     */
    void commit(IAsyncResultHandler<Void> result);

    /**
     * Abort transaction
     *
     * @param result the result
     */
    void rollback(IAsyncResultHandler<Void> result);

    /**
     * In addition to AutoCloseable we can have a version that provides a result.
     * Closes any associated connection(s).
     *
     * @param result the result
     */
    void close(IAsyncResultHandler<Void> result);
}
