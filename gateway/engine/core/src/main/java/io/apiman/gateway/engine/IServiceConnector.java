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
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;

/**
 * Interface implemented by connectors to back end systems.  The engine uses
 * a connector to invoke a back end system on behalf of a managed service.
 * All connectors are expected to do their work asynchronously.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IServiceConnector {

    /**
     * Invokes the back-end system.
     *
     * @param request The inbound service request
     * @param handler An async handler to receive the service connection response (or a connection error if one occurs)
     * @return A connection used by caller to pass data to the back-end
     * @throws ConnectorException If a connection error occurs
     */
    IServiceConnection connect(ServiceRequest request, IAsyncResultHandler<IServiceConnectionResponse> handler) throws ConnectorException;
}
