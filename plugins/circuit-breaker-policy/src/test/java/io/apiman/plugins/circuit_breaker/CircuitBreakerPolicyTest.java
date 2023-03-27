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

package io.apiman.plugins.circuit_breaker;

import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyFailureError;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author eric.wittmann@gmail.com
 */
@TestingPolicy(CircuitBreakerPolicy.class)
@SuppressWarnings("nls")
public class CircuitBreakerPolicyTest extends ApimanPolicyTest {

    /**
     * Test method for {@link io.apiman.plugins.circuit_breaker.CircuitBreakerPolicy#isMatch(int, java.lang.String)}.
     */
    @Test
    @Configuration("{}")
    public void testIsMatch() {
        Assert.assertTrue(CircuitBreakerPolicy.isMatch(404, "404"));
        Assert.assertTrue(CircuitBreakerPolicy.isMatch(404, "40*"));
        Assert.assertTrue(CircuitBreakerPolicy.isMatch(404, "4**"));
        Assert.assertTrue(CircuitBreakerPolicy.isMatch(404, "***"));

        Assert.assertTrue(CircuitBreakerPolicy.isMatch(500, "500"));
        Assert.assertTrue(CircuitBreakerPolicy.isMatch(500, "50*"));
        Assert.assertFalse(CircuitBreakerPolicy.isMatch(500, "400"));
        Assert.assertFalse(CircuitBreakerPolicy.isMatch(500, "501"));
        Assert.assertFalse(CircuitBreakerPolicy.isMatch(500, "4**"));
    }
    
    @Test
    @Configuration("{" + 
            "    \"errorCodes\" : [ \"5**\", \"4**\" ]," + 
            "    \"window\" : 1," + 
            "    \"limit\" : 5," + 
            "    \"reset\" : 2," + 
            "    \"failureCode\" : 503" + 
            "}")
    public void testTripAndReset() throws PolicyFailureError, Throwable {
        for (int iterations = 0; iterations < 2; iterations++) {
            PolicyTestRequest okRequest = PolicyTestRequest.build(PolicyTestRequestType.GET, "/path/to/resource");
            okRequest.header("Accept", "application/json");
            PolicyTestRequest errorRequest = PolicyTestRequest.build(PolicyTestRequestType.GET, "/path/to/resource");
            errorRequest.header("Accept", "application/json");
            errorRequest.header("X-Echo-ErrorCode", "500");
            
            PolicyTestResponse response;
            
            int numOK, numError;
            
            // Send a bunch of requests that return 200.  Everything should work fine.
            for (numOK = 0; numOK < 10; numOK++) {
                System.out.println(getClass().getName() + "::testTripAndReset:: OK request #" + (numOK+1));
                response = send(okRequest);
                Assert.assertEquals(200, response.code());
            }
    
            // Now send three that will respond with errors.
            for (numError = 0; numError < 3; numError++) {
                System.out.println(getClass().getName() + "::testTripAndReset:: ERROR request #" + (numError+1));
                response = send(errorRequest);
                Assert.assertEquals(500, response.code());
            }
            
            // The circuit was not yet tripped.  Try a couple of OK requests again.  Should work.
            for (; numOK < 12; numOK++) {
                System.out.println(getClass().getName() + "::testTripAndReset:: OK redux request #" + (numOK+1));
                response = send(okRequest);
                Assert.assertEquals(200, response.code());
            }
            
            // Now send two more failures.  The second one (making 5 within the time window) should trip the circuit.
            for (; numError < 5; numError++) {
                System.out.println(getClass().getName() + "::testTripAndReset:: ERROR request #" + (numError+1));
                response = send(errorRequest);
                Assert.assertEquals(500, response.code());
            }
    
            // Now that the circuit is tripped, the policy should fail with a 503
            try {
                response = send(okRequest);
            } catch (PolicyFailureError pf) {
                Assert.assertEquals(503, pf.getFailure().getResponseCode());
                Assert.assertEquals(20001, pf.getFailure().getFailureCode());
            }
            
            // Wait for the circuit reset time to elapse.
            Thread.sleep(2001);
    
            // Send a bunch more OK requests!  The circuit should now be reset.
            for (; numOK < 15; numOK++) {
                System.out.println(getClass().getName() + "::testTripAndReset:: OK request #" + (numOK+1));
                response = send(okRequest);
                Assert.assertEquals(200, response.code());
            }
        }
    }
    
}
