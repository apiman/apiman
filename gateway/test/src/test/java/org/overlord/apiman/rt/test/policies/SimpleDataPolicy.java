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
package org.overlord.apiman.rt.test.policies;

import java.io.UnsupportedEncodingException;

import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.io.AbstractStream;
import org.overlord.apiman.rt.engine.io.IBuffer;
import org.overlord.apiman.rt.engine.io.IReadWriteStream;
import org.overlord.apiman.rt.engine.policy.IDataPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;
import org.overlord.apiman.rt.gateway.servlet.io.ByteBuffer;

/**
 * A simple policy used for testing data policies.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
public class SimpleDataPolicy implements IPolicy, IDataPolicy {
    
    /**
     * Constructor.
     */
    public SimpleDataPolicy() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ServiceRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ServiceRequest> chain) {
        chain.doApply(request);
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#apply(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext, java.lang.Object, org.overlord.apiman.rt.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceResponse response, IPolicyContext context, Object config,
            IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IDataPolicy#getRequestDataHandler(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.policy.IPolicyContext)
     */
    @Override
    public IReadWriteStream<ServiceRequest> getRequestDataHandler(final ServiceRequest request,
            final IPolicyContext context) {
        return new AbstractStream<ServiceRequest>() {
            @Override
            public ServiceRequest getHead() {
                return request;
            }
            
            @Override
            protected void handleHead(ServiceRequest head) {
            }
            
            @Override
            public void write(IBuffer chunk) {
                try {
                    String chunkstr = chunk.toString("UTF-8");
                    if (chunkstr.contains("$NAME")) {
                        chunkstr = chunkstr.replaceAll("\\$NAME", "Barry Allen");
                        ByteBuffer buffer = new ByteBuffer(chunkstr.length());
                        buffer.append(chunkstr);
                        super.write(buffer);
                    } else {
                        super.write(chunk);
                    }
                } catch (UnsupportedEncodingException e) {
                    super.write(chunk);
                }
            }
        };
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IDataPolicy#getResponseDataHandler(org.overlord.apiman.rt.engine.beans.ServiceResponse, org.overlord.apiman.rt.engine.policy.IPolicyContext)
     */
    @Override
    public IReadWriteStream<ServiceResponse> getResponseDataHandler(ServiceResponse response,
            IPolicyContext context) {
        return null;
    }

}
