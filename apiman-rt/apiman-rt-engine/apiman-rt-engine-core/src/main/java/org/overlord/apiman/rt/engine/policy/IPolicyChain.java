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

import org.overlord.apiman.rt.engine.beans.Policy;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * A Policy Chain consists of an inbound and outbound {@link Policy} sequence,
 * applied in order to the {@link ServiceRequest} and corresponding
 * {@link ServiceResponse} respectively.
 * 
 * The request is forwarded to the appropriate {@link Service} only if every
 * inbound policy was evaluated successfully, and its response must successfully
 * pass through every outbound policy in order to be forwarded to the requester.
 * 
 * In both instances, the chain may be interrupted by a Policy via a
 * {@link PolicyFailure} or exception.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IPolicyChain {
    /**
     * Apply the inbound Policy Chain to the request. 
     * 
     * @param request the request
     */
    void doApply(ServiceRequest request);
    
    /**
     * Apply the outbound Policy Chain to the response.
     * 
     * @param response the response
     */
    void doApply(ServiceResponse response);
    
    /**
     * Handle a policy failure. 
     * 
     * @param failure the policy failure
     */
    void doFailure(PolicyFailure failure);
}