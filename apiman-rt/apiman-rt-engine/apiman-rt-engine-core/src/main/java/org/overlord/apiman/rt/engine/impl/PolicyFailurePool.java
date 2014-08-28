/*
 * Copyright 2013 JBoss Inc
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

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;

/**
 * Object pool that can be used to get access to {@link PolicyFailure} objects.
 * Consumers must return the borrowed object to the pool when done with it!
 * 
 * @author eric.wittmann@redhat.com
 */
public final class PolicyFailurePool extends GenericObjectPool<PolicyFailure> {

    private static final GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    static {
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxTotal(1000);
        poolConfig.setMaxIdle(1000);
    }

    /**
     * Constructor.
     */
    public PolicyFailurePool() {
        super(new BasePooledObjectFactory<PolicyFailure>() {
            /**
             * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
             */
            @Override
            public PolicyFailure create() {
                return new PolicyFailure();
            }

            /**
             * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
             */
            @Override
            public PooledObject<PolicyFailure> wrap(PolicyFailure failure) {
                return new DefaultPooledObject<PolicyFailure>(failure);
            }

            /**
             * @see org.apache.commons.pool2.BasePooledObjectFactory#passivateObject(org.apache.commons.pool2.PooledObject)
             */
            @Override
            public void passivateObject(PooledObject<PolicyFailure> p) throws Exception {
                super.passivateObject(p);
                PolicyFailure failure = p.getObject();
                failure.setFailureCode(0);
                failure.setMessage(null);
                failure.setType(null);
                failure.getHeaders().clear();
            }
        }, poolConfig);
    }

}
