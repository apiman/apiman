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
import org.overlord.apiman.rt.engine.beans.ServiceResponse;

/**
 * Response phase policy chain.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class ResponseChain extends Chain<ServiceResponse> {

    private ResponseIterator policyIterator;

    public ResponseChain(List<AbstractPolicy> policies, IPolicyContext context) {
        super(policies, context, policies.size() - 1);

        headPolicyHandler = policies.get(startIndex).getResponseHandler();
        tailPolicyHandler = policies.get(0).getResponseHandler();
        policyIterator = new ResponseIterator(startIndex);

        chainPolicyHandlers();
    }

    protected IReadWriteStream<ServiceResponse> getServiceHandler(AbstractPolicy policy) {
        return policy.getResponseHandler();
    }

    protected void executePolicy(AbstractPolicy policy) {
        policy.response(getHead(), context, this);
    }

    protected ResettableIterator<AbstractPolicy> policyIterator() {
        return policyIterator;
    }

    private class ResponseIterator implements ResettableIterator<AbstractPolicy> {
        private int initial;
        private int index;

        public ResponseIterator(int index) {
            this.index = index;
            this.initial = index;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AbstractPolicy next() {
            return policies.get(index--);
        }

        @Override
        public boolean hasNext() {
            return index >= 0;
        }

        public void reset() {
            this.index = initial;
        }
    };
}
