/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.test.policies;

import io.apiman.gateway.engine.beans.ServiceResponse;

/**
 * A response returned by the {@link IPolicyTestBackEndService}.  This object
 * represents a simulated response from a real HTTP back end service.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyTestBackEndServiceResponse {

    private final ServiceResponse serviceResponse;
    private final String responseBody;

    /**
     * Constructor.
     * @param serviceResponse
     * @param responseBody
     */
    public PolicyTestBackEndServiceResponse(ServiceResponse serviceResponse, String responseBody) {
        this.serviceResponse = serviceResponse;
        this.responseBody = responseBody;
    }

    /**
     * @return the serviceResponse
     */
    public ServiceResponse getServiceResponse() {
        return serviceResponse;
    }

    /**
     * @return the responseBody
     */
    public String getResponseBody() {
        return responseBody;
    }

}
