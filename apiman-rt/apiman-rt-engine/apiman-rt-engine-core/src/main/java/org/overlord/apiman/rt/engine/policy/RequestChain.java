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
package org.overlord.apiman.rt.engine.policy;

import java.util.List;

import org.overlord.apiman.rt.engine.async.IReadWriteStream;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;

/**
 * Request phase policy chain.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class RequestChain extends Chain<ServiceRequest> {

    private RequestIterator policyIterator;

    public RequestChain(List<AbstractPolicy> policies, IPolicyContext context) {
        super(policies, context, 0);

        headPolicyHandler = policies.get(startIndex).getRequestHandler();
        tailPolicyHandler = policies.get(policies.size() - 1).getRequestHandler();
        policyIterator = new RequestIterator(startIndex);

        chainPolicyHandlers();
    }

    protected IReadWriteStream<ServiceRequest> getServiceHandler(AbstractPolicy policy) {
        return policy.getRequestHandler();
    }

    protected void executePolicy(AbstractPolicy policy) {
        policy.request(getHead(), context, this);
    }

    protected ResettableIterator<AbstractPolicy> policyIterator() {
        return policyIterator;
    }

    private class RequestIterator implements ResettableIterator<AbstractPolicy> {
        private int initial;
        private int index;

        public RequestIterator(int index) {
            this.index = index;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbstractPolicy next() {
            return policies.get(index++);
        }

        @Override
        public boolean hasNext() {
            return index < policies.size();
        }

        public void reset() {
            this.index = initial;
        }
    };

}
