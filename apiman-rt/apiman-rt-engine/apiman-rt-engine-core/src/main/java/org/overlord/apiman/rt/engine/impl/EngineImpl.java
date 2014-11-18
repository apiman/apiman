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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.overlord.apiman.rt.engine.IComponentRegistry;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.IEngineResult;
import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.IServiceRequestExecutor;
import org.overlord.apiman.rt.engine.Version;
import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Policy;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceContract;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.exceptions.ConfigurationParseException;
import org.overlord.apiman.rt.engine.beans.exceptions.PolicyNotFoundException;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;
import org.overlord.apiman.rt.engine.policy.PolicyContextImpl;

/**
 * The implementation of the API Management runtime engine.
 *
 * @author eric.wittmann@redhat.com
 */
public class EngineImpl implements IEngine {

    private IRegistry registry;
    private IComponentRegistry componentRegistry;
    private IConnectorFactory connectorFactory;
    private IPolicyFactory policyFactory;

    /**
     * Constructor.
     * @param registry
     * @param componentRegistry
     * @param connectorFactory
     * @param policyFactory
     */
    public EngineImpl(final IRegistry registry, final IComponentRegistry componentRegistry,
            final IConnectorFactory connectorFactory, final IPolicyFactory policyFactory) {
        setRegistry(registry);
        setComponentRegistry(componentRegistry);
        setConnectorFactory(connectorFactory);
        setPolicyFactory(policyFactory);
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngine#getVersion()
     */
    @Override
    public String getVersion() {
        return Version.get().getVersionString();
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngine#request()
     */
    @Override
    public IServiceRequestExecutor executor(ServiceRequest request, final IAsyncResultHandler<IEngineResult> resultHandler) {
        ServiceContract serviceContract = getContract(request);
        request.setContract(serviceContract);
        
        return new ServiceRequestExecutorImpl(request, 
                resultHandler,
                serviceContract,
                new PolicyContextImpl(getComponentRegistry()),
                getPolicies(serviceContract),
                getConnectorFactory());
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngine#publishService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public void publishService(final Service service) throws PublishingException {
        getRegistry().publishService(service);
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngine#retireService(java.lang.String, java.lang.String, java.lang.String)
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
     * @see org.overlord.apiman.rt.engine.IEngine#registerApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public void registerApplication(final Application application) throws RegistrationException {
        Set<Contract> contracts = application.getContracts();
        for (Contract contract : contracts) {
            List<Policy> policies = contract.getPolicies();
            for (Policy policy : policies) {
                try {
                    // Load the policy class and validate the policy config.
                    IPolicy policyImpl = getPolicyFactory().newPolicy(policy.getPolicyImpl());
                    Object policyConfig = policyImpl.parseConfiguration(policy.getPolicyJsonConfig());
                    policy.setPolicyConfig(policyConfig);
                } catch (PolicyNotFoundException e) {
                    throw new RegistrationException(e);
                } catch (ConfigurationParseException e) {
                    throw new RegistrationException(e);
                }
            }
        }
        getRegistry().registerApplication(application);
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngine#unregisterApplication(java.lang.String, java.lang.String, java.lang.String)
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
     *
     * @param request
     */
    private ServiceContract getContract(ServiceRequest request) {
        return getRegistry().getContract(request);
    }

    /**
     * Creates the policies that should be applied for this service invocation.
     * This is achieved by using the policy information set on the contract.
     *
     * @param contract
     */
    private List<IPolicy> getPolicies(ServiceContract contract) {
        // accidentally create the list a few times - it's not worth the
        // overhead of the synch block.
        List<IPolicy> instances = new ArrayList<IPolicy>();

        for (Policy policy : contract.getPolicies()) {
            IPolicy policyImpl = getPolicyFactory().newPolicy(policy.getPolicyImpl());
            policyImpl.setConfiguration(policy.getPolicyConfig());
            instances.add(policyImpl);
        }

        return instances;
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

}
