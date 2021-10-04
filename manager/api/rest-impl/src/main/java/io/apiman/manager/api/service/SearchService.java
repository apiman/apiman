package io.apiman.manager.api.service;

import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.jetbrains.annotations.NotNull;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@Transactional
public class SearchService implements DataAccessUtilMixin {

    private static final SearchResultsBean<?> EMPTY_SEARCH_RESULTS = new SearchResultsBean<>();
    private IStorageQuery query;

    @Inject
    public SearchService(IStorageQuery query) {
        this.query = query;
    }

    public SearchService() {}

    public SearchResultsBean<OrganizationSummaryBean> findOrganizations(@NotNull SearchCriteriaBean criteria) {
        if (criteria.getFilters().isEmpty()) {
            return emptySearchResults();
        }
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findOrganizations(criteria));
    }

    public SearchResultsBean<ClientSummaryBean> findClients(@NotNull SearchCriteriaBean criteria) {
        if (criteria.getFilters().isEmpty()) {
            return emptySearchResults();
        }
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findClients(criteria));
    }

    public SearchResultsBean<ApiSummaryBean> findApis(@NotNull SearchCriteriaBean criteria) {
        if (criteria.getFilters().isEmpty()) {
            return emptySearchResults();
        }
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findApis(criteria));
    }

    public SearchResultsBean<UserBean> findUsers(@NotNull SearchCriteriaBean criteria) {
        if (criteria.getFilters().isEmpty()) {
            return emptySearchResults();
        }
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findUsers(criteria));
    }

    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findRoles(criteria));
    }

    @SuppressWarnings("unchecked")
    private <T> SearchResultsBean<T> emptySearchResults() {
        return (SearchResultsBean<T>) EMPTY_SEARCH_RESULTS;
    }
}
