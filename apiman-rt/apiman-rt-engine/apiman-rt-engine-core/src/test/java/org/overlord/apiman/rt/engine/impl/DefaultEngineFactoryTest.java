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
package org.overlord.apiman.rt.engine.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.apiman.rt.engine.EngineResult;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.IServiceConnector;
import org.overlord.apiman.rt.engine.async.AsyncResultImpl;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConnectorException;

/**
 * Unit test for the default engine factory.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultEngineFactoryTest {

    /**
     * Test method for {@link org.overlord.apiman.rt.engine.impl.AbstractEngineFactory#createEngine()}.
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
    public void testCreateEngine() throws InterruptedException, ExecutionException {
        DefaultEngineFactory factory = new DefaultEngineFactory() {
            @Override
            protected IConnectorFactory createConnectorFactory() {
                return new IConnectorFactory() {
                    @Override
                    public IServiceConnector createConnector(ServiceRequest request, Service service) {
                        Assert.assertEquals("test", service.getEndpointType()); //$NON-NLS-1$
                        Assert.assertEquals("test:endpoint", service.getEndpoint()); //$NON-NLS-1$
                        return new IServiceConnector() {
                            @Override
                            public void invoke(ServiceRequest request, IAsyncHandler<ServiceResponse> handler)
                                    throws ConnectorException {
                                ServiceResponse response = new ServiceResponse();
                                response.setCode(200);
                                response.setMessage("OK"); //$NON-NLS-1$
                                handler.handle(AsyncResultImpl.create(response));
                            }
                        };
                    }
                };
            }
        };
        IEngine engine = factory.createEngine();
        Assert.assertNotNull(engine);
        
        // create a service
        Service service = new Service();
        service.setEndpointType("test"); //$NON-NLS-1$
        service.setEndpoint("test:endpoint"); //$NON-NLS-1$
        service.setOrganizationId("TestOrg"); //$NON-NLS-1$
        service.setServiceId("TestService"); //$NON-NLS-1$
        service.setVersion("1.0"); //$NON-NLS-1$
        // create an app
        Application app = new Application();
        app.setApplicationId("TestApp"); //$NON-NLS-1$
        app.setOrganizationId("TestOrg"); //$NON-NLS-1$
        app.setVersion("1.0"); //$NON-NLS-1$
        Contract contract = new Contract();
        contract.setApiKey("12345"); //$NON-NLS-1$
        contract.setServiceId("TestService"); //$NON-NLS-1$
        contract.setServiceOrgId("TestOrg"); //$NON-NLS-1$
        contract.setServiceVersion("1.0"); //$NON-NLS-1$
        app.addContract(contract);
        
        // simple service/app config
        engine.publishService(service);
        engine.registerApplication(app);
        
        ServiceRequest request = new ServiceRequest();
        request.setApiKey("12345"); //$NON-NLS-1$
        request.setDestination("/"); //$NON-NLS-1$
        request.setType("TEST"); //$NON-NLS-1$
        Future<IAsyncResult<EngineResult>> future = engine.execute(request);
        IAsyncResult<EngineResult> result = future.get();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getResult().getServiceResponse());
        Assert.assertEquals("OK", result.getResult().getServiceResponse().getMessage()); //$NON-NLS-1$
    }

}
