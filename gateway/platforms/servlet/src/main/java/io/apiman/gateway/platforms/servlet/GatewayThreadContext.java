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

package io.apiman.gateway.platforms.servlet;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;

/**
 * Thread context for the WAR implementation of the gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public class GatewayThreadContext {
    private static final ThreadLocal<ApiRequest> apiRequest = new ThreadLocal<>();
    private static final ThreadLocal<ApiResponse> apiResponse = new ThreadLocal<>();
    private static final ThreadLocal<PolicyFailure> policyFailure = new ThreadLocal<>();

    /**
     * @return the thread-local api request
     */
    public static final ApiRequest getApiRequest() {
        ApiRequest request = apiRequest.get();
        if (request == null) {
            request = new ApiRequest();
            apiRequest.set(request);
        }
        request.setApiKey(null);
        request.setUrl(null);
        request.setDestination(null);
        request.getHeaders().clear();
        request.setRawRequest(null);
        request.setRemoteAddr(null);
        request.setType(null);
        request.setTransportSecure(false);
        return request;
    }

    /**
     * @return the thread-local api response
     */
    public static final ApiResponse getApiResponse() {
        ApiResponse response = apiResponse.get();
        if (response == null) {
            response = new ApiResponse();
            apiResponse.set(response);
        }
        response.setCode(0);
        response.getHeaders().clear();
        response.setMessage(null);
        response.getAttributes().clear();
        return response;
    }

    /**
     * @return the thread-local policy failure
     */
    public static final PolicyFailure getPolicyFailure() {
        PolicyFailure failure = policyFailure.get();
        if (failure == null) {
            failure = new PolicyFailure();
            policyFailure.set(failure);
        }
        failure.setResponseCode(0);
        failure.setFailureCode(0);
        failure.setMessage(null);
        failure.setType(null);
        failure.getHeaders().clear();
        return failure;
    }

}
