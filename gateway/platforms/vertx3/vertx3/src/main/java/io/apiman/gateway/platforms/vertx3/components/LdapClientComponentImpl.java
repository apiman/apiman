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

package io.apiman.gateway.platforms.vertx3.components;

import static io.apiman.gateway.platforms.vertx3.helpers.HandlerHelpers.translateFailureHandler;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ILdapComponent;
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.engine.impl.LDAPConnectionFactory;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.components.ldap.LdapClientConnectionImpl;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;

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
public class LdapClientComponentImpl implements ILdapComponent {

    private Vertx vertx;

    // This is likely a temporary measure
    private static SSLSocketFactory DEFAULT_SOCKET_FACTORY;

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

    public LdapClientComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> componentConfig) {
        this.vertx = vertx;
    }

    @Override
    public void connect(LdapConfigBean config, IAsyncResultHandler<ILdapClientConnection> handler) {
        LdapClientConnectionImpl connection = new LdapClientConnectionImpl(vertx, config, DEFAULT_SOCKET_FACTORY);

        connection.connect(result -> {
            if (result.isSuccess()) {
                handler.handle(AsyncResultImpl.create(connection));
            } else {
                handler.handle(AsyncResultImpl.create(result.getError()));
            }
        });
     }

    private <T> void handleBindReturn(Future<T> future, ResultCode resultCode, String message, IAsyncResultHandler<Boolean> handler) {
        if (resultCode.equals(ResultCode.SUCCESS)) {
            handler.handle(AsyncResultImpl.create(Boolean.TRUE));
            future.succeeded();
        } else if  (resultCode.equals(ResultCode.AUTHORIZATION_DENIED) || resultCode.equals(ResultCode.INVALID_CREDENTIALS)) {
            handler.handle(AsyncResultImpl.create(Boolean.FALSE));
            future.succeeded();
        } else {
            future.fail(String.format("LDAP failure: %s %s", resultCode, message)); //$NON-NLS-1$
        }
    }

    @Override
    public void bind(LdapConfigBean config, IAsyncResultHandler<Boolean> handler) {
        vertx.executeBlocking(future -> {
            try {
                //LDAPConnection connection = LDAPConnectionFactory.build(DEFAULT_SOCKET_FACTORY, config.getScheme(), config.getHost(), config.getPort());
                LDAPConnection connection = LDAPConnectionFactory.build(DEFAULT_SOCKET_FACTORY, config);
                BindResult bindResponse = connection.bind(config.getBindDn(), config.getBindPassword());
                handleBindReturn(future, bindResponse.getResultCode(), bindResponse.getDiagnosticMessage(), handler);
            } catch (LDAPException e) {
                handleBindReturn(future, e.getResultCode(), e.getDiagnosticMessage(), handler);
            }
        }, translateFailureHandler(handler));
    }

}
