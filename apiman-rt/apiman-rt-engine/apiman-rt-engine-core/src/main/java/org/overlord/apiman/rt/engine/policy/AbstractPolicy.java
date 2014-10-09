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
package org.overlord.apiman.rt.engine.policy;

import org.overlord.apiman.rt.engine.async.Abortable;
import org.overlord.apiman.rt.engine.async.AbstractStream;
import org.overlord.apiman.rt.engine.async.IReadWriteStream;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConfigurationParseException;

/**
 * A {@link Policy} may inspect a {@link ServiceRequest} and associated {@link ServiceRespose} to indicate
 * whether a given conversation is permitted to continue.
 * 
 * {@link #getRequestHandler()} and {@link #getResponseHandler()} should be overridden if the implementor
 * wishes to inspect or in any way modify the data stream, otherwise a simple pass-through mechanism is
 * assumed.
 * 
 * When an implementation determines the status of a conversation, they must call onto
 * {@link Chain#doApply(Object)} in order to indicate success or failure.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public abstract class AbstractPolicy implements Abortable {
    
    private IReadWriteStream<ServiceRequest> defaultRequestHandler;
    private IReadWriteStream<ServiceResponse> defaultResponseHandler;
    
    /**
     * Parses the JSON configuration into a policy specific configuration object type.  The
     * policy implementation can parse the config in any way it chooses, resulting in any
     * type of object it desires.
     * @param jsonConfiguration
     */
    public abstract Object parseConfiguration(String jsonConfiguration) throws ConfigurationParseException;
    
    /**
     * @return Policy's configuration.
     */
    public abstract Object getConfig();
    
    /**
     * Once a single #parseConfiguration has been called, a previously parsed
     * can be passed in.
     * 
     * @param config Policy specific configuration.
     */
    public abstract void setConfig(Object config);
    
    /**
     * Applies a policy upon a {@link ServiceRequest} based on information
     * included in the request itself in addition to its context and configuration.
     * 
     * 
     * @param request an inbound request to apply to the policy to
     * @param context contextual information
     * @param config the policy's configuration information
     * @param chain the policy chain being invoked
     */
    public abstract void request(ServiceRequest request, IPolicyContext context, Chain<ServiceRequest> chain);
    
    /**
     * @return Request handler to stream request data through the policy.
     */
    public IReadWriteStream<ServiceRequest> getRequestHandler() {
        if (defaultRequestHandler == null) {
            defaultRequestHandler = new AbstractStream<ServiceRequest>() {

                @Override
                public ServiceRequest getHead() {
                    return getServiceRequest();
                }

                @Override
                protected void handleHead(ServiceRequest head) {
                }

            };
        }

        return defaultRequestHandler;
    }

    /**
     * Applies a policy upon a {@link ServiceResponse} based on information
     * included in the response itself in addition to its context and configuration.
     * 
     * @param response an outbound response to apply the policy to
     * @param context contextual information
     * @param config the policy's configuration information
     * @param chain chain the policy chain being invoked
     */
    public abstract void response(ServiceResponse response, IPolicyContext context, Chain<ServiceResponse> chain);
    
    /**
     * @return Response handler to stream request data through the policy.
     */
    public IReadWriteStream<ServiceResponse> getResponseHandler() {
        if (defaultResponseHandler == null) {
            defaultResponseHandler = new AbstractStream<ServiceResponse>() {

                @Override
                public ServiceResponse getHead() {
                    return getServiceResponse();
                }

                @Override
                protected void handleHead(ServiceResponse head) {
                }

            };
        }

        return defaultResponseHandler;
    }
    
    protected abstract ServiceRequest getServiceRequest();
    protected abstract ServiceResponse getServiceResponse();
}