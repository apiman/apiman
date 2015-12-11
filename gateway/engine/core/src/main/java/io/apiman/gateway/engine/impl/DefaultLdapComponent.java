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
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
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
        connection.connect(new IAsyncResultHandler<Void>() {

            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    handler.handle(AsyncResultImpl.<ILdapClientConnection>create(connection));
                } else {
                    handler.handle(AsyncResultImpl.<ILdapClientConnection>create(result.getError()));
                }
            }
        });
    }

    private <T> void handleBindReturn(ResultCode resultCode, String message, IAsyncResultHandler<Boolean> handler) {
        if (resultCode.equals(ResultCode.SUCCESS)) {
            handler.handle(AsyncResultImpl.create(Boolean.TRUE));
        } else if  (resultCode.equals(ResultCode.AUTHORIZATION_DENIED) || resultCode.equals(ResultCode.INVALID_CREDENTIALS)) {
            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
        } else {
            RuntimeException re = new RuntimeException(String.format("LDAP failure: %s %s", resultCode, message)); //$NON-NLS-1$
            handler.handle(AsyncResultImpl.<Boolean>create(re));
        }
    }

    @Override
    public void bind(LdapConfigBean config, IAsyncResultHandler<Boolean> handler) {
        LDAPConnection connection = null;
        try {
            connection = LDAPConnectionFactory.build(DEFAULT_SOCKET_FACTORY, config);
            BindResult bindResponse = connection.bind(config.getBindDn(), config.getBindPassword());
            handleBindReturn(bindResponse.getResultCode(), bindResponse.getDiagnosticMessage(), handler);
        } catch (LDAPException e) { // generally errors as an exception, also potentially normal return(!).
            handleBindReturn(e.getResultCode(), e.getDiagnosticMessage(), handler);
        } finally {
            if (connection != null)
                LDAPConnectionFactory.releaseConnection(connection);
        }
    }

}
