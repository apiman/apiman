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
package io.apiman.gateway.test.policies;

import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.policy.IConnectorInterceptor;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.test.policies.connectors.CannedResponseServiceConnection;

/**
 * A simple policy used to test connector interceptors.
 *
 * @author rubenrm1@gmail.com
 */
public class SimpleConnectorInterceptorPolicy implements IPolicy {
    
    /**
     * Constructor.
     */
    public SimpleConnectorInterceptorPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }
    
    /**
     * A {@link IConnectorInterceptor} is created which will return an instance of {@link IServiceConnector} 
     * that when the connect method is invoked, a {@link CannedResponseServiceConnection} will be returned
     * 
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ServiceRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ServiceRequest> chain) {
        
        if(request.getHeaders().containsKey("intercept")) { //$NON-NLS-1$
            context.setConnectorInterceptor(new IConnectorInterceptor() {
                
                @Override
                public IServiceConnector createConnector() {
                    return new IServiceConnector() {
                        
                        @Override
                        public IServiceConnection connect(ServiceRequest request,
                                IAsyncResultHandler<IServiceConnectionResponse> handler) throws ConnectorException {
                            return new CannedResponseServiceConnection(handler);
                        }
                    };
                }
            });
        }
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ServiceResponse response, IPolicyContext context, Object config,
            final IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }
    
}
