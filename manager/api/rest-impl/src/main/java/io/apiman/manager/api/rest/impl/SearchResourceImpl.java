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
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.search.searchResults.UserSearchResult;
import io.apiman.manager.api.beans.summary.ApiNamespaceBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.AvailableApiBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.core.IApiCatalog;
import io.apiman.manager.api.rest.ISearchResource;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.SearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Implementation of the Search API.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@Transactional
public class SearchResourceImpl implements ISearchResource {

    private IApiCatalog apiCatalog;
    private ISecurityContext securityContext;
    private SearchService searchService;

    /**
     * Constructor.
     */
    @Inject
    public SearchResourceImpl(IApiCatalog apiCatalog,
                              ISecurityContext securityContext,
                              SearchService searchService) {
        this.apiCatalog = apiCatalog;
        this.securityContext = securityContext;
        this.searchService = searchService;
    }

    public SearchResourceImpl() {
    }

    @Override
    public SearchResultsBean<OrganizationSummaryBean> searchOrgs(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        return searchService.findOrganizations(criteria);
    }

    @Override
    public SearchResultsBean<ClientSummaryBean> searchClients(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException, NotAuthorizedException {
        securityContext.checkAdminPermissions();
        return searchService.findClients(criteria);
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria)
            throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        // Hide sensitive data and set only needed data for the UI
        if (securityContext.isAdmin()){
            return searchService.findApis(criteria);
        } else {
            // Redact data and return
            List<ApiSummaryBean> apis = RestHelper.hideSensitiveDataFromApiSummaryBeanList(searchService.findApis(criteria).getBeans());
            return new SearchResultsBean<ApiSummaryBean>()
                    .setBeans(apis)
                    .setTotalSize(apis.size());
        }
    }

    @Override
    //TODO(msavy): push into service layer.
    public SearchResultsBean<AvailableApiBean> searchApiCatalog(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {

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

    @Override
    public SearchResultsBean<UserSearchResult> searchUsers(SearchCriteriaBean criteria) throws InvalidSearchCriteriaException {
        List<UserSearchResult> users = searchService.findUsers(criteria).getBeans()
                .stream()
                .map(user -> new UserSearchResult(user.getUsername(), user.getFullName()))
                .collect(Collectors.toList());

        return new SearchResultsBean<UserSearchResult>()
                .setTotalSize(users.size())
                .setBeans(users);
    }

    @Override
    public SearchResultsBean<RoleBean> searchRoles(SearchCriteriaBean criteria)
            throws InvalidSearchCriteriaException {
        // No permission check needed
        // Hide sensitive data and set only needed data for the UI
        List<RoleBean> roles = searchService.findRoles(criteria).getBeans()
                .stream()
                .map(bean -> RestHelper.hideSensitiveDataFromRoleBean(securityContext, bean))
                .collect(Collectors.toList());

        return new SearchResultsBean<RoleBean>()
                .setBeans(roles)
                .setTotalSize(roles.size());
    }

    @Override
    public List<ApiNamespaceBean> getApiNamespaces() {
        return apiCatalog.getNamespaces(securityContext.getCurrentUser());
    }
}
