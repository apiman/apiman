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
package io.apiman.gateway.test.policies;

import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IDataPolicy;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import io.apiman.gateway.platforms.servlet.io.ByteBuffer;

import java.io.UnsupportedEncodingException;

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
     * @see io.apiman.gateway.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public Object parseConfiguration(String jsonConfiguration) {
        return new Object();
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(final ServiceRequest request, final IPolicyContext context, final Object config,
            final IPolicyChain<ServiceRequest> chain) {
        chain.doApply(request);
    }

    /**
     * @see io.apiman.gateway.engine.policy.IPolicy#apply(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext, java.lang.Object, io.apiman.gateway.engine.policy.IPolicyChain)
     */
    @Override
    public void apply(ServiceResponse response, IPolicyContext context, Object config,
            IPolicyChain<ServiceResponse> chain) {
        chain.doApply(response);
    }
    
    /**
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getRequestDataHandler(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.policy.IPolicyContext)
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
            public void write(IApimanBuffer chunk) {
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
     * @see io.apiman.gateway.engine.policy.IDataPolicy#getResponseDataHandler(io.apiman.gateway.engine.beans.ServiceResponse, io.apiman.gateway.engine.policy.IPolicyContext)
     */
    @Override
    public IReadWriteStream<ServiceResponse> getResponseDataHandler(ServiceResponse response,
            IPolicyContext context) {
        return null;
    }

}
