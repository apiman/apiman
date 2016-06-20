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
package io.apiman.gateway.test;

import io.apiman.gateway.engine.GatewayConfigProperties;
import io.apiman.gateway.test.junit.GatewayRestTestPlan;
import io.apiman.gateway.test.junit.GatewayRestTestSystemProperties;
import io.apiman.gateway.test.junit.GatewayRestTester;

import org.junit.runner.RunWith;

/**
 * Make sure the gateway will parse the payload.
 *
 * @author eric.wittmann@redhat.com
 */
@RunWith(GatewayRestTester.class)
@GatewayRestTestPlan("test-plans/simple/simple-payload-policy-testPlan.xml")
@GatewayRestTestSystemProperties({ 
    GatewayConfigProperties.MAX_PAYLOAD_BUFFER_SIZE, "4096",
    GatewayConfigProperties.ERROR_WRITER_CLASS, "io.apiman.gateway.engine.impl.TracePolicyErrorWriter"
})
public class SimplePayloadPolicyTest {

}
