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

import io.apiman.common.datastructures.map.LRUMap;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;

import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LDAPConnectionFactory {
    private static final int MAX_CONNECTIONS_PER_POOL = 20;
    private static final int MAX_POOLS = 20;

    private LDAPConnectionFactory() {
    }

    protected static Map<LdapConfigBean, LDAPConnectionPool> connectionPoolCache =
            new LRUMap<LdapConfigBean, LDAPConnectionPool>(MAX_POOLS) {
        private static final long serialVersionUID = 1L;

        @Override
        protected void handleRemovedElem(Map.Entry<LdapConfigBean, LDAPConnectionPool> eldest) {
            // When evicted, close the associated connection pool.
            LDAPConnectionPool pool = eldest.getValue();
            pool.close();
        }
    };

    public static LDAPConnection build(SSLSocketFactory socketFactory, LdapConfigBean config) throws LDAPException {
        if (isLdaps(config.getScheme()) || socketFactory == null) {
            return getConnection(connectionPoolCache, socketFactory, config);
        } else {
            return getConnection(connectionPoolCache, null, config);
        }
    }

    public static void releaseConnection(LDAPConnection connection) {
        if (connection != null) {
            if (connection.getConnectionPool() != null) {
                connection.getConnectionPool().releaseConnection(connection);
            } else {
                connection.close();
            }
        }
    }

    public static void releaseDefunct(LDAPConnection connection) {
        if (connection != null && connection.getConnectionPool() != null)
            connection.getConnectionPool().releaseDefunctConnection(connection);
    }

    public static void releaseConnectionAfterException(LDAPConnection connection, LDAPException e) {
        if (connection != null && connection.getConnectionPool() != null)
            connection.getConnectionPool().releaseConnectionAfterException(connection, e);
    }

    private static LDAPConnection getConnection(Map<LdapConfigBean, LDAPConnectionPool> map,
            SSLSocketFactory socketFactory, LdapConfigBean config) throws LDAPException {
        if (!map.containsKey(config)) {
            LDAPConnection template;
            if (socketFactory != null) {
                // LDAPS (with SSL)
                template = new LDAPConnection(socketFactory);
                template.connect(config.getHost(), config.getPort());
            } else {
                // LDAP
                template = new LDAPConnection(config.getHost(), config.getPort());
            }
            map.put(config, new LDAPConnectionPool(template, MAX_CONNECTIONS_PER_POOL));
        }
        return map.get(config).getConnection();
    }

    private static boolean isLdaps(String scheme) {
        return scheme.toLowerCase().startsWith("ldaps"); //$NON-NLS-1$
    }


}
