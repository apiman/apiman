/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policies.auth;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.jdbc.IJdbcClient;
import io.apiman.gateway.engine.components.jdbc.IJdbcComponent;
import io.apiman.gateway.engine.components.jdbc.IJdbcConnection;
import io.apiman.gateway.engine.components.jdbc.IJdbcResultSet;
import io.apiman.gateway.engine.policies.AuthorizationPolicy;
import io.apiman.gateway.engine.policies.config.basicauth.JDBCIdentitySource;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * An identity validator that uses the static information in the config
 * to validate the user.
 *
 * @author eric.wittmann@redhat.com
 */
public class JDBCIdentityValidator implements IIdentityValidator<JDBCIdentitySource> {

    /**
     * Constructor.
     */
    public JDBCIdentityValidator() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.auth.IIdentityValidator#validate(String, String, ServiceRequest, IPolicyContext, Object, IAsyncResultHandler)
     */
    @Override
    public void validate(String username, String password, ServiceRequest request, IPolicyContext context,
            JDBCIdentitySource config, final IAsyncResultHandler<Boolean> handler) {
        IJdbcComponent jdbcComponent = context.getComponent(IJdbcComponent.class);
        
        DataSource ds = null;
        try {
            ds = lookupDatasource(config);
        } catch (Throwable t) {
            handler.handle(AsyncResultImpl.create(t, Boolean.class));
            return;
        }
        String sqlPwd = password;
        switch (config.getHashAlgorithm()) {
        case MD5:
            sqlPwd = DigestUtils.md5Hex(password);
            break;
        case SHA1:
            sqlPwd = DigestUtils.shaHex(password);
            break;
        case SHA256:
            sqlPwd = DigestUtils.sha256Hex(password);
            break;
        case SHA384:
            sqlPwd = DigestUtils.sha384Hex(password);
            break;
        case SHA512:
            sqlPwd = DigestUtils.sha512Hex(password);
            break;
        case None:
        default:
            break;
        }
        final String query = config.getQuery();
        final String queryUsername = username;
        final String queryPassword = sqlPwd;
        
        IJdbcClient client = jdbcComponent.create(ds);
        client.connect(new IAsyncResultHandler<IJdbcConnection>() {
            @Override
            public void handle(IAsyncResult<IJdbcConnection> result) {
                if (result.isError()) {
                    handler.handle(AsyncResultImpl.create(result.getError(), Boolean.class));
                } else {
                    validate(result.getResult(), query, queryUsername, queryPassword, context, config, handler);
                }
            }
        });
    }

    /**
     * @param connection
     * @param query
     * @param username
     * @param context
     * @param password
     * @param config
     * @param handler
     */
    protected void validate(final IJdbcConnection connection, final String query, final String username,
            final String password, final IPolicyContext context, final JDBCIdentitySource config,
            final IAsyncResultHandler<Boolean> handler) {
        IAsyncResultHandler<IJdbcResultSet> queryHandler = new IAsyncResultHandler<IJdbcResultSet>() {
            @Override
            public void handle(IAsyncResult<IJdbcResultSet> result) {
                if (result.isError()) {
                    closeQuietly(connection);
                    handler.handle(AsyncResultImpl.create(result.getError(), Boolean.class));
                } else {
                    boolean validated = false;
                    IJdbcResultSet resultSet = result.getResult();
                    if (resultSet.next()) {
                        validated = true;
                    }
                    resultSet.close();
                    if (validated && config.isExtractRoles()) {
                        extractRoles(connection, username, context, config, handler);
                    } else {
                        closeQuietly(connection);
                        handler.handle(AsyncResultImpl.create(validated));
                    }
                }
            }
        };
        connection.query(queryHandler, query, username, password);
    }

    /**
     * @param connection
     * @param username
     * @param context
     * @param config
     * @param handler
     */
    protected void extractRoles(final IJdbcConnection connection, final String username,
            final IPolicyContext context, final JDBCIdentitySource config,
            final IAsyncResultHandler<Boolean> handler) {
        
        String roleQuery = config.getRoleQuery();
        IAsyncResultHandler<IJdbcResultSet> roleHandler = new IAsyncResultHandler<IJdbcResultSet>() {
            @Override
            public void handle(IAsyncResult<IJdbcResultSet> result) {
                if (result.isError()) {
                    closeQuietly(connection);
                    handler.handle(AsyncResultImpl.create(result.getError(), Boolean.class));
                } else {
                    Set<String> extractedRoles = new HashSet<>();
                    IJdbcResultSet resultSet = result.getResult();
                    while (resultSet.next()) {
                        String roleName = resultSet.getString(1);
                        extractedRoles.add(roleName);
                    }
                    context.setAttribute(AuthorizationPolicy.AUTHENTICATED_USER_ROLES, extractedRoles);
                    closeQuietly(connection);
                    handler.handle(AsyncResultImpl.create(true));
                }
            }
        };
        connection.query(roleHandler, roleQuery, username);
    }

    /**
     * @param connection
     */
    protected void closeQuietly(IJdbcConnection connection) {
        try {
            connection.close();
        } catch (Exception e) {
            // TODO log this error
        }
    }

    /**
     * Lookup the datasource from JNDI.
     * @param config
     */
    private DataSource lookupDatasource(JDBCIdentitySource config) {
        DataSource ds = null;
        try {
            InitialContext ctx = new InitialContext();
            ds = lookupDS(ctx, config.getDatasourcePath());
            if (ds == null) {
                ds = lookupDS(ctx, "java:comp/env/" + config.getDatasourcePath()); //$NON-NLS-1$
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (ds == null) {
            throw new RuntimeException("Datasource not found: " + config.getDatasourcePath()); //$NON-NLS-1$
        }
        return ds;
    }

    /**
     * Lookup the datasource from JNDI.
     * @param ctx
     * @param path
     */
    private DataSource lookupDS(InitialContext ctx, String path) {
        try {
            return (DataSource) ctx.lookup(path);
        } catch (NamingException e) {
            return null;
        }
    }

}
