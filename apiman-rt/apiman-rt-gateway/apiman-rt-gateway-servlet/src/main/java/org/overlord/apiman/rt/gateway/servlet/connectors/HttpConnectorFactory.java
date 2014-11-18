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
package org.overlord.apiman.rt.gateway.servlet.connectors;

import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IServiceConnector;
import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConnectorException;
import org.overlord.apiman.rt.engine.io.ISignalReadStream;
import org.overlord.apiman.rt.engine.io.ISignalWriteStream;

/**
 * Connector factory that uses HTTP to invoke back end systems.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpConnectorFactory implements IConnectorFactory {
    
    /**
     * Constructor.
     */
    public HttpConnectorFactory() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.IConnectorFactory#createConnector(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public IServiceConnector createConnector(ServiceRequest request, final Service service) {
        return new IServiceConnector() {
            /**
             * @see org.overlord.apiman.rt.engine.IServiceConnector#request(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.async.IAsyncResultHandler)
             */
            @Override
            public ISignalWriteStream request(ServiceRequest request,
                    IAsyncResultHandler<ISignalReadStream<ServiceResponse>> handler)
                    throws ConnectorException {
                HttpServiceConnection connection = new HttpServiceConnection(request, service, handler);
                return connection;
            }
        };
    }

}
