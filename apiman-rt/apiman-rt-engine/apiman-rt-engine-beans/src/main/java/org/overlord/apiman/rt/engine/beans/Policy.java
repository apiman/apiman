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
package org.overlord.apiman.rt.engine.beans;

import java.io.Serializable;

/**
 * Models a policy.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class Policy implements Serializable {

    private static final long serialVersionUID = -5945877012261045491L;
    private long policyId;
    private Object policyConfig; //config_info json str
    private String policyClass; //Reference to policy (classname?) we're going to load?

    public Policy() {
    }

    /**
     * @return the policyId
     */
    public long getPolicyId() {
        return policyId;
    }

    /**
     * @param policyId the policyId to set
     */
    public void setPolicyId(long policyId) {
        this.policyId = policyId;
    }

    /**
     * @return the policyConfig.
     */
    public Object getPolicyConfig() {
        return policyConfig;
    }

    /**
     * @param policyConfig the policyConfig to set
     */
    public void setPolicyConfig(Object policyConfig) {
        this.policyConfig = policyConfig;
    }

    /**
     * @return the policyClass
     */
    public String getPolicyClass() {
        return policyClass;
    }

    /**
     * @param policyClass the policyClass to set
     */
    public void setPolicyClass(String policyClass) {
        this.policyClass = policyClass;
    }
}
