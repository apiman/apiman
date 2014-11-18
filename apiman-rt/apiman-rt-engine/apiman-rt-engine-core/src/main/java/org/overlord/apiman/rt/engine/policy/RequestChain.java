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

import java.util.Iterator;
import java.util.List;

import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.io.IReadWriteStream;

/**
 * Request phase policy chain.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
public class RequestChain extends Chain<ServiceRequest> {

    /**
     * Constructor.
     * @param policies
     * @param context
     */
    public RequestChain(List<IPolicy> policies, IPolicyContext context) {
        super(policies, context);
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IPolicy> iterator() {
        return new RequestIterator(getPolicies());
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.Chain#getServiceHandler(org.overlord.apiman.rt.engine.policy.IPolicy)
     */
    @Override
    protected IReadWriteStream<ServiceRequest> getServiceHandler(IPolicy policy) {
        return policy.getRequestHandler();
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.Chain#applyPolicy(org.overlord.apiman.rt.engine.policy.IPolicy, org.overlord.apiman.rt.engine.policy.IPolicyContext)
     */
    @Override
    protected void applyPolicy(IPolicy policy, IPolicyContext context) {
        policy.apply(getHead(), context, this);
    }

    /**
     * An iterator over a list of policies - iterates through the policies from
     * front to back, which is the proper order when applying the policies to
     * the inbound request.
     */
    private class RequestIterator implements Iterator<IPolicy> {
        private List<IPolicy> policies;
        private int index;

        /**
         * Constructor.
         */
        public RequestIterator(List<IPolicy> policies) {
            this.policies = policies;
            this.index = 0;
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
        public IPolicy next() {
            return policies.get(index++);
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return index < policies.size();
        }
    };

}
