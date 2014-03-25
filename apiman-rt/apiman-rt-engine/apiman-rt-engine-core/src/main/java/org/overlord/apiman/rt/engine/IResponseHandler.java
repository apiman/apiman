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

import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * Interface that must be implemented in order for invokers of the 
 * API Management engine to receive a callback when the processing
 * of their 
 *
 * @author eric.wittmann@redhat.com
 */
public interface IResponseHandler {
    
    /**
     * Called after an managed service was successfully invoked.
     * @param response the response from the back-end service
     */
    public void onResponse(ServiceResponse response);

    /**
     * Called if an error occurs while trying to invoke a managed
     * service.
     * @param error the error from the service or policies
     */
    public void onError(Throwable error);

}
