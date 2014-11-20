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
package io.apiman.manager.ui.client.local.services.rest;

import io.apiman.manager.ui.client.local.services.RestInvokerService;

/**
 * Callback used when making rest invokation via the {@link RestInvokerService}.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IRestInvokerCallback<T> {
    
    /**
     * Called when the rest invokation returns a 200 response.
     * @param response
     */
    public void onSuccess(T response);
    
    /**
     * Called when the rest invokation returns an error response.
     * @param error
     */
    public void onError(Throwable error);

}
