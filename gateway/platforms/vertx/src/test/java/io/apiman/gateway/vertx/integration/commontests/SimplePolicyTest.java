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
package io.apiman.gateway.vertx.integration.commontests;

import io.apiman.gateway.vertx.integration.java.RestTestBase;
import io.apiman.gateway.vertx.unit.SimplePolicy;

import org.junit.Test;

/**
 * Make sure the gateway and test echo server are working.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimplePolicyTest extends RestTestBase {
    
    @Test
    public void test() throws Exception {
        SimplePolicy.reset();
        runTestPlan("test-plans/simple/simple-policy-testPlan.xml"); //$NON-NLS-1$
        // This test invokes the echo service twice, so that should result in two
        // invokations of the simple policy
        
        // TODO: These are being run after #after() is called in runTestPlan, which breaks it.
        //Assert.assertEquals(2, SimplePolicy.inboundCallCounter);
        //Assert.assertEquals(2, SimplePolicy.outboundCallCounter);
    }

}
