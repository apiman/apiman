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

import io.apiman.gateway.engine.components.ldap.result.LdapResultCode;
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
    public void bindPasswordSuccess() {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("secret");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 if (result.isError())
                     result.getError().printStackTrace(System.out);

                 context.assertTrue(result.isSuccess());
                 context.assertEquals(LdapResultCode.SUCCESS, result.getResult().getResultCode());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldFailWhenUidInvalid() {
        config.setBindDn("uid=tacos,ou=system");
        config.setBindPassword("secret");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 if (result.isError())
                     result.getError().printStackTrace(System.out);

                 context.assertTrue(result.isSuccess());
                 context.assertEquals(LdapResultCode.INVALID_CREDENTIALS, result.getResult().getResultCode());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldFailWhenOuInvalid() {
        config.setBindDn("uid=admin,ou=tacos");
        config.setBindPassword("secret");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 if (result.isError())
                     result.getError().printStackTrace(System.out);

                 context.assertTrue(result.isSuccess());
                 context.assertEquals(LdapResultCode.INVALID_CREDENTIALS, result.getResult().getResultCode());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }

    @Test
    public void shouldFailWhenPasswordInvalid() {
        config.setBindDn("uid=admin,ou=system");
        config.setBindPassword("miso-soup");

         TestCompletion completion = TestSuite.create("").test("",  context -> {
             Async async = context.async();

             ldapClientComponent.bind(config, result -> {
                 context.assertTrue(result.isSuccess());
                 context.assertEquals(LdapResultCode.INVALID_CREDENTIALS, result.getResult().getResultCode());
                 async.complete();
             });

             async.awaitSuccess();
        }).run();

        completion.awaitSuccess();
    }
}
