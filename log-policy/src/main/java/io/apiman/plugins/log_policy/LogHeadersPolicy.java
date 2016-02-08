/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.plugins.log_policy;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.log_policy.beans.LogHeadersConfigBean;
import io.apiman.plugins.log_policy.beans.LogHeadersDirectionType;

import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * A policy that logs the headers of the HTTP request and HTTP response at the current position in the chain.
 *
 * @author ton.swieb@finalist.nl
 */
public class LogHeadersPolicy extends AbstractMappedPolicy<LogHeadersConfigBean> {

	private static enum HttpDirection {
		REQUEST("HTTP Request"),  //$NON-NLS-1$
		RESPONSE("HTTP Response"); //$NON-NLS-1$

		private final String description;

		HttpDirection(String descr) {
			this.description = descr;
		}

		String getDescription() {
			return description;
		}
	};

	public static final String ENDPOINT_ATTRIBUTE = "LogHeadersPolicy_EndpointAttribute"; //$NON-NLS-1$

    /**
     * Constructor.
     */
    public LogHeadersPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<LogHeadersConfigBean> getConfigurationClass() {
        return LogHeadersConfigBean.class;
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, LogHeadersConfigBean config,
            IPolicyChain<ApiRequest> chain) {
        String endpoint = request.getApi().getEndpoint();
        context.setAttribute(ENDPOINT_ATTRIBUTE, endpoint);
        if (config.getDirection() != LogHeadersDirectionType.response) {
            logHeaders(request.getHeaders(),HttpDirection.REQUEST, endpoint);
        }
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, LogHeadersConfigBean config,
            IPolicyChain<ApiResponse> chain) {
        if (config.getDirection() != LogHeadersDirectionType.request) {
            String endpoint = context.getAttribute(ENDPOINT_ATTRIBUTE, ""); //$NON-NLS-1$
            logHeaders(response.getHeaders(),HttpDirection.RESPONSE, endpoint);
        }
        chain.doApply(response);
    }

	/**
	 * Logs the given headers to standard output.
	 * @param headers
	 * @param direction
	 * @param endpoint
	 */
	private void logHeaders(final HeaderMap headers, final HttpDirection direction, final String endpoint) {
        System.out.println(String.format("Logging %d %s headers for %s", headers.size(), direction.getDescription(), endpoint)); //$NON-NLS-1$
        TreeSet<String> sortedKeys = new TreeSet<>(headers.keySet());
        for (String key : sortedKeys) {
            for (Entry<String, String> pair : headers.getAllEntries(key)) {
                System.out.println(String.format("Key : %s, Value : %s", pair.getKey(), pair.getValue())); //$NON-NLS-1$
            }
        }
	}

}
