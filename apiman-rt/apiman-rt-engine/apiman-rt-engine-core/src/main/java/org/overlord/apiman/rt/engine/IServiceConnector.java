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

import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConnectorException;

/**
 * Interface implemented by connectors to back end systems.  The engine uses
 * a connector to invoke a back end system on behalf of a managed service.
 * All connectors are expected to do their work asynchronously.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IServiceConnector {

    /**
     * Invokes the back end system.
     * @param request the inbound service request
     * @param handler an async handler to receive the service response when complete
     * @return a response from the back end system
     * @throws ConnectorException if a connection error occurs
     */
    public void invoke(ServiceRequest request, IAsyncHandler<ServiceResponse> handler) throws ConnectorException;

}
