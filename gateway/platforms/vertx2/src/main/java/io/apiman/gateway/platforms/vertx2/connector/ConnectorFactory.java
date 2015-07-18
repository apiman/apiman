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
package io.apiman.gateway.platforms.vertx2.connector;

import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.vertx.core.Vertx;

import java.util.HashSet;
import java.util.Set;

/**
 * Create Vert.x connectors to the enable apiman to connect to a backend service.
 *
 * @author Marc Savy <msavy@redhat.com>
 */
public class ConnectorFactory implements IConnectorFactory {

    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("Content-Length"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("X-API-Key"); //$NON-NLS-1$
    }

    private Vertx vertx;

    /**
     * Constructor
     * @param vertx a vertx instance
     */
    public ConnectorFactory(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public IServiceConnector createConnector(ServiceRequest request, final Service service, RequiredAuthType authType) {
        return new IServiceConnector() {

            @Override
            public IServiceConnection connect(ServiceRequest request,
                    IAsyncResultHandler<IServiceConnectionResponse> resultHandler)
                    throws ConnectorException {
                // In the future we can switch to different back-end implementations here!
                return new HttpConnector(vertx, service, request, resultHandler);
            }
        };
    }
}
