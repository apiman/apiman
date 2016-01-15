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

import java.util.Map;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConfigurationParseException;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A policy that logs the headers of the HTTP request and HTTP response at the current position in the chain.
 *
 * @author ton.swieb@finalist.nl
 */
public class LogHeadersPolicy implements IPolicy {

	private static enum HttpDirection {
		REQUEST("HTTP Request"), 
		RESPONSE("HTTP Response");
		
		private final String description;
		
		HttpDirection(String descr) {
			this.description = descr;
		}
		
		String getDescription() {
			return description;
		}		
	};
	
	public static final String ENDPOINT_ATTRIBUTE="LogHeadersPolicy_EndpointAttribute";
	
    /**
     * Constructor.
     */
    public LogHeadersPolicy() {
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        return null;
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceRequest request, IPolicyContext context, Object config,
            IPolicyChain<ServiceRequest> chain) {
    	
    	String endpoint = request.getService().getEndpoint();
    	context.setAttribute(ENDPOINT_ATTRIBUTE, endpoint);
    	logHeaders(request.getHeaders(),HttpDirection.REQUEST, endpoint);
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceResponse response, IPolicyContext context, Object config,
            IPolicyChain<ServiceResponse> chain) {
    	
    	String endpoint = context.getAttribute(ENDPOINT_ATTRIBUTE,"");
    	logHeaders(response.getHeaders(),HttpDirection.RESPONSE,endpoint);
        chain.doApply(response);
    }

	private void logHeaders(final Map<String, String> headers, final HttpDirection direction, final String endpoint) {
		
		System.out.println(String.format("Logging %d %s headers for %s", headers.size(), direction.getDescription(), endpoint));
    	for (String key : headers.keySet()) {
    		System.out.println(String.format("Key : %s, Value : %s", key, headers.get(key)));
    	}
	}

}
