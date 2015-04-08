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
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.policies.config.basicauth.JDBCIdentitySource;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
            JDBCIdentitySource config, IAsyncResultHandler<Boolean> handler) {
        DataSource ds = lookupDatasource(config);
        String sqlPwd = password;
        switch (config.getHashAlgorithm()) {
        case MD5:
            sqlPwd = DigestUtils.md5Hex(password);
            break;
        case SHA1:
            sqlPwd = DigestUtils.sha1Hex(password);
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
        String query = config.getQuery();
        Connection conn = null;
        boolean validated = false;
        try {
            conn = ds.getConnection();
            conn.setReadOnly(true);
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, sqlPwd);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                validated = true;
            }
            resultSet.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { }
            }
        }
        
        handler.handle(AsyncResultImpl.create(validated));
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
