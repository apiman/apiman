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

    /**
     * @see org.overlord.apiman.rt.engine.IEngine#executeAsync(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.IResponseHandler)
     */
    @Override
    public void executeAsync(ServiceRequest request, IResponseHandler handler) {
        // TODO implement this!
        try {
            ServiceResponse response = execute(request);
            handler.onResponse(response);
        } catch (Exception e) {
            handler.onError(e);
        }
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IEngine#execute(org.overlord.apiman.rt.engine.beans.ServiceRequest)
     */
    @Override
    public ServiceResponse execute(ServiceRequest request) throws Exception {
        Service service = getRegistry().getService(request);
        Contract contract = getRegistry().getContract(request);
        
        // TODO apply inbound policies to request
        
        IServiceConnector connector = getConnectorFactory().createConnector(request, service);
        ServiceResponse response = connector.invoke(request);
        
        // TODO apply outbound policies to response
        
        return response;
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
