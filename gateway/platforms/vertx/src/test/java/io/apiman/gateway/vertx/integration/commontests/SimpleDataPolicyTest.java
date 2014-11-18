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

import org.junit.Test;

/**
 * Make sure the gateway and test echo server are working.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleDataPolicyTest extends RestTestBase {
    
    @Test
    public void test() throws Exception {
        runTestPlan("test-plans/simple/simple-data-policy-testPlan.xml"); //$NON-NLS-1$
    }

}
