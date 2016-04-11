/*
 * Copyright 2016 JBoss Inc
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

package io.apiman.plugins.circuit_breaker;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.plugins.circuit_breaker.beans.CircuitBreakerConfigBean;

import java.util.HashMap;
import java.util.Map;

/**
 * A policy that implements basic circuit breaker functionality.
 * 
 * @author eric.wittmann@gmail.com
 */
public class CircuitBreakerPolicy extends AbstractMappedPolicy<CircuitBreakerConfigBean> {

    private static final int BROKEN_CIRCUIT_FAILURE_CODE = 20001;

    private final Map<CircuitKey, Circuit> circuits = new HashMap<CircuitKey, Circuit>();
    
    /**
     * Constructor.
     */
    public CircuitBreakerPolicy() {
    }
    
    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#getConfigurationClass()
     */
    @Override
    protected Class<CircuitBreakerConfigBean> getConfigurationClass() {
        return CircuitBreakerConfigBean.class;
    }
    
    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, CircuitBreakerConfigBean config,
            IPolicyChain<ApiRequest> chain) {
        CircuitKey ckey = new CircuitKey(request.getApiOrgId(), request.getApiId(), request.getApiVersion());
        Circuit circuit = circuits.get(ckey);
        if (circuit == null) {
            circuit = new Circuit(config.getLimit(), config.getWindow(), config.getReset());
            circuits.put(ckey, circuit);
        }
        
        context.setAttribute(CircuitBreakerPolicy.class.getName() + ".circuit", circuit); //$NON-NLS-1$

        // Is the circuit broken?  If so, either immediately send a failure, or else
        // if the circuit is ready to be reset (the reset time has elapsed) then try
        // to reset the circuit by letting through the request and seeing what happens.
        if (circuit.isBroken()) {
            // Can the circuit possibly be reset?  If so, try...otherwise fail.
            if (circuit.isResettable()) {
                circuit.startReset();
                super.doApply(request, context, config, chain);
            } else {
                IPolicyFailureFactoryComponent failureFactory = context.getComponent(IPolicyFailureFactoryComponent.class);
                PolicyFailure failure = failureFactory.createFailure(PolicyFailureType.Other, BROKEN_CIRCUIT_FAILURE_CODE, "Circuit broken."); //$NON-NLS-1$
                failure.setResponseCode(config.getFailureCode());
                chain.doFailure(failure);
            }
        } else {
            super.doApply(request, context, config, chain);
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.policies.AbstractMappedPolicy#doApply(io.apiman.gateway.engine.beans.ApiResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    protected void doApply(ApiResponse response, IPolicyContext context, CircuitBreakerConfigBean config,
            IPolicyChain<ApiResponse> chain) {
        Circuit circuit = context.getAttribute(CircuitBreakerPolicy.class.getName() + ".circuit", (Circuit) null); //$NON-NLS-1$
        boolean isFault = isCircuitFault(response, config);
        if (circuit.isResetting()) {
            if (isFault) {
                circuit.trip();
            } else {
                circuit.reset();
            }
        } else {
            if (isFault) {
                circuit.addFault();
            }
        }
        super.doApply(response, context, config, chain);
    }

    /**
     * Returns true if the API response represents a circuit fault.  This depends on
     * the return code from the back end API as well as the configuration of the policy.
     * @param response
     * @param config
     */
    protected static boolean isCircuitFault(ApiResponse response, CircuitBreakerConfigBean config) {
        int code = response.getCode();
        for (String codePattern : config.getErrorCodes()) {
            if (isMatch(code, codePattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the code matches the given pattern.
     * @param code
     * @param codePattern
     */
    protected static boolean isMatch(int code, String codePattern) {
        String scode = String.valueOf(code);
        byte [] c = scode.getBytes();
        byte [] cp = codePattern.getBytes();
        if (c.length != cp.length) {
            return false;
        }
        for (int idx = 0; idx < c.length; idx++) {
            byte cb = c[idx];
            byte cpb = cp[idx];
            if (cb != cpb && cpb != '*') {
                return false;
            }
        }
        return true;
    }

}
