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

package io.apiman.plugins.soap_authorization_policy;

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

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;


/**
 * Unit test
 *
 * @author rubenrm1@gmail.com
 * @author rachel.yordan@redhat.com
 * @author tevo.souza@hotmail.com
 * Test the {@link SoapAuthorizationPolicy}.
 *
 */

@TestingPolicy(SoapAuthorizationPolicy.class)
@SuppressWarnings({ "nls" })
public class SoapAuthorizationPolicyTest extends ApimanPolicyTest {

    @Test
    @Configuration("{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"*\", \"role\" : \"role-1\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testSimple() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("role-1");

        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/invoices/1");
        request.header("SOAPAction", "reportIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
    }

    @Test
    @Configuration("{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"*\", \"role\" : \"role-1\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testNoSOAPHeader() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("role-1");

        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/invoices/1");

        try {
        	request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        	send(request);
        	Assert.fail("expected a failure response");
        } catch (PolicyFailureError failure) {
        	 Assert.assertNotNull(failure.getFailure());
        	 Assert.assertEquals(PolicyFailureType.Other, failure.getFailure().getType());
        }
    }

    @Test
    @Configuration("{\r\n" +
                " \"requestUnmatched\" : \"pass\"," +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"reportIncident\", \"role\" : \"the-role\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testAction() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("other-role");


        // Should Succeed
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/auth/my-items");
        request.header("SOAPAction", "closeIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);


        // Should Fail
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/auth/my-items");

        try {
            request.header("SOAPAction", "reportIncident");
            request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
            Assert.assertEquals(PolicyFailureType.Authorization, policyFailure.getType());
        }
    }

    @Test
    @Configuration("{\r\n" +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"reportIncident\", \"role\" : \"user\" },\r\n" +
                "    { \"action\" : \"resolveIncident\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testMultiple() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("user");

        // Should Succeed
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/path/to/user/resource");
        request.header("SOAPAction", "reportIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);


        // Should Fail
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/admin/path/to/admin/resource");

        try {
            request.header("SOAPAction", "resolveIncident");
            request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
            Assert.assertEquals(PolicyFailureType.Authorization, policyFailure.getType());
        }

        //

        userRoles.add("admin");

        // Should Succeed
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/path/to/user/resource");
        request.header("SOAPAction", "reportIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);


        // Should Succeed
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/admin/path/to/admin/resource");
        request.header("SOAPAction", "reportIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
    }

    @Test
    @Configuration("{\r\n" +
                " \"requestUnmatched\" : \"pass\"," +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"viewIncident\", \"role\" : \"user\" },\r\n" +
                "    { \"action\" : \"viewIncident\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testNoneMatchedPass() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();

        // Should Succeed
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/other/resource");
        request.header("SOAPAction", "resolveIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);


        // Should Succeed
        request = PolicyTestRequest.build(PolicyTestRequestType.PUT, "/admin/resource");
        request.header("SOAPAction", "closeIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
    }

    @Test
    @Configuration("{\r\n" +
                " \"requestUnmatched\" : \"fail\"," +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"reportIncident\", \"role\" : \"user\" },\r\n" +
                "    { \"action\" : \"reportIncident\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testNoneMatchedFail() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();

        // Should Fail
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/other/resource");

        try {
            request.header("SOAPAction", "reportIncident");
            request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
            Assert.assertEquals(PolicyFailureType.Authorization, policyFailure.getType());
        }


        // Should Fail
        request = PolicyTestRequest.build(PolicyTestRequestType.POST, "/admin/resource");

        try {
            request.header("SOAPAction", "reportIncident");
            request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
        }
    }

    @Test
    @Configuration("{\r\n" +
                " \"multiMatch\" : \"any\"," +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"*\", \"role\" : \"user\" },\r\n" +
                "    { \"action\" : \"*\", \"role\" : \"role-1\" },\r\n" +
                "    { \"action\" : \"*\", \"role\" : \"role-2\" },\r\n" +
                "    { \"action\" : \"*\", \"role\" : \"admin\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testMultipleAnyMatch() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("other-role");

        // Should Fail
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/multi/resource");

        try {
            request.header("SOAPAction", "reportIncident");
            request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
        }


        userRoles.add("role-1");

        // Should Succeed
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/multi/resource");
        request.header("SOAPAction", "reportIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);


        userRoles.add("role-2");

        // Should Succeed
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/multi/resource");
        request.header("SOAPAction", "reportIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
        response = send(request);
        echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
    }

    @Test
    @Configuration("{\r\n" +
                " \"multiMatch\" : \"all\"," +
                "  \"rules\" : [\r\n" +
                "    { \"action\" : \"reportIncident\", \"role\" : \"role-1\" },\r\n" +
                "    { \"action\" : \"reportIncident\", \"role\" : \"role-2\" }\r\n" +
                "  ]\r\n" +
                "}")
    public void testMultipleAllMatch() throws Throwable {
        HashSet<String> userRoles = new HashSet<>();
        userRoles.add("other-role");

        // Should Fail
        PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/multi/resource");

        try {
            request.header("SOAPAction", "reportIncident");
            request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
            Assert.assertEquals(PolicyFailureType.Authorization, policyFailure.getType()); // Expected <Authorization> but was <Other>
        }

        userRoles.add("role-1");

        // Should Fail
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/multi/resource");

        try {
            request.header("SOAPAction", "reportIncident");
            request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);
            send(request);
            Assert.fail("Expected a failure response!");
        } catch (PolicyFailureError failure) {
            PolicyFailure policyFailure = failure.getFailure();
            Assert.assertNotNull(policyFailure);
            Assert.assertEquals(PolicyFailureType.Authorization, policyFailure.getType());
        }

        userRoles.add("role-2");

        // Should Succeed
        request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/multi/resource");
        request.header("SOAPAction", "reportIncident");
        request.contextAttribute(SoapAuthorizationPolicy.AUTHENTICATED_USER_ROLES, userRoles);

        PolicyTestResponse response = send(request);
        EchoResponse echo = response.entity(EchoResponse.class);
        Assert.assertNotNull(echo);
    }

}
