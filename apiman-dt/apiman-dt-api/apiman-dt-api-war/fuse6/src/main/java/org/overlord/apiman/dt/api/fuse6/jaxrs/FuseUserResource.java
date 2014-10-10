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

import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.NotAuthorizedException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.UserNotFoundException;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * User resource proxy.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseUserResource extends AbstractFuseResource<IUserResource> implements IUserResource {
    
    /**
     * Constructor.
     */
    public FuseUserResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected IUserResource getProxy() {
        return ServiceRegistryUtil.getSingleService(IUserResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#get(java.lang.String)
     */
    @Override
    public UserBean get(String userId) throws UserNotFoundException {
        return getProxy().get(userId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#update(java.lang.String, org.overlord.apiman.dt.api.beans.idm.UserBean)
     */
    @Override
    public void update(String userId, UserBean user) throws UserNotFoundException, NotAuthorizedException {
        getProxy().update(userId, user);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#search(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> search(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        return getProxy().search(criteria);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#getOrganizations(java.lang.String)
     */
    @Override
    public List<OrganizationSummaryBean> getOrganizations(String userId) {
        return getProxy().getOrganizations(userId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#getApplications(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> getApplications(String userId) {
        return getProxy().getApplications(userId);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IUserResource#getServices(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> getServices(String userId) {
        return getProxy().getServices(userId);
    }

}
