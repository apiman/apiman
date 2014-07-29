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
package org.overlord.apiman.rt.engine;

import java.util.concurrent.Future;

import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.async.IAsyncResult;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;

/**
 * The implementation of the API Management runtime engine.
 *
 * @author eric.wittmann@redhat.com
 */
public class EngineImpl implements IEngine {

    private IRegistry registry;
    private IConnectorFactory connectorFactory;

    /**
     * Constructor.
     */
    public EngineImpl() {
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IEngine#getVersion()
     */
    @Override
    public String getVersion() {
        return Version.get().getVersionString();
    }

    /**
     * Constructor.
     * @param registry the registry to use
     * @param connectorFactory the connector factory to use
     */
    public EngineImpl(IRegistry registry, IConnectorFactory connectorFactory) {
        setRegistry(registry);
        setConnectorFactory(connectorFactory);
    }

    public void execute(ServiceRequest request, IAsyncHandler<EngineResult> handler) {
        return;
    }
    
    public Future<IAsyncResult<EngineResult>> execute(ServiceRequest request) {
        //return execute(request, null);
        //TODO Set callback wrapper to update Future's result & status.
        return null;
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IEngine#publishService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public void publishService(Service service) throws PublishingException {
        getRegistry().publishService(service);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IEngine#retireService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public void retireService(Service service) throws PublishingException {
        getRegistry().retireService(service);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IEngine#registerApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public void registerApplication(Application application) throws RegistrationException {
        getRegistry().registerApplication(application);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IEngine#unregisterApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public void unregisterApplication(Application application) throws RegistrationException {
        getRegistry().unregisterApplication(application);
    }

    /**
     * @return the registry
     */
    public IRegistry getRegistry() {
        return registry;
    }

    /**
     * @param registry the registry to set
     */
    public void setRegistry(IRegistry registry) {
        this.registry = registry;
    }

    /**
     * @return the connectorFactory
     */
    public IConnectorFactory getConnectorFactory() {
        return connectorFactory;
    }

    /**
     * @param connectorFactory the connectorFactory to set
     */
    public void setConnectorFactory(IConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

}
