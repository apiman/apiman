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
import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.ILdapSearchEntry;
import io.apiman.gateway.engine.components.ldap.LdapConfigBean;
import io.apiman.gateway.engine.components.ldap.LdapSearchScope;

import java.util.ArrayList;
import java.util.List;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

public class DefaultLdapClientConnection implements ILdapClientConnection {
        private LdapConfigBean config;
        private LDAPConnection connection;
        private boolean closed;

        public DefaultLdapClientConnection(LdapConfigBean config) {
            this.config = config;
        }

        public void connect(final IAsyncResultHandler<Void> resultHandler) {
            try {
                this.connection = new LDAPConnection(config.getHost(), config.getPort(), config.getBindDn(), config.getBindPassword());
                resultHandler.handle(AsyncResultImpl.create((Void) null));
            } catch (LDAPException e) {
                resultHandler.handle(AsyncResultImpl.<Void>create(e));
            }
        }

        private void getResults(String searchDn, String filter, LdapSearchScope scope, final IAsyncResultHandler<List<SearchResultEntry>> result) {
            if (connection.isConnected()) {
                try {
                    SearchScope searchScope = (scope == LdapSearchScope.ONE) ? SearchScope.ONE : SearchScope.SUB;
                    List<SearchResultEntry> searchResults = connection.search(searchDn, searchScope, filter).getSearchEntries();
                    result.handle(AsyncResultImpl.create(searchResults));
                } catch (Exception e) {
                    result.handle(AsyncResultImpl.<List<SearchResultEntry>>create(e));
                }
            } else {
                throw new IllegalStateException("Not connected to LDAP server"); //$NON-NLS-1$
            }
        }

        @Override
        public void search(String searchDn, String filter, LdapSearchScope scope, final IAsyncResultHandler<List<ILdapSearchEntry>> resultHandler) {
            getResults(searchDn, filter, scope, new IAsyncResultHandler<List<SearchResultEntry>>() {

                @Override
                public void handle(IAsyncResult<List<SearchResultEntry>> results) {
                    if (results.isSuccess()) {
                        List<ILdapSearchEntry> searchResults = toSearchEntry(results.getResult());
                        resultHandler.handle(AsyncResultImpl.create(searchResults));
                    } else {
                        resultHandler.handle(AsyncResultImpl.<List<ILdapSearchEntry>>create(results.getError()));
                    }
                }
            });
        }

        private List<ILdapSearchEntry> toSearchEntry(List<SearchResultEntry> result) {
            List<ILdapSearchEntry> ldapSearchEntries = new ArrayList<>(result.size());

            for (SearchResultEntry e : result) {
                ldapSearchEntries.add(new DefaultLdapSearchEntry(e));
            }
            return ldapSearchEntries;
        }

        @Override
        public void close() {
            if (!closed)
                connection.close();
            closed = true;
        }

        @Override
        public void close(IAsyncResultHandler<Void> closeResultHandler) {
            if (!closed)
                connection.close();
            closed = true;
            closeResultHandler.handle(AsyncResultImpl.create((Void) null));
        }
    }