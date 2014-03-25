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
package org.overlord.apiman.rt.test;

import org.junit.Test;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Service;

/**
 * Make sure the gateway and test echo server are working.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleEchoTest extends AbstractGatewayTest {
    
    @Test
    public void test() throws Exception {
        Service service = new Service();
        service.setOrganizationId("SimpleEchoTest");
        service.setServiceId("echo");
        service.setVersion("1.0.0");
        service.setEndpoint(getEchoEndpoint("/"));
        service.setEndpointType("REST");
        publishService(service);
        
        Application app = new Application();
        app.setOrganizationId("SimpleEchoTest");
        app.setApplicationId("test");
        app.setVersion("1.0.0");
        Contract contract = new Contract();
        contract.setApiKey("12345");
        contract.setContractId("echo-1");
        contract.setServiceOrgId("SimpleEchoTest");
        contract.setServiceId("echo");
        contract.setServiceVersion("1.0.0");
        app.getContracts().add(contract);
        registerApplication(app);
        
        runTestPlan("test-plans/simple-echo-testPlan.xml");
    }

}
