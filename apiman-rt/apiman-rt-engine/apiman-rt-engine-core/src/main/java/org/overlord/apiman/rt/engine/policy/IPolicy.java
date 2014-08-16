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

import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConfigurationParseException;

/**
 * A {@link Policy} may inspect a {@link ServiceRequest} and associated {@link ServiceRespose} to indicate
 * whether a given conversation is permitted to continue.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public interface IPolicy {
    
    /**
     * Parses the JSON configuration into a policy specific configuration object type.  The
     * policy implementation can parse the config in any way it chooses, resulting in any
     * type of object it desires.
     * @param jsonConfiguration
     */
    public Object parseConfiguration(String jsonConfiguration) throws ConfigurationParseException;
    
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
    public void apply(ServiceRequest request, IPolicyContext context, Object config, IPolicyChain chain);
    
    /**
     * Applies a policy upon a {@link ServiceResponse} based on information
     * included in the response itself in addition to its context and configuration.
     * 
     * @param response an outbound response to apply the policy to
     * @param context contextual information
     * @param config the policy's configuration information
     * @param chain chain the policy chain being invoked
     */
    public void apply(ServiceResponse response, IPolicyContext context, Object config, IPolicyChain chain);
}