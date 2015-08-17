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
package io.apiman.plugins.cors_policy;

import java.util.Collections;
import java.util.Map;

import io.apiman.gateway.engine.IServiceConnector;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IConnectorInterceptor;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * A policy implementing CORS (Cross-origin resource sharing): a method of defining access to resources
 * outside of the originating domain. It is principally a security mechanism to prevent the loading of
 * resources from unexpected domains, for instance via XSS injection attacks.
 *
 * @see <a href="http://www.w3.org/TR/2014/REC-cors-20140116/">CORS W3C Recommendation 16 January 2014</a>
 * @see <a href="http://www.w3.org/wiki/CORS">CORS W3 Wiki</a>
 *
 * @author Marc Savy {@literal <msavy@redhat.com>}
 */
public class CorsPolicy extends AbstractMappedPolicy<CorsConfigBean> {
    private static final String CORS_SIMPLE_RESPONSE_HEADERS = "cors-simple-response-headers"; //$NON-NLS-1$
    private static final Map<String, String> EMPTY_MAP = Collections.<String,String>emptyMap();


    public CorsPolicy() {
    }

    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<CorsConfigBean> getConfigurationClass() {
        return CorsConfigBean.class;
    }


    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(ServiceRequest, IPolicyContext, C, IPolicyChain)
     */
    @Override
    protected void doApply(final ServiceRequest request, final IPolicyContext context,
            final CorsConfigBean config, final IPolicyChain<ServiceRequest> chain) {
        // Is this request CORS enabled? If not, skip.
        if (CorsConnector.candidateCorsRequest(request)) {
            final CorsConnector corsConnector = new CorsConnector(request, config,
                    context.getComponent(IPolicyFailureFactoryComponent.class));

            // We only need to set the short-circuit connector if it's a pre-flight request.
            if (corsConnector.isShortcircuit()) {
                context.setConnectorInterceptor(new IConnectorInterceptor() {

                    @Override
                    public IServiceConnector createConnector() {
                        return corsConnector;
                    }
                });
            } else {
                setResponseHeaders(context, corsConnector.getResponseHeaders());
            }

            if (corsConnector.isFailure()) {
                chain.doFailure(corsConnector.getFailure());
            } else { // We want to hit the response chain immediately (i.e. avoid auth policies)
                chain.doSkip(request);
            }
        } else {
            chain.doApply(request);
        }
    }


    /* (non-Javadoc)
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(ServiceResponse, IPolicyContext, C, IPolicyChain)
     */
    @Override
    protected void doApply(ServiceResponse response, IPolicyContext context, CorsConfigBean config,
            IPolicyChain<ServiceResponse> chain) {

        Map<String, String> corsHeaders = getResponseHeaders(context);

        if(corsHeaders != EMPTY_MAP) {
            response.getHeaders().putAll(corsHeaders);
        }

        chain.doApply(response);
    }

    private void setResponseHeaders(IPolicyContext context, Map<String, String> response) {
        context.setAttribute(CORS_SIMPLE_RESPONSE_HEADERS, response);
    }

    private Map<String, String> getResponseHeaders(IPolicyContext context) {
        return context.getAttribute(CORS_SIMPLE_RESPONSE_HEADERS, EMPTY_MAP);
    }
}
