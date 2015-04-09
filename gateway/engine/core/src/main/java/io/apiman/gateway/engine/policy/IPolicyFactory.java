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
package io.apiman.gateway.engine.policy;

import io.apiman.gateway.engine.IPluginRegistry;
import io.apiman.gateway.engine.async.IAsyncResultHandler;


/**
 * Factory used to create instances of policies.  This is done asynchronously in case
 * a policy implementation is being provided via a plugin that has not yet been 
 * downloaded.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPolicyFactory {

    /**
     * @param pluginRegistry the plugin registry
     */
    public void setPluginRegistry(IPluginRegistry pluginRegistry);
    
    /**
     * Load a policy implementation asynchronously.
     * @param policyImpl the policy implementation
     * @param handler the result handler
     */
    public void loadPolicy(String policyImpl, IAsyncResultHandler<IPolicy> handler);

    /**
     * Loads the given configuration data into a config object.
     * @param policy the policy
     * @param configData the config data
     * @return config the loaded config object
     */
    public Object loadConfig(IPolicy policy, String configData);
    
}
