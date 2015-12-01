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

import java.util.Map;

import com.unboundid.ldap.sdk.BindResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

public class DefaultLdapComponent implements ILdapComponent {

    public DefaultLdapComponent(Map<String, String> componentConfig) {
    }

    @Override
    public void connect(LdapConfigBean config, final IAsyncResultHandler<ILdapClientConnection> handler) {
        DefaultLdapClientConnection connection = new DefaultLdapClientConnection(config);
        connection.connect(new IAsyncResultHandler<Void>() {

            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    handler.handle(AsyncResultImpl.create(connection));
                } else {
                    handler.handle(AsyncResultImpl.create(result.getError()));
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
            handler.handle(AsyncResultImpl.create(re));
        }
    }

    @Override
    public void bind(LdapConfigBean config, IAsyncResultHandler<Boolean> handler) {
        try {
            LDAPConnection connection = new LDAPConnection(config.getHost(), config.getPort());
            BindResult bindResponse = connection.bind(config.getBindDn(), config.getBindPassword());
            handleBindReturn(bindResponse.getResultCode(), bindResponse.getDiagnosticMessage(), handler);
        } catch (LDAPException e) { // generally errors as an exception, also potentially normal return(!).
            handleBindReturn(e.getResultCode(), e.getDiagnosticMessage(), handler);
        }
    }

}
