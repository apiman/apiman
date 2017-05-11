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

import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.log_policy.beans.LogHeadersConfigBean;
import io.apiman.plugins.log_policy.beans.LogHeadersDirectionType;

import java.util.Map.Entry;
import java.util.Set;
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
        IApimanLogger logger = context.getLogger(getClass());
        String endpoint = request.getApi().getEndpoint();
        context.setAttribute(ENDPOINT_ATTRIBUTE, endpoint);
        if (config.isLogHeaders() && config.getDirection() != LogHeadersDirectionType.response) {
            logHeaders(logger, request.getHeaders(), HttpDirection.REQUEST, endpoint);
        }
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, LogHeadersConfigBean config,
            IPolicyChain<ApiResponse> chain) {
        IApimanLogger logger = context.getLogger(getClass());
        if (config.getDirection() != LogHeadersDirectionType.request) {
            String endpoint = context.getAttribute(ENDPOINT_ATTRIBUTE, ""); //$NON-NLS-1$
            if (config.isLogStatusCode()) {
                logger.info(String.format("Status code %d for %s", response.getCode(), endpoint));
            }
            if (config.isLogHeaders()) {
                logHeaders(logger, response.getHeaders(), HttpDirection.RESPONSE, endpoint);
            }
        }
        chain.doApply(response);
    }

    /**
	 * Logs the given headers to standard output.
	 * @param logger 
	 * @param headers
	 * @param direction
	 * @param endpoint
	 */
	private void logHeaders(IApimanLogger logger, final HeaderMap headers, final HttpDirection direction, final String endpoint) {
	    final StringBuilder sb = new StringBuilder();
        sb.append(String.format("Logging %d %s headers for %s", headers.size(), direction.getDescription(), endpoint)); //$NON-NLS-1$
        final Set<String> sortedKeys = new TreeSet<>(headers.keySet());
        for (String key : sortedKeys) {
            for (Entry<String, String> pair : headers.getAllEntries(key)) {
                sb.append(String.format("\r\nKey : %s, Value : %s", pair.getKey(), pair.getValue())); //$NON-NLS-1$
            }
        }
        logger.info(sb.toString());
	}
}
