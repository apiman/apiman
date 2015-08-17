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

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.test.common.mock.EchoResponse;
import io.apiman.test.policies.ApimanPolicyTest;
import io.apiman.test.policies.Configuration;
import io.apiman.test.policies.PolicyFailureError;
import io.apiman.test.policies.PolicyTestRequest;
import io.apiman.test.policies.PolicyTestRequestType;
import io.apiman.test.policies.PolicyTestResponse;
import io.apiman.test.policies.TestingPolicy;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@TestingPolicy(BasicAuthenticationPolicy.class)
@SuppressWarnings("nls")
public class BasicAuthenticationPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{\r\n" +
            "    \"realm\" : \"TestRealm\",\r\n" +
            "    \"forwardIdentityHttpHeader\" : \"X-Authenticated-Identity\",\r\n" +
            "    \"staticIdentity\" : {\r\n" +
            "      \"identities\" : [\r\n" +
            "        { \"username\" : \"ckent\", \"password\" : \"ckent123!\" },\r\n" +
            "        { \"username\" : \"bwayne\", \"password\" : \"bwayne123!\" },\r\n" +
            "        { \"username\" : \"dprince\", \"password\" : \"dprince123!\" }\r\n" +
            "      ]\r\n" +
            "    }\r\n" +
            "}")
    public void testStatic() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");

        // Failure
        try {
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
            Assert.assertEquals(PolicyFailureType.Authentication, policyFailure.getType());
            Assert.assertEquals(10004, policyFailure.getFailureCode());
        }

        // Failure
        try {
            request.header("Authorization", createBasicAuthorization("ckent", "invalid_password"));
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
            Assert.assertEquals(PolicyFailureType.Authentication, policyFailure.getType());
            Assert.assertEquals(10003, policyFailure.getFailureCode());
        }

        // Success
        request.header("Authorization", createBasicAuthorization("ckent", "ckent123!"));
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        String header = echo.getHeaders().get("X-Authenticated-Identity");
        Assert.assertNotNull(header);
        Assert.assertEquals("ckent", header);
    }

    @Test
    @Configuration("{\r\n" +
            "    \"realm\" : \"TestRealm\",\r\n" +
            "    \"requireBasicAuth\" : false,\r\n" +
            "    \"staticIdentity\" : {\r\n" +
            "      \"identities\" : [\r\n" +
            "        { \"username\" : \"ckent\", \"password\" : \"ckent123!\" },\r\n" +
            "        { \"username\" : \"bwayne\", \"password\" : \"bwayne123!\" },\r\n" +
            "        { \"username\" : \"dprince\", \"password\" : \"dprince123!\" }\r\n" +
            "      ]\r\n" +
            "    }\r\n" +
            "}")
    public void testBasicAuthNotRequired() throws Throwable {
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
        String header = echo.getHeaders().get("X-Authenticated-Identity");
        Assert.assertNull(header);
    }

    /**
     * Creates the http Authorization string for the given credentials.
     * @param username
     * @param password
     */
    private String createBasicAuthorization(String username, String password) {
        String creds = username + ":" + password;
        StringBuilder builder = new StringBuilder();
        builder.append("Basic ");
        builder.append(Base64.encodeBase64String(creds.getBytes()));
        return builder.toString();
    }
}
