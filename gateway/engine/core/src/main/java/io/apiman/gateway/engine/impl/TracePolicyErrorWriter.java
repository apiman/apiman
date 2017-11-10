/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.EngineErrorResponse;

/**
 * @author eric.wittmann@gmail.com
 */
public class TracePolicyErrorWriter extends DefaultPolicyErrorWriter {

    /**
     * @see io.apiman.gateway.engine.impl.DefaultPolicyErrorWriter#createErrorResponse(java.lang.Throwable)
     */
    @Override
    protected EngineErrorResponse createErrorResponse(Throwable error, String message, int statusCode) {
        EngineErrorResponse response = super.createErrorResponse(error, message, statusCode);
        response.setTrace(error);
        return response;
    }

    @Override
    protected String createErrorMessage(ApiRequest request, Throwable error) {
        if (error.getMessage() == null) {
            return error.getClass().getCanonicalName();
        }
        return error.getMessage();
    }

}
