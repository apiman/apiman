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
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.ILdapSearch;
import io.apiman.gateway.engine.components.ldap.ILdapSearchEntry;
import io.apiman.gateway.engine.components.ldap.LdapSearchScope;
import io.apiman.gateway.engine.components.ldap.result.DefaultExceptionFactory;
import io.apiman.gateway.engine.components.ldap.result.LdapException;

import java.util.ArrayList;
import java.util.List;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class DefaultLdapSearchImpl implements ILdapSearch {
    private LDAPConnection connection;
    private IAsyncHandler<LdapException> ldapErrorHandler;
    private String searchDn;
    private String filter;
    private LdapSearchScope scope;

    public DefaultLdapSearchImpl(String searchDn, String filter, LdapSearchScope scope, LDAPConnection connection) {
        this.searchDn = searchDn;
        this.filter = filter;
        this.scope = scope;
        this.connection = connection;
    }

    private void getResults(String searchDn, String filter, LdapSearchScope scope,
            final IAsyncResultHandler<List<SearchResultEntry>> result) {
        try {
            SearchScope searchScope = (scope == LdapSearchScope.ONE) ? SearchScope.ONE : SearchScope.SUB;
            List<SearchResultEntry> searchResults = connection.search(searchDn, searchScope, filter).getSearchEntries();
            result.handle(AsyncResultImpl.create(searchResults));
        } catch (LDAPException e) {
            if (ldapErrorHandler == null) {
                // TODO wire in logger
                System.err.println("LDAP Error Handler not set. Error may be swallowed; this is probably not what you intended.");
            }
            ldapErrorHandler.handle(DefaultExceptionFactory.create(e));
        } catch (Exception e) {
            result.handle(AsyncResultImpl.<List<SearchResultEntry>>create(e));
        }
    }

    @Override
    public void search(final IAsyncResultHandler<List<ILdapSearchEntry>> resultHandler) {
        getResults(searchDn, filter, scope, (IAsyncResult<List<SearchResultEntry>> results) -> {
            if (results.isSuccess()) {
                List<ILdapSearchEntry> searchResults = toSearchEntry(results.getResult());
                resultHandler.handle(AsyncResultImpl.create(searchResults));
            } else {
                resultHandler.handle(AsyncResultImpl.<List<ILdapSearchEntry>>create(results.getError()));
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
    public ILdapSearch setLdapErrorHandler(IAsyncHandler<LdapException> handler) {
        this.ldapErrorHandler = handler;
        return this;
    }
}
