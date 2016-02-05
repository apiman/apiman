/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policies;

import java.util.HashSet;

import org.junit.Test;
import org.mockito.Mockito;

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * Unit test
 *
 * @author rubenrm1@gmail.com
 *
 */
@SuppressWarnings({ "nls" })
public class AuthorizationPolicyTest {

    @Test
    public void testApplySimple() {
        String json = "{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \".*\", \"role\" : \"role-1\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("role-1");
        doTest(json, userRoles, "GET", "/invoices/1", true);
    }

    @Test
    public void testApplyPath() {
        String json = "{\r\n" +
                " \"requestUnmatched\" : \"pass\"," +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/auth/.*\", \"role\" : \"the-role\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("other-role");
        doTest(json, userRoles, "GET", "/auth/my-items", false);
        doTest(json, userRoles, "GET", "/unauth/my-items", true);
    }

    @Test
    public void testApplyVerb() {
        String json = "{\r\n" +
                " \"requestUnmatched\" : \"pass\"," +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"PUT\", \"pathPattern\" : \"/auth/.*\", \"role\" : \"the-role\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("other-role");
        doTest(json, userRoles, "GET", "/auth/my-items", true);
        doTest(json, userRoles, "PUT", "/auth/my-items", false);
    }

    @Test
    public void testApplyMultiple() {
        String json = "{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/.*\", \"role\" : \"user\" },\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/admin/.*\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("user");
        doTest(json, userRoles, "GET", "/path/to/user/resource", true);
        doTest(json, userRoles, "GET", "/admin/path/to/admin/resource", false);

        userRoles.add("admin");
        doTest(json, userRoles, "GET", "/path/to/user/resource", true);
        doTest(json, userRoles, "GET", "/admin/path/to/admin/resource", true);
    }

    @Test
    public void testApplyNoneMatchedPass() {
        String json = "{\r\n" +
                " \"requestUnmatched\" : \"pass\"," +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"GET\", \"pathPattern\" : \"/user/.*\", \"role\" : \"user\" },\r\n" +
                "    { \"verb\" : \"GET\", \"pathPattern\" : \"/admin/.*\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();

        doTest(json, userRoles, "GET", "/other/resource", true);
        doTest(json, userRoles, "PUT", "/admin/resource", true);
    }

    @Test
    public void testApplyNoneMatchedFail() {
        String json = "{\r\n" +
                " \"requestUnmatched\" : \"fail\"," +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"GET\", \"pathPattern\" : \"/user/.*\", \"role\" : \"user\" },\r\n" +
                "    { \"verb\" : \"GET\", \"pathPattern\" : \"/admin/.*\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();

        doTest(json, userRoles, "GET", "/other/resource", false);
        doTest(json, userRoles, "POST", "/admin/resource", false);
    }

    @Test
    public void testApplyMultipleAnyMatch() {
        String json = "{\r\n" +
                " \"multiMatch\" : \"any\"," +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/.*\", \"role\" : \"user\" },\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/multi/.*\", \"role\" : \"role-1\" },\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/multi/.*\", \"role\" : \"role-2\" },\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/admin/.*\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("other-role");
        doTest(json, userRoles, "GET", "/multi/resource", false);

        userRoles.add("role-1");
        doTest(json, userRoles, "GET", "/multi/resource", true);

        userRoles.add("role-2");
        doTest(json, userRoles, "GET", "/multi/resource", true);
    }

    @Test
    public void testApplyMultipleAllMatch() {
        String json = "{\r\n" +
                " \"multiMatch\" : \"all\"," +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/multi/.*\", \"role\" : \"role-1\" },\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/multi/.*\", \"role\" : \"role-2\" },\r\n" +
                "    { \"verb\" : \"*\", \"pathPattern\" : \"/admin/.*\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}";

        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("other-role");
        doTest(json, userRoles, "GET", "/multi/resource", false);

        userRoles.add("role-1");
        doTest(json, userRoles, "GET", "/multi/resource", false);

        userRoles.add("role-2");
        doTest(json, userRoles, "GET", "/multi/resource", true);
    }



    private void doTest(String json, HashSet<String> userRoles, String verb, String path, boolean shouldSucceed) {
        AuthorizationPolicy policy = new AuthorizationPolicy();
        Object config = policy.parseConfiguration(json);

        ApiRequest request = new ApiRequest();
        request.setType(verb);
        request.setDestination(path);
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);

        Mockito.when(context.getAttribute(AuthorizationPolicy.AUTHENTICATED_USER_ROLES, (HashSet<String>) null)).thenReturn(userRoles);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(
                new IPolicyFailureFactoryComponent() {
                    @Override
                    public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                        failure.setFailureCode(failureCode);
                        failure.setType(type);
                        failure.setMessage(message);
                        return failure;
                    }
                });

        // Success
        policy.apply(request, context, config, chain);

        if (shouldSucceed) {
            Mockito.verify(chain).doApply(request);
        } else {
            Mockito.verify(chain).doFailure(failure);
        }
    }

}
