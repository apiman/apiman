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
package org.overlord.apiman.dt.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.overlord.apiman.dt.test.server.MockGatewayServlet;
import org.overlord.apiman.dt.test.util.AbstractTestPlanTest;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Policy;

/**
 * Runs the "publishing" test plan.
 *
 * @author eric.wittmann@redhat.com
 */
public class PublishingTest extends AbstractTestPlanTest {

    private static final String EXPECTED_GATEWAY_LOG = 
            "GET:/mock-gateway/api/system/status\n" +  //$NON-NLS-1$
            "PUT:/mock-gateway/api/services\n" + //$NON-NLS-1$
            "GET:/mock-gateway/api/system/status\n" +  //$NON-NLS-1$
            "PUT:/mock-gateway/api/applications\n"; //$NON-NLS-1$
    
    private static final String EXPECTED_PUBLISH_PAYLOAD = 
            "{\"organizationId\":\"Organization1\",\"serviceId\":\"Service1\",\"version\":\"1.0\",\"endpointType\":\"rest\",\"endpoint\":\"http://localhost:8080/ping\",\"endpointProperties\":{}}"; //$NON-NLS-1$

    @Test
    public void test() throws JsonParseException, JsonMappingException, UnsupportedEncodingException, IOException {
        runTestPlan("test-plans/publishing-testPlan.xml", PublishingTest.class.getClassLoader()); //$NON-NLS-1$

        // This test includes publishing of a service to the gateway REST API.  The
        // test framework incldues a mock gateway API to test that the REST calls were
        // properly make.  Here is where we assert the result.
        String actualGatewayLog = MockGatewayServlet.getRequestLog();
        Assert.assertEquals(EXPECTED_GATEWAY_LOG, actualGatewayLog);
        
        String publishServicePayload = MockGatewayServlet.getPayloads().get(1);
        Assert.assertEquals(EXPECTED_PUBLISH_PAYLOAD, publishServicePayload.trim());
        String registerAppPayload = MockGatewayServlet.getPayloads().get(3);
        
        ObjectMapper mapper = new ObjectMapper();
        Application app = mapper.readValue(registerAppPayload.getBytes("UTF-8"), Application.class); //$NON-NLS-1$
        Assert.assertNotNull(app);
        Assert.assertEquals(app.getOrganizationId(), "Organization1"); //$NON-NLS-1$
        Assert.assertEquals(app.getApplicationId(), "Application1"); //$NON-NLS-1$
        Assert.assertEquals(app.getVersion(), "1.0"); //$NON-NLS-1$
        Assert.assertEquals(app.getContracts().size(), 1);
        Contract contract = app.getContracts().iterator().next();
        Assert.assertNotNull(contract);
        Assert.assertEquals(contract.getServiceOrgId(), "Organization1"); //$NON-NLS-1$
        Assert.assertEquals(contract.getServiceId(), "Service1"); //$NON-NLS-1$
        Assert.assertEquals(contract.getServiceVersion(), "1.0"); //$NON-NLS-1$
        List<Policy> policies = contract.getPolicies();
        Assert.assertEquals(policies.size(), 2);

        Policy policy2 = policies.get(0);
        Assert.assertNotNull(policy2);
        Assert.assertEquals(policy2.getPolicyImpl(), "org.example.PolicyDefTwo"); //$NON-NLS-1$
        Assert.assertEquals(policy2.getPolicyJsonConfig(), "{ 'foo' : 'bar' }"); //$NON-NLS-1$
        
        Policy policy1 = policies.get(1);
        Assert.assertNotNull(policy1);
        Assert.assertEquals(policy1.getPolicyImpl(), "org.example.PolicyDefOne"); //$NON-NLS-1$
        Assert.assertEquals(policy1.getPolicyJsonConfig(), "{ 'kung' : 'foo' }"); //$NON-NLS-1$
    }

}
