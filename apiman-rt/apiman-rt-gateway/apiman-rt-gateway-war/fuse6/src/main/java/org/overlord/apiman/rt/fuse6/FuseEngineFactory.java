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
package org.overlord.apiman.rt.fuse6;

import org.overlord.apiman.rt.engine.IComponentRegistry;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IEngine;
import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.impl.AbstractEngineFactory;
import org.overlord.apiman.rt.engine.impl.DefaultComponentRegistry;
import org.overlord.apiman.rt.engine.impl.InMemoryRegistry;
import org.overlord.apiman.rt.engine.osgi.policy.OSGIPolicyFactory;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;

/**
 * Create the runtime engine within Fuse.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseEngineFactory extends AbstractEngineFactory {
    
    /**
     * Constructor.
     */
    public FuseEngineFactory() {
    }

    /**
     * Creates the engine.
     */
    public static IEngine create() {
        return new FuseEngineFactory().createEngine();
    }

    /**
     * @see org.overlord.apiman.rt.engine.impl.AbstractEngineFactory#createRegistry()
     */
    @Override
    protected IRegistry createRegistry() {
        // TODO driven by config info
        return new InMemoryRegistry();
    }

    /**
     * @see org.overlord.apiman.rt.engine.impl.AbstractEngineFactory#createComponentRegistry()
     */
    @Override
    protected IComponentRegistry createComponentRegistry() {
        // TODO driven by config info
        return new DefaultComponentRegistry();
    }

    /**
     * @see org.overlord.apiman.rt.engine.impl.AbstractEngineFactory#createConnectorFactory()
     */
    @Override
    protected IConnectorFactory createConnectorFactory() {
        return new FuseHttpConnectorFactory();
    }

    /**
     * @see org.overlord.apiman.rt.engine.impl.AbstractEngineFactory#createPolicyFactory()
     */
    @Override
    protected IPolicyFactory createPolicyFactory() {
        return new OSGIPolicyFactory();
    }

}
