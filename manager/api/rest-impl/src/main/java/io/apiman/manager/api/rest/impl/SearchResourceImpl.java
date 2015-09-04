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

package io.apiman.manager.api.rest.impl;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.AvailableServiceBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.core.IServiceCatalog;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.ISearchResource;
import io.apiman.manager.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;

/**
 * Implementation of the Search API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class SearchResourceImpl implements ISearchResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject IServiceCatalog serviceCatalog;

    /**
     * Constructor.
     */
    public SearchResourceImpl() {
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISearchResource#searchOrgs(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<OrganizationSummaryBean> searchOrgs(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        try {
            return query.findOrganizations(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISearchResource#searchApps(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApplicationSummaryBean> searchApps(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        // TODO only return applications that the user is permitted to see?
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        try {
            return query.findApplications(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISearchResource#searchServices(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ServiceSummaryBean> searchServices(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        try {
            return query.findServices(criteria);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISearchResource#searchServiceCatalogs(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<AvailableServiceBean> searchServiceCatalogs(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        SearchCriteriaUtil.validateSearchCriteria(criteria);

        SearchResultsBean<AvailableServiceBean> rval = new SearchResultsBean<>();

        if (criteria.getFilters().isEmpty()) {
            return rval;
        }
        SearchCriteriaFilterBean bean = criteria.getFilters().get(0);
        if (bean == null) {
            return rval;
        }
        if (!bean.getName().equals("name")) { //$NON-NLS-1$
            return rval;
        }

        String keyword = bean.getValue();
        List<AvailableServiceBean> services = serviceCatalog.search(keyword);

        PagingBean paging = criteria.getPaging();
        if (paging == null) {
            paging = new PagingBean();
            paging.setPage(1);
            paging.setPageSize(20);
        }
        int page = paging.getPage();
        int pageSize = paging.getPageSize();
        int start = (page - 1) * pageSize;

        int totalSize = services.size();
        if (start <= totalSize) {
            int end = Math.min(start + pageSize, services.size());
            rval.getBeans().addAll(services.subList(start, end));
        }

        rval.setTotalSize(totalSize);
        return rval;
    }

    /**
     * @return the storage
     */
    public IStorage getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(IStorage storage) {
        this.storage = storage;
    }
}
