package io.apiman.manager.api.service;

import io.apiman.manager.api.beans.idm.OrgsPermissionConstraint;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Search with permissions and visibility-related constraints.
 *
 * @see SearchCriteriaUtil
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
@ParametersAreNonnullByDefault
public class SearchService implements DataAccessUtilMixin {

    private IStorageQuery query;

    @Inject
    public SearchService(IStorageQuery query) {
        this.query = query;
    }

    public SearchService() {}

    public SearchResultsBean<OrganizationSummaryBean> findOrganizations(SearchCriteriaBean criteria, OrgsPermissionConstraint constraints) {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findOrganizations(criteria, constraints));
    }

    public SearchResultsBean<ClientSummaryBean> findClients(SearchCriteriaBean criteria, OrgsPermissionConstraint constraints) {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findClients(criteria, constraints));
    }

    public SearchResultsBean<ApiSummaryBean> findApis(SearchCriteriaBean criteria, OrgsPermissionConstraint constraints) {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findApis(criteria, constraints, true));
    }

    public SearchResultsBean<ApiSummaryBean> findAllFeaturedApis(OrgsPermissionConstraint constraints) {
        SearchCriteriaBean criteria = new SearchCriteriaBean().addFilter("tags.key", "featured", SearchCriteriaFilterOperator.eq);
        return tryAction(() -> query.findApis(criteria, constraints, false)); // TODO(msavy): when we update/drop H2 change paginate to true -- was generating invalid SQL.
    }

    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findUsers(criteria));
    }

    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findRoles(criteria));
    }
}
