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
package io.apiman.gateway.engine;

import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.impl.EngineResultImpl;

/**
 * The API Management runtime engine.  This engine can either be embedded or used as part
 * of a web app gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IEngine {

    /**
     * @return the version of the engine
     */
    String getVersion();

    /**
     * Executes an asynchronous request for a managed API, with the provided
     * handler being passed an {@link EngineResultImpl} with the status and result
     * of the policy chain invocation.
     *
     * @param request a request for a managed API
     * @param resultHandler
     * @param handler an async handler called when a response is returned or an
     *            exception is captured.
     * @return a API request executor
     */
    IApiRequestExecutor executor(ApiRequest request, IAsyncResultHandler<IEngineResult> resultHandler);

    /**
     * Returns the registry that can be used to publish/retire APIs and
     * register/unregister clients.
     * @return the registry
     */
    IRegistry getRegistry();

}
