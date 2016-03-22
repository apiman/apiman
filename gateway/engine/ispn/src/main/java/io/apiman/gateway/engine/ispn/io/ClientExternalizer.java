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

package io.apiman.gateway.engine.ispn.io;

import io.apiman.gateway.engine.beans.Client;

/**
 * @author eric.wittmann@gmail.com
 */
public class ClientExternalizer extends InfinispanBeanExternalizer<Client> {
    
    private static final long serialVersionUID = -3538635928064957797L;

    /**
     * Constructor.
     */
    public ClientExternalizer() {
    }

    /**
     * @see org.infinispan.commons.marshall.AdvancedExternalizer#getId()
     */
    @Override
    public Integer getId() {
        return ExternalizerIds.CLIENT_EXTERNALIZER;
    }

    /**
     * @see io.apiman.gateway.engine.ispn.io.InfinispanBeanExternalizer#getBeanClass()
     */
    @Override
    protected Class<Client> getBeanClass() {
        return Client.class;
    }

}
