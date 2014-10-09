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
package org.overlord.apiman.rt.engine.components;

import org.overlord.apiman.rt.engine.IComponent;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.components.http.HttpMethod;
import org.overlord.apiman.rt.engine.components.http.IHttpClientRequest;
import org.overlord.apiman.rt.engine.components.http.IHttpClientResponse;

/**
 * A component that policies can use to make HTTP calls.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IHttpClientComponent extends IComponent {
    
    /**
     * Creates a new http client request.  The connection isn't made until 
     * data is (optionally) written (http request body) and end() is called.
     * @param endpoint
     * @param method
     * @param handler
     */
    IHttpClientRequest request(String endpoint, HttpMethod method, IAsyncHandler<IHttpClientResponse> handler);

}
