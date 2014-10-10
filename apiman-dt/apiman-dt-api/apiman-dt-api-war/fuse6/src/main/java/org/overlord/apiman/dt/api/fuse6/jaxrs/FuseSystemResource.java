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
package org.overlord.apiman.dt.api.fuse6.jaxrs;

import org.overlord.apiman.dt.api.beans.system.SystemStatusBean;
import org.overlord.apiman.dt.api.rest.contract.ISystemResource;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * System resource proxy.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseSystemResource extends AbstractFuseResource<ISystemResource> implements ISystemResource {
    
    /**
     * Constructor.
     */
    public FuseSystemResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected ISystemResource getProxy() {
        return ServiceRegistryUtil.getSingleService(ISystemResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ISystemResource#getStatus()
     */
    @Override
    public SystemStatusBean getStatus() {
        return getProxy().getStatus();
    }

}
