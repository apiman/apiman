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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.apiman.gateway.engine.policies.config.AuthorizationConfig;
import io.apiman.gateway.engine.policies.config.AuthorizationRule;
import io.apiman.gateway.engine.policies.config.MultipleMatchType;
import io.apiman.gateway.engine.policies.config.UnmatchedRequestType;

/**
 * Unit test
 *
 * @author rubenrm1@gmail.com
 *
 */
@SuppressWarnings({ "nls" })
public class AuthorizationPolicyConfigTest {

    @Test
    public void testParseConfiguration() {
        AuthorizationPolicy policy = new AuthorizationPolicy();

        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(AuthorizationConfig.class, parsed.getClass());

        AuthorizationConfig parsedConfig = (AuthorizationConfig) parsed;
        Assert.assertNotNull(parsedConfig.getRules());
        Assert.assertTrue(parsedConfig.getRules().isEmpty());

        // Single Path
        config = "{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"verb\" : \"GET\", \"pathPattern\" : \".*\", \"role\" : \"role-1\" }\r\n" +
                "  ]\r\n" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (AuthorizationConfig) parsed;
        Assert.assertNotNull(parsedConfig.getRules());
        AuthorizationRule rule1 = new AuthorizationRule();
        rule1.setVerb("GET");
        rule1.setPathPattern(".*");
        rule1.setRole("role-1");
        List<AuthorizationRule> expectedConfiguration = Arrays.asList(rule1);
        Assert.assertEquals(expectedConfiguration, parsedConfig.getRules());

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
        Assert.assertNotNull(parsedConfig.getRules());
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
        Assert.assertEquals(expectedConfiguration, parsedConfig.getRules());


        // Boolean flags
        config = "{\r\n" +
                "    \"requestUnmatched\" : \"pass\",\r\n" +
                "    \"multiMatch\" : \"any\"\r\n" +
                "}\r\n" +
                "";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (AuthorizationConfig) parsed;
        Assert.assertEquals(Collections.emptyList(), parsedConfig.getRules());
        Assert.assertEquals(MultipleMatchType.any, parsedConfig.getMultiMatch());
        Assert.assertEquals(UnmatchedRequestType.pass, parsedConfig.getRequestUnmatched());
    }

}
