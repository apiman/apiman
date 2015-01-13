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
package io.apiman.gateway.engine.osgi.policy;

import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyFactory;

/**
 * A version of the policy factory that works in an OSGi environment.
 * 
 * @author eric.wittmann@redhat.com
 */
public class OSGIPolicyFactory implements IPolicyFactory {

    /**
     * Constructor.
     */
    public OSGIPolicyFactory() {
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.IPolicyFactory#setPluginRegistry(io.apiman.gateway.engine.IPluginRegistry)
     */
    @Override
    public void setPluginRegistry(IPluginRegistry pluginRegistry) {
        throw new RuntimeException("Not yet implemented."); //$NON-NLS-1$
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.IPolicyFactory#loadConfig(io.apiman.gateway.engine.policy.IPolicy, java.lang.String)
     */
    @Override
    public Object loadConfig(IPolicy policy, String configData) {
        throw new RuntimeException("Not yet implemented."); //$NON-NLS-1$
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.IPolicyFactory#loadPolicy(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void loadPolicy(String policyImpl, IAsyncResultHandler<IPolicy> handler) {
        throw new RuntimeException("Not yet implemented."); //$NON-NLS-1$
    }

}
