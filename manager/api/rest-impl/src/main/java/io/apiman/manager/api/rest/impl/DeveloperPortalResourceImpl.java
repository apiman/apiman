package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.rest.IDeveloperPortalResource;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.service.ApiService;
import io.apiman.manager.api.service.DevPortalService;
import io.apiman.manager.api.service.OrganizationService;
import io.apiman.manager.api.service.SearchService;

import java.util.List;
import java.util.stream.Collectors;
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
    private DevPortalService portalService;
    private OrganizationService orgService;
    private ISecurityContext securityContext;

    @Inject
    public DeveloperPortalResourceImpl(SearchService searchService, ApiService apiService, DevPortalService portalService, OrganizationService orgService,
                                       ISecurityContext securityContext) {
        this.searchService = searchService;
        this.apiService = apiService;
        this.portalService = portalService;
        this.orgService = orgService;
        this.securityContext = securityContext;
    }

    public DeveloperPortalResourceImpl() {
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> searchExposedApis(SearchCriteriaBean criteria) throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        LOG.debug("Searching for APIs by criteria {0}", criteria);
        return portalService.findExposedApis(criteria);
    }

    @Override
    public List<ApiSummaryBean> getFeaturedApis() {
        return apiService.getFeaturedApis();
    }

    @Override
    public List<ApiVersionSummaryBean> listApiVersions(String orgId, String apiId) {
        return apiService.listApiVersions(orgId, apiId).stream()
                .filter(ApiVersionSummaryBean::isExposeInPortal)
                .collect(Collectors.toList());
    }

    @Override
    public ApiVersionBean getApiVersion(String orgId, String apiId, String apiVersion) {
        ApiVersionBean retrieved = apiService.getApiVersion(orgId, apiId, apiVersion);
        if (!retrieved.isExposeInPortal()) {
            throw ExceptionFactory.apiVersionNotFoundException(apiId, apiVersion);
        }
        return retrieved;
    }

    @Override
    public List<DeveloperApiPlanSummaryDto> getApiVersionPlans(String orgId, String apiId, String version) {
        return portalService.getApiVersionPlans(orgId, apiId, version);
    }

    @Override
    public OrganizationBean createHomeOrgForDeveloper(NewOrganizationBean newOrg) {
        mustBeLoggedIn();
        if (!newOrg.getName().equals(securityContext.getCurrentUser())) {
            throw new IllegalArgumentException("Sorry, for now a developer's default org must be the same as their username. This restriction may be lifted later");
        }
        return orgService.createOrg(newOrg);
    }

    private void mustBeLoggedIn() {
        if (securityContext.getCurrentUser() == null) {
            throw ExceptionFactory.notAuthorizedException();
        }
    }
}
