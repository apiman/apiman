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

package io.apiman.gateway.platforms.vertx3.components.ldap;

import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.ILdapSearchEntry;
import io.apiman.gateway.engine.components.ldap.LdapSearchScope;
import io.apiman.gateway.engine.impl.DefaultLdapSearchImpl;
import io.vertx.core.Vertx;

import java.util.List;

import com.unboundid.ldap.sdk.LDAPConnection;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class LdapSearchImpl extends DefaultLdapSearchImpl {
    private Vertx vertx;

    public LdapSearchImpl(Vertx vertx, String searchDn, String filter, LdapSearchScope scope, LDAPConnection connection) {
        super(searchDn, filter, scope, connection);
        this.vertx = vertx;
    }

    @Override
    public void search(IAsyncResultHandler<List<ILdapSearchEntry>> result) {
        vertx.executeBlocking(blocking -> {
            System.out.println("Blocking request starting");
            super.search(result);
            System.out.println("Blocking request completed");
            blocking.complete();
        }, res -> {});
    }

}
