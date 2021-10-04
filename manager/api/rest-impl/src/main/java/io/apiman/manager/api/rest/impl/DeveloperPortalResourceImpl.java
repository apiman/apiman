package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.rest.IDeveloperPortalResource;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.service.ApiService;
import io.apiman.manager.api.service.SearchService;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * {@inheritDoc}.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class DeveloperPortalResourceImpl implements IDeveloperPortalResource {

    private final IApimanLogger LOG = ApimanLoggerFactory.getLogger(DeveloperPortalResourceImpl.class);
    private SearchService searchService;
    private ApiService apiService;

    @Inject
    public DeveloperPortalResourceImpl(SearchService searchService, ApiService apiService) {
        this.searchService = searchService;
        this.apiService = apiService;
    }

    public DeveloperPortalResourceImpl() {
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria) throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        LOG.debug("Searching for APIs by criteria {0}", criteria);
        List<ApiSummaryBean> apis = searchService.findApis(criteria).getBeans();
    }

    @Override
    public List<ApiSummaryBean> getFeaturedApis() {
        return apiService.getFeaturedApis();
    }

    private SearchCriteriaBean requireExposeInPortal() {

    }
}
