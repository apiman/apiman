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

import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

public interface IPolicyChainHandler {

    /**
     * Called when the policy chain has handled the inbound policy sequence.
     * 
     * @param request the inbound request
     */
    void onInboundComplete(ServiceRequest request);

    /**
     * Called when the policy chain has handled the outbound element of the response.
     * 
     * @param response the outbound response
     */
    void onOutputComplete(ServiceResponse response);

    /**
     * Called if a failure is indicated during policy chain execution. 
     * 
     * @param failure the policy failure
     */
    void onFailure(PolicyFailure failure);

    /** 
     * Called if an exception was raised during policy chain execution.
     * 
     * @param t
     */
    void onError(Throwable t);
 
}