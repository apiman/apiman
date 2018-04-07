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

import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IDelegateFactory;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.IApiRequestPathParser;
import io.apiman.gateway.engine.IComponentRegistry;
import io.apiman.gateway.engine.IGatewayInitializer;
import io.apiman.gateway.engine.IMetrics;
import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;

import java.util.Collections;
import java.util.List;


/**
 * A default engine factory useful for quickly getting ramped up with apiman.
 * This should likely never be used in any sort of production situation,
 * although it's useful for testing and bootstrapping.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class DefaultEngineFactory extends AbstractEngineFactory {

    /**
     * Constructor.
     */
    public DefaultEngineFactory() {
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createRegistry(io.apiman.gateway.engine.IPluginRegistry, io.apiman.common.util.crypt.IDataEncrypter)
     */
    @Override
    protected IRegistry createRegistry(IPluginRegistry pluginRegistry, IDataEncrypter encrypter) {
        return new SecureRegistryWrapper(createRegistryInternal(pluginRegistry), encrypter);
    }

    /**
     * Subclasses can extend this to provide a custom registry.
     * @param pluginRegistry
     */
    protected IRegistry createRegistryInternal(IPluginRegistry pluginRegistry) {
        return new InMemoryRegistry();
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createDataEncrypter(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    protected IDataEncrypter createDataEncrypter(IPluginRegistry pluginRegistry) {
        return new DefaultDataEncrypter();
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createPluginRegistry()
     */
    @Override
    protected IPluginRegistry createPluginRegistry() {
        return new DefaultPluginRegistry();
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createComponentRegistry(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    protected IComponentRegistry createComponentRegistry(IPluginRegistry pluginRegistry) {
        return new DefaultComponentRegistry();
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createPolicyFactory(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    protected IPolicyFactory createPolicyFactory(IPluginRegistry pluginRegistry) {
        return new PolicyFactoryImpl(Collections.emptyMap());
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createMetrics(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    protected IMetrics createMetrics(IPluginRegistry pluginRegistry) {
        return new InMemoryMetrics();
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createInitializers(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    protected List<IGatewayInitializer> createInitializers(IPluginRegistry pluginRegistry) {
        return Collections.emptyList();
    }

    @Override
    protected IDelegateFactory createLoggerFactory(IPluginRegistry pluginRegistry) {
        return new DefaultDelegateFactory();
    }

    @Override
    protected IApiRequestPathParser createRequestPathParser(IPluginRegistry pluginRegistry) {
        return new DefaultRequestPathParser(Collections.emptyMap());
    }
}
