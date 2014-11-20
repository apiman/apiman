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
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;


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
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createRegistry()
     */
    @Override
    protected IRegistry createRegistry() {
        return new InMemoryRegistry();
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createComponentRegistry()
     */
    @Override
    protected IComponentRegistry createComponentRegistry() {
        return new DefaultComponentRegistry();
    }

    /**
     * @see io.apiman.gateway.engine.impl.AbstractEngineFactory#createPolicyFactory()
     */
    @Override
    protected IPolicyFactory createPolicyFactory() {
        return new PolicyFactoryImpl();
    }

}
