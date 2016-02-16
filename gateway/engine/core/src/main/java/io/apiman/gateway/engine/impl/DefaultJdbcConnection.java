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

package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.IJdbcResultSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A simple/default implementation of the {@link IJdbcConnection} interface.  This
 * provides an implementation useful when running in a synchronous environment.  It
 * should not be used when the Gateway is running on an async platform such as vert.x.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultJdbcConnection implements IJdbcConnection {
    
    private Connection connection;

    /**
     * Constructor.
     * @param connection
     */
    public DefaultJdbcConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        connection.close();
    }
    
    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#isClosed()
     */
    @Override
    public boolean isClosed() throws Exception {
        return connection.isClosed();
    }
    
    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#query(io.apiman.gateway.engine.async.IAsyncResultHandler, java.lang.String, java.lang.Object[])
     */
    @Override
    public void query(IAsyncResultHandler<IJdbcResultSet> handler, String sql, Object... params) {
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            int idx = 1;
            for (Object param : params) {
                statement.setObject(idx++, param);
            }
            ResultSet resultSet = statement.executeQuery();
            IJdbcResultSet rval = new DefaultJdbcResultSet(resultSet);
            handler.handle(AsyncResultImpl.create(rval));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(e, IJdbcResultSet.class));
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#execute(io.apiman.gateway.engine.async.IAsyncResultHandler, java.lang.String, java.lang.Object[])
     */
    @Override
    public void execute(IAsyncResultHandler<Void> handler, String sql, Object... params) {
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            int idx = 1;
            for (Object param : params) {
                statement.setObject(idx++, param);
            }
            statement.execute();
            handler.handle(AsyncResultImpl.create((Void) null, Void.class));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(e, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#setAutoCommit(boolean, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void setAutoCommit(boolean autoCommit, IAsyncResultHandler<Void> handler) {
        try {
            connection.setAutoCommit(autoCommit);
            handler.handle(AsyncResultImpl.create((Void) null, Void.class));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#commit(io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void commit(IAsyncResultHandler<Void> handler) {
        try {
            connection.commit();
            handler.handle(AsyncResultImpl.create((Void) null, Void.class));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#rollback(io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void rollback(IAsyncResultHandler<Void> handler) {
        try {
            connection.rollback();
            handler.handle(AsyncResultImpl.create((Void) null, Void.class));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.jdbc.IJdbcConnection#close(io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void close(IAsyncResultHandler<Void> handler) {
        try {
            connection.close();
            handler.handle(AsyncResultImpl.create((Void) null, Void.class));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Void.class));
        }
    }

}
