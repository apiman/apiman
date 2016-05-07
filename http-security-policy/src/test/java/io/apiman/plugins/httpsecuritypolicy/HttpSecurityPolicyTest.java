/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.plugins.httpsecuritypolicy;

import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
@TestingPolicy(HttpSecurityPolicy.class)
@SuppressWarnings("nls")
public class HttpSecurityPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{\n" +
            "\"hsts\":\n" +
            "{ \"enabled\" : false, \"includeSubdomains\" : true, \"maxAge\" : 13, \"preload\" : true }\n" +
            ",\n" +
            "\"contentSecurityPolicy\":\n" +
            "{ \"mode\" : \"ENABLED\", \"csp\" : \"script-src 'self' https://apiman.io\" }\n" +
            ",\n" +
            "\"frameOptions\" : \"DENY\",\n" +
            "\"xssProtection\" : \"ON\",\n" +
            "\"contentTypeOptions\" : true\n" +
            "}")
    public void test() throws Throwable {
        PolicyTestResponse response = send(PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource"));
        Set<Entry<String, String>> expected = expected(
                ent("Content-Security-Policy", "script-src 'self' https://apiman.io"),
                ent("X-Content-Type-Options", "nosniff"),
                ent("X-XSS-Protection", "1"),
                ent("X-Frame-Options", "DENY")
                );

        Set<Entry<String, String>> actual = toSet(response.headers().getEntries());
        Assert.assertTrue(actual.containsAll(expected));
    }

    private Entry<String, String> ent(String k, String v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    @SafeVarargs
    private static <T> Set<T> expected(T... entries) {
        return new HashSet<>(Arrays.asList(entries));
    }

    private static <T> Set<T> toSet(List<T> list) {
        return new HashSet<>(list);
    }
}
