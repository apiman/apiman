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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.apiman.manager.test.server.MockGatewayServlet;
import io.apiman.manager.test.util.AbstractTestPlanTest;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * Runs the "services" test plan.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServicesTest extends AbstractTestPlanTest {

    private static final String EXPECTED_GATEWAY_LOG =
            "GET:/mock-gateway/system/status\n" +  //$NON-NLS-1$
            "PUT:/mock-gateway/services\n"; //$NON-NLS-1$

    private static final String EXPECTED_PAYLOAD =
            "{\"publicService\":false,\"organizationId\":\"Organization1\",\"serviceId\":\"Service1\",\"version\":\"1.0\",\"endpointType\":\"rest\",\"endpoint\":\"http://localhost:8080/ping\",\"endpointProperties\":{\"foo\":\"foo-value\",\"bar\":\"bar-value\"},\"servicePolicies\":[]}"; //$NON-NLS-1$

    @Test
    public void test() throws IOException {
        runTestPlan("test-plans/services-testPlan.xml", ServicesTest.class.getClassLoader()); //$NON-NLS-1$

        // This test includes publishing of a service to the gateway REST API.  The
        // test framework incldues a mock gateway API to test that the REST calls were
        // properly make.  Here is where we assert the result.
        String actualGatewayLog = MockGatewayServlet.getRequestLog();
        Assert.assertEquals(EXPECTED_GATEWAY_LOG, actualGatewayLog);

        String payload = MockGatewayServlet.getPayloads().get(1);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expected = mapper.readTree(EXPECTED_PAYLOAD);
        JsonNode actual = mapper.readTree(payload.trim());
        Assert.assertTrue(expected.equals(actual));

    }

}
