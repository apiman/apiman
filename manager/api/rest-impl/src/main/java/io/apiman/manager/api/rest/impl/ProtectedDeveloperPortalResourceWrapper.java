package io.apiman.manager.api.rest.impl;

import io.apiman.manager.api.beans.apis.dto.ApiVersionBeanDto;
import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionEndpointSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.rest.IDeveloperPortalResource;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidApiStatusException;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
@Path("devportal/protected")
@PermitAll
public class ProtectedDeveloperPortalResourceWrapper implements IDeveloperPortalResource {

    private DeveloperPortalResourceImpl delegate;

    @Inject
    public ProtectedDeveloperPortalResourceWrapper(DeveloperPortalResourceImpl developerPortalResource) {
        this.delegate = developerPortalResource;
    }

    public ProtectedDeveloperPortalResourceWrapper() {
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria) throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        return delegate.searchApis(criteria);
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> getFeaturedApis() {
        return delegate.getFeaturedApis();
    }

    @Override
    public List<ApiVersionSummaryBean> listApiVersions(String orgId, String apiId) {
        return delegate.listApiVersions(orgId, apiId);
    }

    @Override
    public ApiVersionBeanDto getApiVersion(String orgId, String apiId, String apiVersion) {
        return delegate.getApiVersion(orgId, apiId, apiVersion);
    }

    @Override
    public List<DeveloperApiPlanSummaryDto> getApiVersionPlans(String orgId, String apiId, String apiVersion) {
        return delegate.getApiVersionPlans(orgId, apiId, apiVersion);
    }

    @Override
    public Response createHomeOrgForDeveloper(NewOrganizationBean newOrg) {
        return delegate.createHomeOrgForDeveloper(newOrg);
    }

    @Override
    public List<ApiVersionPolicySummaryDto> listApiPolicies(String orgId, String apiId, String apiVersion)
            throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        return delegate.listApiPolicies(orgId, apiId, apiVersion);
    }

    @Override
    public Response getApiDefinition(String orgId, String apiId, String apiVersion) throws ApiVersionNotFoundException {
        return delegate.getApiDefinition(orgId, apiId, apiVersion);
    }

    @Override
    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(String orgId, String apiId, String apiVersion)
            throws ApiVersionNotFoundException, InvalidApiStatusException, GatewayNotFoundException {
        return delegate.getApiVersionEndpointInfo(orgId, apiId, apiVersion);
    }

    @Override
    public List<PolicySummaryBean> listPlanPolicies(String orgId, String planId, String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        return delegate.listPlanPolicies(orgId, planId, version);
    }

    @Override
    public PolicyBean getPlanPolicy(String orgId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        return delegate.getPlanPolicy(orgId, planId, version, policyId);
    }
}
