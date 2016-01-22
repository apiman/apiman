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

import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.components.ldap.result.LdapException;

/**
 * Represents an ongoing LDAP connection.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public interface ILdapClientConnection extends AutoCloseable {

    /**
     * Perform a simple LDAP search
     *
     * @param searchDn the search
     * @param filter the query filter
     * @param scope the scope
     * @param result the query results
     * @return ldap search ready to execute
     */
    ILdapSearch search(String searchDn, String filter, LdapSearchScope scope);

    /**
     * Closes the connection, with a handler response indicating success.
     *
     * @param result the result indicating success
     */
    void close(IAsyncResultHandler<Void> result);

    /**
     * Close the connection.
     */
    @Override
    void close();

    /**
     * Close a connection with the given exception, which may allow it to be reused.
     *
     * @param e the exception
     */
    void close(LdapException e);
}
