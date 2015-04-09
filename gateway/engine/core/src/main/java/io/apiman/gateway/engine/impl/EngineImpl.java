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

import java.lang.reflect.InvocationTargetException;

import io.apiman.common.util.ReflectionUtils;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.IServiceRequestExecutor;
import io.apiman.gateway.engine.Version;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyContextImpl;

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
    private IMetrics metrics;

    /**
     * Constructor.
     * @param registry the registry
     * @param pluginRegistry the plugin registry
     * @param componentRegistry the component registry
     * @param connectorFactory the connector factory
     * @param policyFactory the policy factory
     * @param metrics the metrics implementation
     */
    public EngineImpl(final IRegistry registry, final IPluginRegistry pluginRegistry,
            final IComponentRegistry componentRegistry, final IConnectorFactory connectorFactory,
            final IPolicyFactory policyFactory, final IMetrics metrics) {
        setRegistry(registry);
        setPluginRegistry(pluginRegistry);
        setComponentRegistry(componentRegistry);
        setConnectorFactory(connectorFactory);
        setPolicyFactory(policyFactory);
        setMetrics(metrics);
        
        policyFactory.setPluginRegistry(pluginRegistry);
        metrics.setComponentRegistry(componentRegistry);
        
        initialize(registry, pluginRegistry, componentRegistry, connectorFactory, policyFactory, metrics);
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#getVersion()
     */
    @Override
    public String getVersion() {
        return Version.get().getVersionString();
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#executor(ServiceRequest, IAsyncResultHandler)
     */
    @Override
    public IServiceRequestExecutor executor(ServiceRequest request, final IAsyncResultHandler<IEngineResult> resultHandler) {
        return new ServiceRequestExecutorImpl(request, 
                resultHandler,
                registry,
                new PolicyContextImpl(getComponentRegistry()),
                policyFactory,
                getConnectorFactory(),
                getMetrics());
    }

    /**
     * @see io.apiman.gateway.engine.IEngine#getRegistry()
     */
    @Override
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

    /**
     * @return the metrics
     */
    public IMetrics getMetrics() {
        return metrics;
    }

    /**
     * @param metrics the metrics to set
     */
    public void setMetrics(IMetrics metrics) {
        this.metrics = metrics;
    }
    
    private void initialize(Object... m) {
        try {
            for(Object o : m) {
                ReflectionUtils.callIfExists(o, "initialize"); //$NON-NLS-1$
            }
        } catch (SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e); // If anything breaks at this point, we can't fix it.
        }
    }
}
