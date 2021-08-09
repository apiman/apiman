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

import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.*;
import io.apiman.manager.api.beans.search.searchResults.UserSearchResult;
import io.apiman.manager.api.beans.summary.*;
import io.apiman.manager.api.core.IApiCatalog;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.rest.ISearchResource;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.SystemErrorException;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;
import io.apiman.manager.api.security.ISecurityContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Search API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@Transactional
public class SearchResourceImpl implements ISearchResource {

    private IStorage storage;
    private IStorageQuery query;
    private IApiCatalog apiCatalog;
    private ISecurityContext securityContext;

    /**
     * Constructor.
     */
    @Inject
    public SearchResourceImpl(
        IStorage storage,
        IStorageQuery query,
        IApiCatalog apiCatalog,
        ISecurityContext securityContext
    ) {
        this.storage = storage;
        this.query = query;
        this.apiCatalog = apiCatalog;
        this.securityContext = securityContext;
    }

    public SearchResourceImpl() {
    }

    /**
     * @see io.apiman.manager.api.rest.ISearchResource#searchOrgs(io.apiman.manager.api.beans.search.SearchCriteriaBean)
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
     * @see io.apiman.manager.api.rest.ISearchResource#searchClients(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ClientSummaryBean> searchClients(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException, NotAuthorizedException {
        securityContext.checkAdminPermissions();

        SearchCriteriaUtil.validateSearchCriteria(criteria);
        try {
            return query.findClients(criteria);
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.ISearchResource#searchApis(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        try {
            // Hide sensitive data and set only needed data for the UI
            if (securityContext.isAdmin()){
                return query.findApis(criteria);
            } else {
                List<ApiSummaryBean> apis = RestHelper.hideSensitiveDataFromApiSummaryBeanList(query.findApis(criteria).getBeans());
                SearchResultsBean<ApiSummaryBean> result = new SearchResultsBean<>();
                result.setBeans(apis);
                result.setTotalSize(apis.size());
                return result;
            }
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.rest.ISearchResource#searchApiCatalog(io.apiman.manager.api.beans.search.SearchCriteriaBean)
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

        List<AvailableApiBean> catalogEntries = apiCatalog.search(keyword, namespace);
        List<AvailableApiBean> apis = new ArrayList<>();

        // Hide sensitive data like endpoint if the user has no permission to create an new API
        if (securityContext.getPermittedOrganizations(PermissionType.apiEdit).isEmpty() && !securityContext.isAdmin()){
            for (AvailableApiBean api : catalogEntries) {
                AvailableApiBean entry = new AvailableApiBean();
                entry.setId(api.getId());
                entry.setIcon(api.getIcon());
                entry.setRouteEndpoint(api.getRouteEndpoint());
                entry.setEndpointType(api.getEndpointType());
                entry.setName(api.getName());
                entry.setDescription(api.getDescription());
                entry.setDefinitionType(api.getDefinitionType());
                entry.setNamespace(api.getNamespace());
                entry.setTags(api.getTags());
                entry.setInternal(api.isInternal());
                apis.add(entry);
            }
        } else {
            apis.addAll(catalogEntries);
        }

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
     * @see io.apiman.manager.api.rest.ISearchResource#searchUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserSearchResult> searchUsers(SearchCriteriaBean criteria) throws InvalidSearchCriteriaException {
        List<UserSearchResult> users = new ArrayList<>();
        try {
            // Maybe this should be a new query in the future?
            List<UserBean> userBeans = query.findUsers(criteria).getBeans();
            for (UserBean user : userBeans) {
                users.add(new UserSearchResult(user.getUsername(), user.getFullName()));
            }
            SearchResultsBean<UserSearchResult> searchResultsBean = new SearchResultsBean<>();
            searchResultsBean.setBeans(users);
            searchResultsBean.setTotalSize(users.size());
            return  searchResultsBean;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }

    /**
     * @see ISearchResource#searchRoles(SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> searchRoles(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        // No permission check needed
        try {
            // Hide sensitive data and set only needed data for the UI
            SearchCriteriaUtil.validateSearchCriteria(criteria);
            List<RoleBean> roles = new ArrayList<>();
            for (RoleBean bean : query.findRoles(criteria).getBeans()) {
                roles.add(RestHelper.hideSensitiveDataFromRoleBean(securityContext, bean));
            }
            SearchResultsBean<RoleBean> result = new SearchResultsBean<>();
            result.setBeans(roles);
            result.setTotalSize(roles.size());
            return result;
        } catch (StorageException e) {
            throw new SystemErrorException(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.rest.ISearchResource#getApiNamespaces()
     */
    @Override
    public List<ApiNamespaceBean> getApiNamespaces() {
        return apiCatalog.getNamespaces(securityContext.getCurrentUser());
    }
}
