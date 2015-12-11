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

    protected static Map<LdapConfigBean, LDAPConnectionPool> connectionPoolCache =
            new LRUMap<LdapConfigBean, LDAPConnectionPool>(MAX_POOLS) {
        private static final long serialVersionUID = 1L;

        @Override
        protected void handleRemovedElem(Map.Entry<LdapConfigBean, LDAPConnectionPool> eldest) {
            eldest.getValue().close();
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
        if (connection.getConnectionPool() != null)
            connection.getConnectionPool().releaseConnection(connection);
    }

    public static void releaseDefunct(LDAPConnection connection) {
        if (connection.getConnectionPool() != null)
            connection.getConnectionPool().releaseDefunctConnection(connection);
    }

    private static LDAPConnection getConnection(Map<LdapConfigBean, LDAPConnectionPool> map,
            SSLSocketFactory socketFactory, LdapConfigBean config) throws LDAPException {
        if (!map.containsKey(config)) {
            LDAPConnection template = new LDAPConnection(config.getHost(), config.getPort());
            if (socketFactory != null)
                template.setSocketFactory(socketFactory);
            map.put(config, new LDAPConnectionPool(template, MAX_CONNECTIONS_PER_POOL));
        }
        return map.get(config).getConnection();
    }

    private static boolean isLdaps(String scheme) {
        return scheme.toLowerCase().startsWith("ldaps"); //$NON-NLS-1$
    }
}
