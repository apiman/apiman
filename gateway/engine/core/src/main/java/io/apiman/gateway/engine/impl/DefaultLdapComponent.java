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
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.ILdapResult;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.engine.components.ldap.result.DefaultExceptionFactory;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.unboundid.util.ssl.SSLUtil;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultLdapComponent implements ILdapComponent {

    public DefaultLdapComponent() {
    }

    protected static SSLSocketFactory DEFAULT_SOCKET_FACTORY;

    static {
        try {
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init((KeyStore) null);

            X509TrustManager trustManager = null;
            for (TrustManager tm : tmFactory.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    trustManager = (X509TrustManager) tm;
                    break;
                }
            }
            DEFAULT_SOCKET_FACTORY = new SSLUtil(trustManager).createSSLSocketFactory();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
    }

    @Override
    public void connect(LdapConfigBean config, final IAsyncResultHandler<ILdapClientConnection> handler) {
        final DefaultLdapClientConnection connection = new DefaultLdapClientConnection(config, DEFAULT_SOCKET_FACTORY);
        connection.connect(new IAsyncResultHandler<ILdapResult>() {

            @Override
            public void handle(IAsyncResult<ILdapResult> result) {
                if (result.isSuccess()) { // Could still be a non-success return
                    ILdapResult ldapResult = result.getResult();
                    if (ldapResult.getResultCode().isSuccess()) {
                        handler.handle(AsyncResultImpl.<ILdapClientConnection>create(connection));
                    } else { // We don't have any fine-grained handling of exceptions, so bundle all into one.
                        handler.handle(AsyncResultImpl.<ILdapClientConnection>create(DefaultExceptionFactory.create(ldapResult)));
                    }
                } else {
                    handler.handle(AsyncResultImpl.<ILdapClientConnection>create(result.getError()));
                }
            }
        });
    }

    @Override
    public void bind(LdapConfigBean config, IAsyncResultHandler<ILdapResult> handler) {
        DefaultLdapClientConnection.bind(DEFAULT_SOCKET_FACTORY, config, handler);
    }
}
