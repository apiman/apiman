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
package io.apiman.manager.test;

import io.apiman.manager.test.junit.RestTestGatewayLog;
import io.apiman.manager.test.junit.RestTestPlan;
import io.apiman.manager.test.junit.RestTestPublishPayload;
import io.apiman.manager.test.junit.RestTester;

import org.junit.runner.RunWith;

/**
 * Runs the "publishing" test plan.
 *
 * @author eric.wittmann@redhat.com
 */
@RunWith(RestTester.class)
@RestTestPlan("test-plans/publishing-testPlan.xml")
@RestTestGatewayLog(
        "GET:/mock-gateway/system/status\n" +
        "PUT:/mock-gateway/services\n" +
        "GET:/mock-gateway/system/status\n" +
        "PUT:/mock-gateway/applications\n" +
        "GET:/mock-gateway/system/status\n" +
        "DELETE:/mock-gateway/applications/Organization1/Application1/1.0\n" +
        "GET:/mock-gateway/system/status\n" +
        "DELETE:/mock-gateway/services/Organization1/Service1/1.0\n"
  )
@RestTestPublishPayload({
    "",
    "{\"publicService\":false,\"organizationId\":\"Organization1\",\"serviceId\":\"Service1\",\"version\":\"1.0\",\"endpointType\":\"rest\",\"endpoint\":\"http://localhost:8080/ping\",\"endpointProperties\":{},\"servicePolicies\":[]}",
    "",
    "{\"organizationId\":\"Organization1\",\"applicationId\":\"Application1\",\"version\":\"1.0\",\"contracts\":[{\"serviceOrgId\":\"Organization1\",\"serviceId\":\"Service1\",\"serviceVersion\":\"1.0\",\"plan\":\"Plan1\",\"policies\":[{\"policyJsonConfig\":\"{ 'foo' : 'bar' }\",\"policyImpl\":\"org.example.PolicyDefTwo\"},{\"policyJsonConfig\":\"{ 'kung' : 'foo' }\",\"policyImpl\":\"org.example.PolicyDefOne\"}]}]}"
})
public class PublishingTest {
//
//    @Test
//    public void test() throws JsonParseException, JsonMappingException, UnsupportedEncodingException, IOException {
//        runTestPlan("test-plans/publishing-testPlan.xml", PublishingTest.class.getClassLoader());
//
//        // This test includes publishing of a service to the gateway REST API.  The
//        // test framework incldues a mock gateway API to test that the REST calls were
//        // properly make.  Here is where we assert the result.
//        String actualGatewayLog = MockGatewayServlet.getRequestLog();
//        Assert.assertEquals(EXPECTED_GATEWAY_LOG, actualGatewayLog);
//
//        String publishServicePayload = MockGatewayServlet.getPayloads().get(1);
//        Assert.assertEquals(EXPECTED_PUBLISH_PAYLOAD, publishServicePayload.trim());
//        String registerAppPayload = MockGatewayServlet.getPayloads().get(3);
//
//        ObjectMapper mapper = new ObjectMapper();
//        Application app = mapper.readValue(registerAppPayload.getBytes("UTF-8"), Application.class);
//        Assert.assertNotNull(app);
//        Assert.assertEquals(app.getOrganizationId(), "Organization1");
//        Assert.assertEquals(app.getApplicationId(), "Application1");
//        Assert.assertEquals(app.getVersion(), "1.0");
//        Assert.assertEquals(app.getContracts().size(), 1);
//        Contract contract = app.getContracts().iterator().next();
//        Assert.assertNotNull(contract);
//        Assert.assertEquals(contract.getServiceOrgId(), "Organization1");
//        Assert.assertEquals(contract.getServiceId(), "Service1");
//        Assert.assertEquals(contract.getServiceVersion(), "1.0");
//        List<Policy> policies = contract.getPolicies();
//        Assert.assertEquals(policies.size(), 2);
//
//        Policy policy2 = policies.get(0);
//        Assert.assertNotNull(policy2);
//        Assert.assertEquals(policy2.getPolicyImpl(), "org.example.PolicyDefTwo");
//        Assert.assertEquals(policy2.getPolicyJsonConfig(), "{ 'foo' : 'bar' }");
//
//        Policy policy1 = policies.get(1);
//        Assert.assertNotNull(policy1);
//        Assert.assertEquals(policy1.getPolicyImpl(), "org.example.PolicyDefOne");
//        Assert.assertEquals(policy1.getPolicyJsonConfig(), "{ 'kung' : 'foo' }");
//    }

}
