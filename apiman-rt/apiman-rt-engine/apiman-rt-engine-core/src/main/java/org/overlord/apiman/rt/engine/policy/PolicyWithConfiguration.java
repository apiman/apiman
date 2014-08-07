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
package org.overlord.apiman.rt.engine.policy;

/**
 * A simple container to hold both the policy impl and its configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyWithConfiguration {
    
    private IPolicy policy;
    private String configuration;
    
    /**
     * Constructor.
     * @param policy
     * @param configuration
     */
    public PolicyWithConfiguration(IPolicy policy, String configuration) {
        this.setPolicy(policy);
        this.setConfiguration(configuration);
    }

    /**
     * @return the policy
     */
    public IPolicy getPolicy() {
        return policy;
    }

    /**
     * @param policy the policy to set
     */
    public void setPolicy(IPolicy policy) {
        this.policy = policy;
    }

    /**
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

}
