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

import io.apiman.manager.test.junit.ManagerRestTestGatewayLog;
import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTestPublishPayload;
import io.apiman.manager.test.junit.ManagerRestTester;

import org.junit.runner.RunWith;

/**
 * Runs the "publishing" test plan.
 *
 * @author eric.wittmann@redhat.com
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/publishing-testPlan.xml")
@ManagerRestTestGatewayLog(
        "GET:/mock-gateway/system/status\n" +
        "PUT:/mock-gateway/apis\n" +
        "GET:/mock-gateway/system/status\n" +
        "PUT:/mock-gateway/applications\n" +
        "GET:/mock-gateway/system/status\n" +
        "DELETE:/mock-gateway/applications/Organization1/Application1/1.0\n" +
        "GET:/mock-gateway/system/status\n" +
        "DELETE:/mock-gateway/apis/Organization1/API1/1.0\n"
  )
@ManagerRestTestPublishPayload({
    "",
    "{\"publicAPI\":false,\"organizationId\":\"Organization1\",\"apiId\":\"API1\",\"version\":\"1.0\",\"endpointType\":\"rest\",\"endpoint\":\"http://localhost:8080/ping\",\"endpointProperties\":{},\"apiPolicies\":[]}",
    "",
    "{\"organizationId\":\"Organization1\",\"applicationId\":\"Application1\",\"version\":\"1.0\",\"contracts\":[{\"apiOrgId\":\"Organization1\",\"apiId\":\"API1\",\"apiVersion\":\"1.0\",\"plan\":\"Plan1\",\"policies\":[{\"policyJsonConfig\":\"{ 'foo' : 'bar' }\",\"policyImpl\":\"org.example.PolicyDefTwo\"},{\"policyJsonConfig\":\"{ 'kung' : 'foo' }\",\"policyImpl\":\"org.example.PolicyDefOne\"}]}]}"
})
public class PublishingTest {
}
