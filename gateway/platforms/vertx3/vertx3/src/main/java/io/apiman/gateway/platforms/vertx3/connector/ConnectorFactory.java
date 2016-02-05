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
package io.apiman.gateway.platforms.vertx3.connector;

import io.apiman.common.config.options.TLSOptions;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.vertx.core.Vertx;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Create Vert.x connectors to the enable apiman to connect to a backend API.
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class ConnectorFactory implements IConnectorFactory {

    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("Content-Length"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("X-API-Key"); //$NON-NLS-1$
    }

    private Vertx vertx;
    private TLSOptions tlsOptions;

    /**
     * Constructor
     * @param vertx a vertx instance
     * @param config the config
     */
    public ConnectorFactory(Vertx vertx, Map<String, String> config) {
        this.vertx = vertx;
        this.tlsOptions = new TLSOptions(config);
    }

    @Override
    public IApiConnector createConnector(ApiRequest request, final Api api, RequiredAuthType authType) {
        return new IApiConnector() {

            @Override
            public IApiConnection connect(ApiRequest request,
                    IAsyncResultHandler<IApiConnectionResponse> resultHandler)
                    throws ConnectorException {
                // In the future we can switch to different back-end implementations here!
                return new HttpConnector(vertx, api, request, authType, tlsOptions, resultHandler);
            }
        };
    }
}
