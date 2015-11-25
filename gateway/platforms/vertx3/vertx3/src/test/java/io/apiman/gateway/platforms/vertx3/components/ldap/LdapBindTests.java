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

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestCompletion;
import io.vertx.ext.unit.TestSuite;

import org.junit.Test;

/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@SuppressWarnings("nls")
public class LdapBindTests extends LdapTestParent {

    @Test
    public void bindPasswordSuccess() throws InterruptedException {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 if (result.isError())
                     result.getError().printStackTrace(System.out);

                 context.assertTrue(result.isSuccess());
                 context.assertTrue(result.getResult());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldFailWhenUidInvalid() throws InterruptedException {
        config.setBindDn("uid=tacos,ou=system");
        config.setBindPassword("secret");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 if (result.isError())
                     result.getError().printStackTrace(System.out);

                 context.assertTrue(result.isSuccess());
                 context.assertFalse(result.getResult());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldFailWhenOuInvalid() throws InterruptedException {
        config.setBindDn("uid=admin,ou=tacos");
        config.setBindPassword("secret");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 if (result.isError())
                     result.getError().printStackTrace(System.out);

                 context.assertTrue(result.isSuccess());
                 context.assertFalse(result.getResult());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldFailWhenPasswordInvalid() throws InterruptedException {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("miso-soup");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 if (result.isError())
                     result.getError().printStackTrace(System.out);

                 context.assertTrue(result.isSuccess());
                 context.assertFalse(result.getResult());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldErrorWhenFieldsInvalid() throws InterruptedException {
        config.setBindDn("x");
        config.setBindPassword("x");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 context.assertTrue(result.isError());
                 context.assertEquals("LDAP failure: 34 (invalid DN syntax) Incorrect DN given : x (0x78 ) is invalid",
                         result.getError().getMessage());

                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

}
