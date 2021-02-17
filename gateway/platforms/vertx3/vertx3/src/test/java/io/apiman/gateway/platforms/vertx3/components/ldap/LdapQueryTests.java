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

import io.apiman.gateway.engine.components.ldap.ILdapClientConnection;
import io.apiman.gateway.engine.components.ldap.ILdapSearchEntry;
import io.apiman.gateway.engine.components.ldap.LdapSearchScope;
import io.apiman.gateway.engine.components.ldap.result.LdapResultCode;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestCompletion;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.TestSuite;

import java.util.List;

import org.junit.After;
import org.junit.Test;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class LdapQueryTests extends LdapTestParent  {

    public ILdapClientConnection connection;

    @After
    public void after() {
        if (connection != null)
            connection.close();
    }

    @Test
    public void shouldConnectSuccessfully()  {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();
             ldapClientComponent.connect(config, connectionResult -> {
                 context.assertTrue(connectionResult.isSuccess());
                 connection = connectionResult.getResult();
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldCompleteSimpleQuery()  {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

        connect((connection, context) -> {
            Async async = context.async();
            connection.search("ou=people,o=apiman", "(uid=msavy)", LdapSearchScope.SUBTREE)
                 .setLdapErrorHandler(result -> context.fail(result.getCause()))
                 .search(searchResult -> {
                    context.assertTrue(searchResult.isSuccess());
                    List<ILdapSearchEntry> result = searchResult.getResult();
                    context.assertEquals(1, result.size());
                    async.complete();
                });
        });
    }

    @Test
    public void shouldCompleteMultipleSimpleQueries()  {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

        connect((connection, context) -> {
            Async async = context.async();
            Async async2 = context.async();
            connection.search("ou=people,o=apiman", "(uid=msavy)", LdapSearchScope.SUBTREE)
                 .setLdapErrorHandler(result -> context.fail(result.getCause()))
                 .search(searchResult -> {
                    context.assertTrue(searchResult.isSuccess());

                    List<ILdapSearchEntry> result = searchResult.getResult();
                    context.assertEquals(1, result.size());
                    async.complete();
                });

            connection.search("ou=people,o=apiman", "(uid=ewittman)", LdapSearchScope.SUBTREE)
                 .setLdapErrorHandler(result -> context.fail(result.getCause()))
                 .search(searchResult -> {
                    context.assertTrue(searchResult.isSuccess());

                    List<ILdapSearchEntry> result = searchResult.getResult();
                    context.assertEquals(1, result.size());
                    async2.complete();
                });
        });
    }

    @Test
    public void shouldReturnEmptyForUnmatchedFilter()  {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

        connect((connection, context) -> {
            Async async = context.async();
            connection.search("ou=people,o=apiman", "(uid=sushi)", LdapSearchScope.SUBTREE)
                 .setLdapErrorHandler(result -> context.fail(result.getCause()))
                 .search(searchResult -> {
                     context.assertTrue(searchResult.isSuccess());

                     List<ILdapSearchEntry> result = searchResult.getResult();
                     context.assertEquals(0, result.size());
                     async.complete();
                 });
        });
    }

    @Test
    public void shouldErrorIfSearchDnInvalid()  {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

        connect((connection, context) -> {
            Async async = context.async();
            connection.search("invalid", "(uid=msavy)", LdapSearchScope.SUBTREE)
            .setLdapErrorHandler(error -> {
                System.err.println(error.getResultCode());
                context.assertTrue(error.getResultCode().equals(LdapResultCode.INVALID_DN_SYNTAX));
                async.complete();
            })
            .search(searchResult -> {
                context.assertFalse(true); // Should not be executed
            });
        });
    }

    @Test
    public void shouldErrorIfSearchFilterInvalid()  {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

        connect((connection, context) -> {
            Async async = context.async();
            connection.search("ou=people,o=apiman", "!!!!", LdapSearchScope.SUBTREE)
            .setLdapErrorHandler(error -> {
                context.assertTrue(error.getResultCode().equals(LdapResultCode.FILTER_ERROR));
                async.complete();
            })
            .search(searchResult -> {
                context.assertFalse(true); // Should not be executed
            });
        });
    }

    private void connect(DoubleHandler<ILdapClientConnection, TestContext> handler) {
        TestCompletion completion = TestSuite.create("").test("",  context -> {
            Async async = context.async();
            ldapClientComponent.connect(config, connectionResult -> {
                context.assertTrue(connectionResult.isSuccess());
                connection = connectionResult.getResult();
                handler.handle(connection, context);
                async.complete();
            });
            async.awaitSuccess();
       }).run();
        completion.awaitSuccess();
    }

    interface DoubleHandler<X, Y> {
        void handle(X x, Y y);
    }
}
