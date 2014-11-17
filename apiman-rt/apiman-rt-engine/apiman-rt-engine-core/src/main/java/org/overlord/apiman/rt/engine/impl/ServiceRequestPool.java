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
import org.overlord.apiman.rt.engine.beans.ServiceRequest;

/**
 * Object pool that can be used to get access to {@link ServiceRequest} objects.
 * Consumers must return the borrowed object to the pool when done with it!
 *
 * @author eric.wittmann@redhat.com
 */
public final class ServiceRequestPool extends GenericObjectPool<ServiceRequest> {

    private static final GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    static {
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxTotal(1000);
        poolConfig.setMaxIdle(1000);
    }

    /**
     * Constructor.
     */
    public ServiceRequestPool() {
        super(new BasePooledObjectFactory<ServiceRequest>() {
            /**
             * @see org.apache.commons.pool2.BasePooledObjectFactory#create()
             */
            @Override
            public ServiceRequest create() {
                return new ServiceRequest();
            }

            /**
             * @see org.apache.commons.pool2.BasePooledObjectFactory#wrap(java.lang.Object)
             */
            @Override
            public PooledObject<ServiceRequest> wrap(ServiceRequest request) {
                return new DefaultPooledObject<ServiceRequest>(request);
            }

            /**
             * @see org.apache.commons.pool2.BasePooledObjectFactory#passivateObject(org.apache.commons.pool2.PooledObject)
             */
            @Override
            public void passivateObject(PooledObject<ServiceRequest> p) throws Exception {
                super.passivateObject(p);
                ServiceRequest request = p.getObject();
                request.setApiKey(null);
                request.setContract(null);
                request.setDestination(null);
                request.getHeaders().clear();
                request.setRawRequest(null);
                request.setRemoteAddr(null);
                request.setType(null);
            }
        }, poolConfig);
    }

}
