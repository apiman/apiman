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
package io.apiman.gateway.engine.policies;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A base class for policy impls that use jackson to parse configuration info.
 *
 * @author eric.wittmann@redhat.com
 * @param <C> the config type
 */
public abstract class AbstractMappedPolicy<C> implements IPolicy {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Constructor.
     */
    public AbstractMappedPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public C parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        try {
            return mapper.reader(getConfigurationClass()).readValue(jsonConfiguration);
        } catch (Exception e) {
            throw new ConfigurationParseException(e);
        }
    }

    /**
     * @return the class to use for JSON configuration deserialization
     */
    protected abstract Class<C> getConfigurationClass();

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void apply(ApiRequest request, IPolicyContext context, Object config,
            IPolicyChain<ApiRequest> chain) {
        doApply(request, context, (C) config, chain);
    }

    /**
     * @param request
     * @param chain
     */
    protected void doApply(ApiRequest request, IPolicyContext context, C config, IPolicyChain<ApiRequest> chain) {
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void apply(ApiResponse response, IPolicyContext context, Object config,
            IPolicyChain<ApiResponse> chain) {
        doApply(response, context, (C) config, chain);
    }

    /**
     * Apply the policy to the response.
     * @param response
     * @param context
     * @param config
     * @param chain
     */
    protected void doApply(ApiResponse response, IPolicyContext context, C config, IPolicyChain<ApiResponse> chain) {
        chain.doApply(response);
    }

}
