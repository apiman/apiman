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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.ILdapResult;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.engine.impl.DefaultLdapClientConnection;
import io.apiman.gateway.engine.impl.DefaultLdapComponent;
import io.apiman.gateway.platforms.vertx3.common.config.VertxEngineConfig;
import io.apiman.gateway.platforms.vertx3.components.ldap.LdapClientConnectionImpl;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.Map;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LdapClientComponentImpl extends DefaultLdapComponent {

    private Vertx vertx;

    public LdapClientComponentImpl(Vertx vertx, VertxEngineConfig engineConfig, Map<String, String> componentConfig) {
        this.vertx = vertx;
    }

    @Override
    public void connect(LdapConfigBean config, final IAsyncResultHandler<ILdapClientConnection> handler) {
        final LdapClientConnectionImpl connection = new LdapClientConnectionImpl(vertx, config, socketFactory);
        connection.connect(result -> {
                if (result.isSuccess()) {
                    handler.handle(AsyncResultImpl.create(connection));
                } else {
                    handler.handle(AsyncResultImpl.create(result.getError()));
                }
            });
    }

    @Override
    public void bind(LdapConfigBean config, IAsyncResultHandler<ILdapResult> handler) {
        vertx.executeBlocking((Promise<Object> blocking) -> {
            DefaultLdapClientConnection.bind(socketFactory, config, handler);
            blocking.complete();
        }, res -> {});
    }
}
