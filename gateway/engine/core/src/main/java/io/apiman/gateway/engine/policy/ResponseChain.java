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
package io.apiman.gateway.engine.policy;

import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.io.IReadWriteStream;

import java.util.Iterator;
import java.util.List;

/**
 * Response phase policy chain.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class ResponseChain extends Chain<ServiceResponse> {

    /**
     * Constructor.
     * @param policies
     * @param context
     */
    public ResponseChain(List<PolicyWithConfiguration> policies, IPolicyContext context) {
        super(policies, context);
    }
    
    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<PolicyWithConfiguration> iterator() {
        return new ResponseIterator(getPolicies());
    }

    /**
     * @see io.apiman.gateway.engine.policy.Chain#getServiceHandler(io.apiman.gateway.engine.policy.IPolicy)
     */
    @Override
    protected IReadWriteStream<ServiceResponse> getServiceHandler(IPolicy policy) {
        if (policy instanceof IDataPolicy) {
            return ((IDataPolicy) policy).getResponseDataHandler(getHead(), getContext());
        } else {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.policy.Chain#applyPolicy(io.apiman.gateway.engine.policy.PolicyWithConfiguration, io.apiman.gateway.engine.policy.IPolicyContext)
     */
    @Override
    protected void applyPolicy(PolicyWithConfiguration policy, IPolicyContext context) {
        policy.getPolicy().apply(getHead(), context, policy.getConfiguration(), this);
    }

    /**
     * An iterator over a list of policies - iterates through the policies from
     * back to front (in reverse), which is the proper order when applying the 
     * policies to a response (on the way back out).
     */
    private class ResponseIterator implements Iterator<PolicyWithConfiguration> {
        private List<PolicyWithConfiguration> policies;
        private int index;

        /**
         * Constructor.
         */
        public ResponseIterator(List<PolicyWithConfiguration> policies) {
            this.policies = policies;
            this.index = policies.size() - 1;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public PolicyWithConfiguration next() {
            return policies.get(index--);
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return index >= 0;
        }
    };
}
