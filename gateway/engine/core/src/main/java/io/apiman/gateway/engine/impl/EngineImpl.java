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

import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.util.ReflectionUtils;
import io.apiman.gateway.engine.DependsOnComponents;
import io.apiman.gateway.engine.IApiRequestExecutor;
import io.apiman.gateway.engine.IComponent;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IEngine;
import io.apiman.gateway.engine.IEngineResult;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.IRequiresInitialization;
import io.apiman.gateway.engine.Version;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyContextImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

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
    private IDelegateFactory logFactory;

    /**
     * Constructor.
     * @param registry the registry
     * @param pluginRegistry the plugin registry
     * @param componentRegistry the component registry
     * @param connectorFactory the connector factory
     * @param policyFactory the policy factory
     * @param metrics the metrics implementation
     * @param logFactory the logger factory
     */
    public EngineImpl(final IRegistry registry, final IPluginRegistry pluginRegistry,
            final IComponentRegistry componentRegistry, final IConnectorFactory connectorFactory,
            final IPolicyFactory policyFactory, final IMetrics metrics, final IDelegateFactory logFactory) {
        setRegistry(registry);
        setPluginRegistry(pluginRegistry);
        setComponentRegistry(componentRegistry);
        setConnectorFactory(connectorFactory);
        setPolicyFactory(policyFactory);
        setMetrics(metrics);
        setLogFactory(logFactory);

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
     * @see io.apiman.gateway.engine.IEngine#executor(ApiRequest, IAsyncResultHandler)
     */
    @Override
    public IApiRequestExecutor executor(ApiRequest request, final IAsyncResultHandler<IEngineResult> resultHandler) {
        return new ApiRequestExecutorImpl(request,
                resultHandler,
                registry,
                new PolicyContextImpl(getComponentRegistry(), getLogFactory()),
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
     * @see io.apiman.gateway.engine.IEngine#getPluginRegistry()
     */
    @Override
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

    /**
     * @return the log factory
     */
    public IDelegateFactory getLogFactory() {
        return logFactory;
    }

    /**
     * Set the log factory
     * @param logFactory the log factory
     */
    public void setLogFactory(IDelegateFactory logFactory) {
        this.logFactory = logFactory;
    }

    private void initialize(Object... m) {
        for (Object o : m) {
            DependsOnComponents annotation = o.getClass().getAnnotation(DependsOnComponents.class);
            if (annotation != null) {
                Class<? extends IComponent>[] value = annotation.value();
                for (Class<? extends IComponent> componentClass : value) {
                    Method setter = ReflectionUtils.findSetter(o.getClass(), componentClass);
                    if (setter != null) {
                        IComponent component = componentRegistry.getComponent(componentClass);
                        try {
                            setter.invoke(o, new Object[] { component });
                        } catch (IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if (o instanceof IRequiresInitialization) {
                ((IRequiresInitialization) o).initialize();
            }
            // Make sure to also initialize all components!
            if (o instanceof IComponentRegistry) {
                Collection<IComponent> components = ((IComponentRegistry) o).getComponents();
                for (IComponent component : components) {
                    initialize(component);
                }
            }
        }
    }
}
