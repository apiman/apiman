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
package io.apiman.gateway.engine.policy;

import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;

import org.apache.commons.lang.NotImplementedException;

/**
 * All policy implementations must implement this interface.
 *
 * @author eric.wittmann@redhat.com
 * @author marc@blackparrotlabs.io
 */
public interface IPolicy {

    /**
     * Parses the JSON configuration into a policy specific configuration object type.  The
     * policy implementation can parse the config in any way it chooses, resulting in any
     * type of object it desires.
     *
     * @param jsonConfiguration the json configuration
     * @return the parsed configuration
     * @throws ConfigurationParseException when unable to parse config
     */
    public Object parseConfiguration(String jsonConfiguration) throws ConfigurationParseException;

    /**
     * Applies a policy upon a {@link ApiRequest} based on information
     * included in the request itself in addition to its context and configuration.
     *
     * @param request an inbound request to apply to the policy to
     * @param context contextual information
     * @param config the policy's configuration information
     * @param chain the policy chain being invoked
     */
    public void apply(ApiRequest request, IPolicyContext context, Object config, IPolicyChain<ApiRequest> chain);

    /**
     * Applies a policy upon a {@link ApiResponse} based on information
     * included in the response itself in addition to its context and configuration.
     *
     * @param response an outbound response to apply the policy to
     * @param context contextual information
     * @param config the policy's configuration information
     * @param chain chain the policy chain being invoked
     */
    public void apply(ApiResponse response, IPolicyContext context, Object config, IPolicyChain<ApiResponse> chain);

    /**
     * Process any failure emitted by a subsequent policy in the chain.
     * <p>
     * For example, implementors may wish to add or remove certain headers, even in
     * the case of failures.
     * <p>
     * By default, this is a no-op and providing an implementation is not required.
     *
     * @param failure the policy failure
     * @param context contextual information
     * @param config the policy's configuration information
     * @param chain the chain
     */
    default void processFailure(PolicyFailure failure, IPolicyContext context, Object config,  IPolicyFailureChain chain) {
        chain.doFailure(failure);
    }

}
