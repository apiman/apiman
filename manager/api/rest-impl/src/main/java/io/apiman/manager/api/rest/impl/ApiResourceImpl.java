package io.apiman.manager.api.rest.impl;

import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.rest.IApiResource;
import io.apiman.manager.api.rest.impl.util.PermissionsHelper;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.SearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Transactional
public class ApiResourceImpl implements IApiResource {

    private ISecurityContext securityContext;
    private SearchService searchService;

    @Inject
    public ApiResourceImpl(ISecurityContext securityContext, SearchService searchService) {
        this.securityContext = securityContext;
        this.searchService = searchService;
    }

    public ApiResourceImpl() {
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> getFeaturedApis() {
        return searchService.findAllFeaturedApis(PermissionsHelper.orgConstraints(securityContext, PermissionType.apiView));
    }
}
