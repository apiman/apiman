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
package io.apiman.gateway.engine;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;

/**
 * Used to format an apiman policy failure object into a response suitable for sending
 * back to the calling client.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPolicyFailureWriter {

    /**
     * Formats the given policy failure.
     * @param request the API request
     * @param failure the policy failure
     * @param response the client response
     */
    public void write(ApiRequest request, PolicyFailure failure, IApiClientResponse response);

}
