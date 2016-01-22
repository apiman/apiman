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

package io.apiman.gateway.engine.components.ldap;

import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.result.LdapException;

import java.util.List;

/**
 * Perform an LDAP Search
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface ILdapSearch {
    /**
     * Handle LDAP Exceptions, for instance authorization and authentication.
     *
     * @param handler the exception handler
     * @return fluent
     */
    ILdapSearch setLdapErrorHandler(IAsyncHandler<LdapException> handler);

    /**
     * Search LDAP, which will return a list of one or more entries. In the case of
     * LDAP-related errors {@link #setLdapErrorHandler(IAsyncHandler)} will be invoked.
     * Unexpected exceptions will funnel through this handler.
     *
     * @param result a handler receiving a list of search entries.
     */
    void search(IAsyncResultHandler<List<ILdapSearchEntry>> result);
}
