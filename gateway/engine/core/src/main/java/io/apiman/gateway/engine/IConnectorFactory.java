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

import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;

/**
 * Factory used to create connectors to back-end systems.  An implementation of
 * this interface must be available to the Engine so that it can invoke back-end
 * systems on behalf of a managed service.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IConnectorFactory {

    /**
     * Creates a connector to a back-end system.
     * @param request the inbound service request
     * @param service the managed service being invoked
     * @param requiredAuthType the required authorization type
     * @return a connector to the back-end service
     */
    public IServiceConnector createConnector(ServiceRequest request, Service service,
            RequiredAuthType requiredAuthType);

}
