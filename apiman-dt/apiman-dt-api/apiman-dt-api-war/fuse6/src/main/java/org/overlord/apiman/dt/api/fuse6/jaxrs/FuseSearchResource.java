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

import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.ISearchResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.OrganizationNotFoundException;
import org.overlord.commons.services.ServiceRegistryUtil;

/**
 * Search resource proxy.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseSearchResource extends AbstractFuseResource<ISearchResource> implements ISearchResource {
    
    /**
     * Constructor.
     */
    public FuseSearchResource() {
    }

    /**
     * @see org.overlord.apiman.dt.api.fuse6.jaxrs.AbstractFuseResource#getProxy()
     */
    @Override
    protected ISearchResource getProxy() {
        return ServiceRegistryUtil.getSingleService(ISearchResource.class);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ISearchResource#searchOrgs(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<OrganizationBean> searchOrgs(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        return getProxy().searchOrgs(criteria);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ISearchResource#searchApps(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApplicationSummaryBean> searchApps(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        return getProxy().searchApps(criteria);
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ISearchResource#searchServices(org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ServiceSummaryBean> searchServices(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        return getProxy().searchServices(criteria);
    }

}
