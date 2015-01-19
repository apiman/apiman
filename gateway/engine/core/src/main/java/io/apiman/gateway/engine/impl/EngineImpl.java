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
package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.Version;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.InvalidServiceException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.i18n.Messages;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyContextImpl;

import java.util.List;

/**
 * The implementation of the API Management runtime engine.
 *
 * @author eric.wittmann@redhat.com
 */
public class EngineImpl implements IEngine {

    private IRegistry registry;
    private IPluginRegistry pluginRegistry;
    private IComponentRegistry componentRegistry;
    private IConnectorFactory connectorFactory;
    private IPolicyFactory policyFactory;

    /**
     * Constructor.
     * @param registry
     * @param pluginRegistry
     * @param componentRegistry
     * @param connectorFactory
     * @param policyFactory
     */
    public EngineImpl(final IRegistry registry, final IPluginRegistry pluginRegistry,
            final IComponentRegistry componentRegistry, final IConnectorFactory connectorFactory,
            final IPolicyFactory policyFactory) {
        setRegistry(registry);
        setPluginRegistry(pluginRegistry);
        setComponentRegistry(componentRegistry);
        setConnectorFactory(connectorFactory);
        setPolicyFactory(policyFactory);
        
        policyFactory.setPluginRegistry(pluginRegistry);
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#getVersion()
     */
    @Override
    public String getVersion() {
        return Version.get().getVersionString();
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#request()
     */
    @Override
    public IServiceRequestExecutor executor(ServiceRequest request, final IAsyncResultHandler<IEngineResult> resultHandler) {
        Service service = null;
        List<Policy> policies = null;
        
        // If no API Key provided - the service must be public.  If an API Key *is* provided
        // then we lookup the Contract and use that.
        if (request.getApiKey() == null) {
            service = getService(request.getServiceOrgId(), request.getServiceId(), request.getServiceVersion());
            if (service == null) {
                throw new InvalidServiceException(Messages.i18n.format("EngineImpl.ServiceNotFound")); //$NON-NLS-1$
            }
            if (!service.isPublicService()) {
                throw new InvalidServiceException(Messages.i18n.format("EngineImpl.ServiceNotPublic")); //$NON-NLS-1$
            }
            policies = service.getServicePolicies();
        } else {
            ServiceContract serviceContract = getContract(request);
            service = serviceContract.getService();
            request.setContract(serviceContract);
            policies = serviceContract.getPolicies();
            if (request.getServiceOrgId() != null) {
                validateRequest(request);
            }
        }
        
        return new ServiceRequestExecutorImpl(request, 
                resultHandler,
                service,
                new PolicyContextImpl(getComponentRegistry()),
                policies,
                policyFactory,
                getConnectorFactory());
    }

    /**
     * Gets a published service by its service coordinates.
     * @param serviceOrgId
     * @param serviceId
     * @param serviceVersion
     */
    protected Service getService(String serviceOrgId, String serviceId, String serviceVersion) {
        return getRegistry().getService(serviceOrgId, serviceId, serviceVersion);
    }

    /**
     * Validates that the contract being used for the request is valid against the
     * service information included in the request.  Basically the request includes
     * information indicating which specific service is being invoked.  This method
     * ensures that the service information in the contract matches the requested
     * service.
     * @param request
     */
    protected void validateRequest(ServiceRequest request) throws InvalidContractException {
        ServiceContract contract = request.getContract();
        
        boolean matches = true;
        if (!contract.getService().getOrganizationId().equals(request.getServiceOrgId())) {
            matches = false;
        }
        if (!contract.getService().getServiceId().equals(request.getServiceId())) {
            matches = false;
        }
        if (!contract.getService().getVersion().equals(request.getServiceVersion())) {
            matches = false;
        }
        if (!matches) {
            throw new InvalidContractException(Messages.i18n.format("EngineImpl.InvalidContractForService", //$NON-NLS-1$
                    request.getServiceOrgId(), request.getServiceId(), request.getServiceVersion()));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#publishService(io.apiman.gateway.engine.beans.Service)
     */
    @Override
    public void publishService(final Service service) throws PublishingException {
        getRegistry().publishService(service);
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#retireService(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void retireService(String organizationId, String serviceId, String version)
            throws PublishingException {
        Service svc = new Service();
        svc.setOrganizationId(organizationId);
        svc.setServiceId(serviceId);
        svc.setVersion(version);
        getRegistry().retireService(svc);
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#registerApplication(io.apiman.gateway.engine.beans.Application)
     */
    @Override
    public void registerApplication(final Application application) throws RegistrationException {
        getRegistry().registerApplication(application);
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#unregisterApplication(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void unregisterApplication(String organizationId, String applicationId, String version)
            throws RegistrationException {
        Application app = new Application();
        app.setOrganizationId(organizationId);
        app.setApplicationId(applicationId);
        app.setVersion(version);
        getRegistry().unregisterApplication(app);
    }
    
    /**
     * Gets the service contract to use for the given request.
     * @param request
     */
    private ServiceContract getContract(ServiceRequest request) {
        return getRegistry().getContract(request);
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
    public void setRegistry(final IRegistry registry) {
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
    public void setConnectorFactory(final IConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    /**
     * @return the policyFactory
     */
    public IPolicyFactory getPolicyFactory() {
        return policyFactory;
    }

    /**
     * @param policyFactory the policyFactory to set
     */
    public void setPolicyFactory(IPolicyFactory policyFactory) {
        this.policyFactory = policyFactory;
    }

    /**
     * @return the componentRegistry
     */
    public IComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }

    /**
     * @param componentRegistry the componentRegistry to set
     */
    public void setComponentRegistry(IComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    /**
     * @return the pluginRegistry
     */
    public IPluginRegistry getPluginRegistry() {
        return pluginRegistry;
    }

    /**
     * @param pluginRegistry the pluginRegistry to set
     */
    public void setPluginRegistry(IPluginRegistry pluginRegistry) {
        this.pluginRegistry = pluginRegistry;
    }

}
