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
import io.apiman.gateway.engine.components.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * A default implementation of the JDBC Component {@link IJdbcComponent}.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultJdbcComponent implements IJdbcComponent {

    private Map<String, IJdbcClient> clients = new HashMap<>();

    /**
     * Constructor.
     */
    public DefaultJdbcComponent() {
    }

    /**
     * @see io.apiman.gateway.engine.components.IJdbcComponent#createShared(java.lang.String, io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean)
     */
    @Override
    public synchronized IJdbcClient createShared(String dsName, JdbcOptionsBean config) {
        if (clients.containsKey(dsName)) {
            return clients.get(dsName);
        } else {
            DataSource ds = datasourceFromConfig(config);
            DefaultJdbcClient client = new DefaultJdbcClient(ds);
            clients.put(dsName, client);
            return client;
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.IJdbcComponent#createStandalone(io.apiman.gateway.engine.components.jdbc.JdbcOptionsBean)
     */
    @Override
    public IJdbcClient createStandalone(JdbcOptionsBean config) {
        return createShared(dsNameFromConfig(config), config);
    }

    /**
     * @see io.apiman.gateway.engine.components.IJdbcComponent#create(javax.sql.DataSource)
     */
    @Override
    public IJdbcClient create(DataSource ds) {
        return new DefaultJdbcClient(ds);
    }

    /**
     * Creates a datasource from the given jdbc config info.
     * @param config
     */
    @SuppressWarnings("nls")
    protected DataSource datasourceFromConfig(JdbcOptionsBean config) {
        Properties props = new Properties();
        props.putAll(config.getDsProperties());
        setConfigProperty(props, "jdbcUrl", config.getJdbcUrl());
        setConfigProperty(props, "username", config.getUsername());
        setConfigProperty(props, "password", config.getPassword());

        setConfigProperty(props, "connectionTimeout", config.getConnectionTimeout());
        setConfigProperty(props, "idleTimeout", config.getIdleTimeout());
        setConfigProperty(props, "maxPoolSize", config.getMaximumPoolSize());
        setConfigProperty(props, "maxLifetime", config.getMaxLifetime());
        setConfigProperty(props, "minIdle", config.getMinimumIdle());
        setConfigProperty(props, "poolName", config.getPoolName());
        setConfigProperty(props, "autoCommit", config.isAutoCommit());

        HikariConfig hikariConfig = new HikariConfig(props);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Sets a configuration property, but only if it's not null.
     * @param props
     * @param propName
     * @param value
     */
    private void setConfigProperty(Properties props, String propName, Object value) {
        if (value != null) {
            props.setProperty(propName, String.valueOf(value));
        }
    }

    /**
     * Creates a datasource name (for caching) from the config.
     * @param config
     */
    private String dsNameFromConfig(JdbcOptionsBean config) {
        StringBuilder builder = new StringBuilder();
        builder.append(config.getJdbcUrl())
            .append("::") //$NON-NLS-1$
            .append(config.getUsername())
            .append("::") //$NON-NLS-1$
            .append(config.getPassword())
            .append("::") //$NON-NLS-1$
            .append(config.getConnectionTimeout())
            .append("::") //$NON-NLS-1$
            .append(config.getIdleTimeout())
            .append("::") //$NON-NLS-1$
            .append(config.getMaximumPoolSize())
            .append("::") //$NON-NLS-1$
            .append(config.getMaxLifetime())
            .append("::") //$NON-NLS-1$
            .append(config.getMinimumIdle())
            .append("::") //$NON-NLS-1$
            .append(config.getPoolName())
            .append("::") //$NON-NLS-1$
            .append(config.isAutoCommit());
        return builder.toString();
    }

    /**
     * JDBC client impl.
     * @author eric.wittmann@redhat.com
     */
    private static class DefaultJdbcClient implements IJdbcClient {

        protected DataSource ds;

        /**
         * Constructor.
         * @param ds
         */
        public DefaultJdbcClient(DataSource ds) {
            this.ds = ds;
        }

        /**
         * @see io.apiman.gateway.engine.components.jdbc.IJdbcClient#connect(io.apiman.gateway.engine.async.IAsyncResultHandler)
         */
        @Override
        public void connect(IAsyncResultHandler<IJdbcConnection> handler) {
            IJdbcConnection jdbcConnection = null;
            try {
                Connection connection = ds.getConnection();
                jdbcConnection = new DefaultJdbcConnection(connection);
                handler.handle(AsyncResultImpl.create(jdbcConnection));
            } catch (Exception e) {
                handler.handle(AsyncResultImpl.create(e, IJdbcConnection.class));
            } finally {
                try {
                    // If not closed by now (since this is a synchronous implementation of the client
                    // interface) then the consumer messed up.  We'll be nice and close it for them here.
                    if (!jdbcConnection.isClosed()) {
                        System.err.print("NOTE: closing a JDBC connection that should have already been closed!"); //$NON-NLS-1$
                        jdbcConnection.close();
                    }
                } catch (Exception e) {
                    // eat it
                }
            }
        }

    }

}
