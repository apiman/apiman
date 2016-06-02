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

import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiNamespaceBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.AvailableApiBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.core.IApiCatalog;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.contract.ISearchResource;
import io.apiman.manager.api.rest.contract.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.contract.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of the Search API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class SearchResourceImpl implements ISearchResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject IApiCatalog apiCatalog;
    @Inject ISecurityContext securityContext;

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
     * @see io.apiman.manager.api.rest.contract.ISearchResource#searchClients(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ClientSummaryBean> searchClients(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        // TODO only return clients that the user is permitted to see?
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        try {
            return query.findClients(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISearchResource#searchApis(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        try {
            return query.findApis(criteria);
        } catch (StorageException e) {
            storage.rollbackTx();
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.contract.ISearchResource#searchApiCatalog(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<AvailableApiBean> searchApiCatalog(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        SearchCriteriaUtil.validateSearchCriteria(criteria);

        SearchResultsBean<AvailableApiBean> rval = new SearchResultsBean<>();

        if (criteria.getFilters().isEmpty()) {
            return rval;
        }
        
        // First criteria is the name search keyword
        SearchCriteriaFilterBean bean = criteria.getFilters().get(0);
        if (bean == null) {
            return rval;
        }
        if (!bean.getName().equals("name")) { //$NON-NLS-1$
            return rval;
        }
        String keyword = bean.getValue();
        
        // Second criteria is the namespace
        String namespace = null;
        if (criteria.getFilters().size() >= 2) {
            bean = criteria.getFilters().get(1);
            if (bean != null && bean.getName().equals("namespace") && bean.getOperator() == SearchCriteriaFilterOperator.eq) { //$NON-NLS-1$
                namespace = bean.getValue();
            }
        }

        List<AvailableApiBean> apis = apiCatalog.search(keyword, namespace);

        PagingBean paging = criteria.getPaging();
        if (paging == null) {
            paging = new PagingBean();
            paging.setPage(1);
            paging.setPageSize(500);
        }
        int page = paging.getPage();
        int pageSize = paging.getPageSize();
        int start = (page - 1) * pageSize;

        int totalSize = apis.size();
        if (start <= totalSize) {
            int end = Math.min(start + pageSize, apis.size());
            rval.getBeans().addAll(apis.subList(start, end));
        }

        rval.setTotalSize(totalSize);
        return rval;
    }
    
    /**
     * @see io.apiman.manager.api.rest.contract.ISearchResource#getApiNamespaces()
     */
    @Override
    public List<ApiNamespaceBean> getApiNamespaces() {
        return apiCatalog.getNamespaces(securityContext.getCurrentUser());
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
