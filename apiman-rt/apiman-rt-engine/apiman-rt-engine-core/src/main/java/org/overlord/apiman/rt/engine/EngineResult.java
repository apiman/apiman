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
package org.overlord.apiman.rt.engine;

import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * The result of a call through the policy engine. Encapsulates either a
 * response or a failure.
 * 
 * @author eric.wittmann@redhat.com
 */
public class EngineResult {
    
    public ServiceResponse serviceResponse = null;
    public PolicyFailure policyFailure = null;
    
    /**
     * Constructor.
     */
    public EngineResult() {
    }
    
    /**
     * Construct a successful EngineResult.
     * 
     * @param serviceResponse the service response
     */
    public EngineResult(ServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    /**
     * Construct an unsuccessful EngineResult.
     * @param policyFailure
     */
    public EngineResult(PolicyFailure policyFailure) {
        this.policyFailure = policyFailure;
    }

    /**
     * Whether a response has been set.
     * 
     * @return true if response set, false if unset.
     */
    public boolean isResponse() {
        return policyFailure == null;
    }
    
    /**
     * Whether a failure occurred during engine execution.
     * 
     * @return true if failure set, false if unset.
     */
    public boolean isFailure() {
        return policyFailure != null;
    }

    /**
     * @return the serviceResponse
     */
    public ServiceResponse getServiceResponse() {
        return serviceResponse;
    }

    /**
     * @param serviceResponse the serviceResponse to set
     */
    public void setServiceResponse(ServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    /**
     * @return the policyFailure
     */
    public PolicyFailure getPolicyFailure() {
        return policyFailure;
    }

    /**
     * @param policyFailure the policyFailure to set
     */
    public void setPolicyFailure(PolicyFailure policyFailure) {
        this.policyFailure = policyFailure;
    }
}
