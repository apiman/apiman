package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.IDeveloperPortalResource;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.search.SearchService;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
public class DeveloperPortalResourceImpl implements IDeveloperPortalResource {

    private final IApimanLogger LOG = ApimanLoggerFactory.getLogger(DeveloperPortalResourceImpl.class);
    private

    @Inject
    public DeveloperPortalResourceImpl() {

    }

    public DeveloperPortalResourceImpl() {
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria) throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        LOG.debug("Searching for APIs by criteria {0}", criteria);
        return ;
    }
}
