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

import java.util.List;

import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.rest.contract.IRoleResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleAlreadyExistsException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.RoleNotFoundException;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Role resource proxy.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseRoleResource extends AbstractFuseResource<IRoleResource> implements IRoleResource {
    
    /**
     * Constructor.
     */
    public FuseRoleResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected IRoleResource getProxy() {
        return ServiceRegistryUtil.getSingleService(IRoleResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#create(org.overlord.apiman.dt.api.beans.idm.RoleBean)
     */
    @Override
    public RoleBean create(RoleBean bean) throws RoleAlreadyExistsException, NotAuthorizedException {
        return getProxy().create(bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#list()
     */
    @Override
    public List<RoleBean> list() throws NotAuthorizedException {
        return getProxy().list();
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#get(java.lang.String)
     */
    @Override
    public RoleBean get(String roleId) throws RoleNotFoundException, NotAuthorizedException {
        return getProxy().get(roleId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.idm.RoleBean)
     */
    @Override
    public void update(String roleId, RoleBean bean) throws RoleNotFoundException, NotAuthorizedException {
        getProxy().update(roleId, bean);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#delete(java.lang.String)
     */
    @Override
    public void delete(String roleId) throws RoleNotFoundException, NotAuthorizedException {
        getProxy().delete(roleId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IRoleResource#search(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException, NotAuthorizedException {
        return getProxy().search(criteria);
    }

}
