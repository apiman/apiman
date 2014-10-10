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

import org.overlord.apiman.dt.api.beans.idm.UserPermissionsBean;
import org.overlord.apiman.dt.api.rest.contract.IPermissionsResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Permission resource proxy.
 *
 * @author eric.wittmann@redhat.com
 */
public class FusePermissionsResource extends AbstractFuseResource<IPermissionsResource> implements IPermissionsResource {
    
    /**
     * Constructor.
     */
    public FusePermissionsResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected IPermissionsResource getProxy() {
        return ServiceRegistryUtil.getSingleService(IPermissionsResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPermissionsResource#getPermissionsForUser(java.lang.String)
     */
    @Override
    public UserPermissionsBean getPermissionsForUser(String userId) throws UserNotFoundException,
            NotAuthorizedException {
        return getProxy().getPermissionsForUser(userId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IPermissionsResource#getPermissionsForCurrentUser()
     */
    @Override
    public UserPermissionsBean getPermissionsForCurrentUser() throws UserNotFoundException {
        return getProxy().getPermissionsForCurrentUser();
    }

}
