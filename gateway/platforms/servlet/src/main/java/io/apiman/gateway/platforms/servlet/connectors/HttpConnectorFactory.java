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
package io.apiman.gateway.platforms.servlet.connectors;

import io.apiman.gateway.engine.IConnectorFactory;
import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.platforms.servlet.connectors.ssl.SSLSessionStrategy;
import io.apiman.gateway.platforms.servlet.connectors.ssl.SSLSessionStrategyFactory;
import io.apiman.gateway.platforms.servlet.connectors.ssl.TLSOptions;

import java.util.Map;

/**
 * Connector factory that uses HTTP to invoke back end systems.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpConnectorFactory implements IConnectorFactory {

    // Standard auth
    private SSLSessionStrategy standardSslStrategy;
    // 2WAY auth (i.e. mutual auth)
    private SSLSessionStrategy mutualAuthSslStrategy;
    private TLSOptions tlsOptions;

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public HttpConnectorFactory(Map<String, String> config) {
        this.tlsOptions = new TLSOptions(config);
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.IConnectorFactory#createConnector(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.auth.RequiredAuthType)
     */
    @Override
    public IServiceConnector createConnector(ServiceRequest request, final Service service,
            final RequiredAuthType requiredAuthType) {
        return new IServiceConnector() {
            /**
             * @see io.apiman.gateway.engine.IServiceConnector#connect(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
             */
            @Override
            public IServiceConnection connect(ServiceRequest request,
                    IAsyncResultHandler<IServiceConnectionResponse> handler) throws ConnectorException {

                HttpServiceConnection connection = new HttpServiceConnection(request,
                        service,
                        requiredAuthType,
                        getSslStrategy(requiredAuthType),
                        handler);
                return connection;
            }
        };
    }

    protected SSLSessionStrategy getSslStrategy(RequiredAuthType authType) {
        try {
            if (authType == RequiredAuthType.MTLS) {
                if (mutualAuthSslStrategy == null) {
                    mutualAuthSslStrategy = SSLSessionStrategyFactory.buildMutual(tlsOptions);
                }
                return mutualAuthSslStrategy;
            } else {
                if (standardSslStrategy == null) {
                    if (tlsOptions.isDevMode()) {
                        standardSslStrategy = SSLSessionStrategyFactory.buildUnsafe();
                    } else {
                        standardSslStrategy = SSLSessionStrategyFactory.buildStandard(tlsOptions);
                    }
                }
                return standardSslStrategy;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
