/*
 * Copyright 2021 Scheer PAS Schweiz AG
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  imitations under the License.
 */

package io.apiman.manager.test;

import io.apiman.manager.test.junit.ManagerRestTestGatewayLog;
import io.apiman.manager.test.junit.ManagerRestTestPlan;
import io.apiman.manager.test.junit.ManagerRestTestPublishPayload;
import io.apiman.manager.test.junit.ManagerRestTester;
import io.apiman.manager.test.junit.RestTestSystemProperties;
import org.junit.runner.RunWith;

/**
 * Runs the "update plugins" test plan.
 * After an update of a plugin we force a republish/reregister
 * This test makes sure that the ManagerRestTestPublishPayload to the gateway contains the new plugin version (2.0.0 instead 1.0-SNAPSHOT)
 *
 * @author florian.volk@scheer-group.com
 */
@RunWith(ManagerRestTester.class)
@ManagerRestTestPlan("test-plans/forcePluginUpdate-testPlan.xml")
@RestTestSystemProperties({
    "apiman.test.m2-path", "src/test/resources/test-plan-data/plugins/m2"
})
@ManagerRestTestGatewayLog(
    "GET:/mock-gateway/system/status\n" +
    "PUT:/mock-gateway/apis\n" +
    "GET:/mock-gateway/system/status\n" +
    "PUT:/mock-gateway/apis\n"
)
@ManagerRestTestPublishPayload({
    "",
    "{\"publicAPI\":true,\"organizationId\":\"Organization1\",\"apiId\":\"API1\",\"version\":\"1.0\",\"endpoint\":\"http://localhost:8080/endpoint\",\"endpointType\":\"rest\",\"endpointContentType\":\"json\",\"endpointProperties\":{},\"parsePayload\":false,\"apiPolicies\":[{\"policyJsonConfig\":null,\"policyImpl\":\"plugin:io.apiman.test:custom-fields-plugin:1.0-SNAPSHOT:war/io.apiman.test.plugins.FieldsPolicy\"}],\"keysStrippingDisabled\":false}",
    "",
    "{\"publicAPI\":true,\"organizationId\":\"Organization1\",\"apiId\":\"API1\",\"version\":\"1.0\",\"endpoint\":\"http://localhost:8080/endpoint\",\"endpointType\":\"rest\",\"endpointContentType\":null,\"endpointProperties\":{},\"parsePayload\":false,\"apiPolicies\":[{\"policyJsonConfig\":null,\"policyImpl\":\"plugin:io.apiman.test:custom-fields-plugin:2.0.0:war/io.apiman.test.plugins.FieldsPolicy\"}],\"keysStrippingDisabled\":false}"
})
public class ForceRePublishAfterPluginUpdateTest {
}
