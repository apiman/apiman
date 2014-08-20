/*
 * Copyright 2013 JBoss Inc
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

package org.overlord.apiman.rt.war;

import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * Thread context for the WAR implementation of the gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public class WarGatewayThreadContext {
    
    private static final ThreadLocal<ServiceRequest> serviceRequest = new ThreadLocal<ServiceRequest>();
    private static final ThreadLocal<ServiceResponse> serviceResponse = new ThreadLocal<ServiceResponse>();
    private static final ThreadLocal<PolicyFailure> policyFailure = new ThreadLocal<PolicyFailure>();
    
    /**
     * @return the thread-local service request
     */
    public static final ServiceRequest getServiceRequest() {
        ServiceRequest request = serviceRequest.get();
        if (request == null) {
            request = new ServiceRequest();
            serviceRequest.set(request);
        }
        return request;
    }

    /**
     * @return the thread-local service response
     */
    public static final ServiceResponse getServiceResponse() {
        ServiceResponse request = serviceResponse.get();
        if (request == null) {
            request = new ServiceResponse();
            serviceResponse.set(request);
        }
        return request;
    }

    /**
     * @return the thread-local policy failure
     */
    public static final PolicyFailure getPolicyFailure() {
        PolicyFailure request = policyFailure.get();
        if (request == null) {
            request = new PolicyFailure();
            policyFailure.set(request);
        }
        return request;
    }
    
    /**
     * Resets all thread local objects.
     */
    public static final void reset() {
        ServiceRequest request = getServiceRequest();
        request.setApiKey(null);
        request.setBody(null);
        request.setDestination(null);
        request.getHeaders().clear();
        request.setRawRequest(null);
        request.setRemoteAddr(null);
        request.setType(null);
        
        ServiceResponse response = getServiceResponse();
        response.setBody(null);
        response.setCode(0);
        response.getHeaders().clear();
        response.setMessage(null);
        response.getAttributes().clear();
        
        PolicyFailure failure = getPolicyFailure();
        failure.setFailureCode(0);
        failure.setMessage(null);
        failure.setType(null);
        
    }

}
