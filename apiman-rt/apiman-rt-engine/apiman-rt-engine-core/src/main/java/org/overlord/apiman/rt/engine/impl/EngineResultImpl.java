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
package org.overlord.apiman.rt.engine.impl;

import org.overlord.apiman.rt.engine.IEngineResult;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.io.AbstractStream;
import org.overlord.apiman.rt.engine.io.ISignalReadStream;

/**
 * The result of a call through the policy engine. Encapsulates either a
 * response or a failure.
 * 
 * @author eric.wittmann@redhat.com
 */
public class EngineResultImpl extends AbstractStream<ServiceResponse> implements IEngineResult {
    
    private ServiceResponse serviceResponse = null;
    private PolicyFailure policyFailure = null;
    private transient ISignalReadStream<ServiceResponse> connectorResponseStream;
    
    /**
     * Constructor.
     */
    public EngineResultImpl() {
    }
    
    /**
     * Construct a successful EngineResult.
     * 
     * @param serviceResponse the service response
     */
    public EngineResultImpl(ServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    /**
     * Construct an unsuccessful EngineResult.
     * @param policyFailure
     */
    public EngineResultImpl(PolicyFailure policyFailure) {
        this.policyFailure = policyFailure;
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngineResult#isResponse()
     */
    @Override
    public boolean isResponse() {
        return policyFailure == null;
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IEngineResult#isFailure()
     */
    @Override
    public boolean isFailure() {
        return policyFailure != null;
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngineResult#getServiceResponse()
     */
    @Override
    public ServiceResponse getServiceResponse() {
        return serviceResponse;
    }

    /**
     * @param serviceResponse the serviceResponse to set
     */
    public void setServiceResponse(ServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }

    /**
     * @see org.overlord.apiman.rt.engine.IEngineResult#getPolicyFailure()
     */
    @Override
    public PolicyFailure getPolicyFailure() {
        return policyFailure;
    }

    /**
     * @param policyFailure the policyFailure to set
     */
    public void setPolicyFailure(PolicyFailure policyFailure) {
        this.policyFailure = policyFailure;
    }

    @Override
    protected void handleHead(ServiceResponse head) {
        return; // ServiceResponse?
    }

    @Override
    public ServiceResponse getHead() {
        return serviceResponse;
    }

    /**
     * @see org.overlord.apiman.rt.engine.io.IAbortable#abort()
     */
    @Override
    public void abort() {
        connectorResponseStream.abort();
    }

    /**
     * @param connectorResponseStream the connectorResponseStream to set
     */
    public void setConnectorResponseStream(ISignalReadStream<ServiceResponse> connectorResponseStream) {
        this.connectorResponseStream = connectorResponseStream;
    }
}
