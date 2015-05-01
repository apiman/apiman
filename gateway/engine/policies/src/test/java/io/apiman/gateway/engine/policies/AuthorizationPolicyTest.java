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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.AuthorizationConfig;
import io.apiman.gateway.engine.policies.config.AuthorizationRule;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test
 *
 * @author rubenrm1@gmail.com
 *
 */
@SuppressWarnings({ "nls" })
public class AuthorizationPolicyTest {

    @Test
    public void testParseConfiguration() {
        AuthorizationPolicy policy = new AuthorizationPolicy();

        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        assertNotNull(parsed);
        assertEquals(AuthorizationConfig.class, parsed.getClass());

        AuthorizationConfig parsedConfig = (AuthorizationConfig) parsed;
        assertNotNull(parsedConfig.getRules());
        assertTrue(parsedConfig.getRules().isEmpty());

        // Single Path
        config = "{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"GET\", \"pathPattern\" : \".*\", \"role\" : \"role-1\" }\r\n" +
                "  ]\r\n" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (AuthorizationConfig) parsed;
        assertNotNull(parsedConfig.getRules());
        AuthorizationRule rule1 = new AuthorizationRule();
        rule1.setVerb("GET");
        rule1.setPathPattern(".*");
        rule1.setRole("role-1");
        List<AuthorizationRule> expectedConfiguration = Arrays.asList(rule1);
        assertEquals(expectedConfiguration, parsedConfig.getRules());

        // Multiple Paths
        config = "{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"GET\", \"pathPattern\" : \".*\", \"role\" : \"role-1\" },\r\n" +
                "    { \"verb\" : \"PUT\", \"pathPattern\" : \".+\", \"role\" : \"role-2\" },\r\n" +
                "    { \"verb\" : \"POST\", \"pathPattern\" : \"(.*)\", \"role\" : \"role-3\" }\r\n" +
                "  ]\r\n" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (AuthorizationConfig) parsed;
        assertNotNull(parsedConfig.getRules());
        expectedConfiguration = new ArrayList<>();
        AuthorizationRule rule2 = new AuthorizationRule();
        rule2.setVerb("PUT");
        rule2.setPathPattern(".+");
        rule2.setRole("role-2");
        AuthorizationRule rule3 = new AuthorizationRule();
        rule3.setVerb("POST");
        rule3.setPathPattern("(.*)");
        rule3.setRole("role-3");
        expectedConfiguration.add(rule1);
        expectedConfiguration.add(rule2);
        expectedConfiguration.add(rule3);

        assertEquals(expectedConfiguration, parsedConfig.getRules());
    }

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


    private void doTest(String json, HashSet<String> userRoles, String verb, String path, boolean shouldSucceed) {
        AuthorizationPolicy policy = new AuthorizationPolicy();
        Object config = policy.parseConfiguration(json);

        ServiceRequest request = new ServiceRequest();
        request.setType(verb);
        request.setDestination(path);
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ServiceRequest> chain = Mockito.mock(IPolicyChain.class);

        Mockito.when(context.getAttribute(AuthorizationPolicy.AUTHENTICATED_USER_ROLES, new HashSet<String>())).thenReturn(userRoles);
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
