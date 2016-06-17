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
package io.apiman.gateway.engine.policies;

import io.apiman.test.common.mock.EchoResponse;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@TestingPolicy(CachingPolicy.class)
@SuppressWarnings("nls")
public class CachingPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{" +
            "  \"ttl\" : 2" +
            "}")
    public void testCaching() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/cached-resource");

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Long counterValue = echo.getCounter();
        Assert.assertNotNull(counterValue);
        Assert.assertEquals("application/json", response.header("Content-Type"));

        // Now send the request again - we should get the *same* counter value!
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Long counterValue2 = echo.getCounter();
        Assert.assertNotNull(counterValue2);
        Assert.assertEquals(counterValue, counterValue2);
        Assert.assertEquals("application/json", response.header("Content-Type"));

        // One more time, just to be sure
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Long counterValue3 = echo.getCounter();
        Assert.assertNotNull(counterValue3);
        Assert.assertEquals(counterValue, counterValue3);
        Assert.assertEquals("application/json", response.header("Content-Type"));

        // Now wait for 3s and make sure the cache entry expired
        Thread.sleep(3000);
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Long counterValue4 = echo.getCounter();
        Assert.assertNotNull(counterValue4);
        Assert.assertNotEquals(counterValue, counterValue4);
        Assert.assertEquals("application/json", response.header("Content-Type"));

        // And again - should be re-cached
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        Long counterValue5 = echo.getCounter();
        Assert.assertNotNull(counterValue5);
        Assert.assertEquals(counterValue4, counterValue5);
        Assert.assertEquals("application/json", response.header("Content-Type"));
    }
}
