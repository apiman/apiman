package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.apis.dto.ApiVersionBeanDto;
import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.rest.IDeveloperPortalResource;
import io.apiman.manager.api.rest.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.InvalidSearchCriteriaException;
import io.apiman.manager.api.rest.exceptions.NotAuthorizedException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.ISecurityContext.EntityType;
import io.apiman.manager.api.service.ApiService;
import io.apiman.manager.api.service.ApiService.ApiDefinitionStream;
import io.apiman.manager.api.service.DevPortalService;
import io.apiman.manager.api.service.OrganizationService;
import io.apiman.manager.api.service.PlanService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class DeveloperPortalResourceImpl implements IDeveloperPortalResource {

    private final IApimanLogger LOG = ApimanLoggerFactory.getLogger(DeveloperPortalResourceImpl.class);
    private static final Set<DiscoverabilityLevel> PORTAL_DISCOVERABILITY = Set.of(DiscoverabilityLevel.PORTAL);
    private ApiService apiService;
    private PlanService planService;
    private DevPortalService portalService;
    private OrganizationService orgService;
    private ISecurityContext securityContext;

    @Inject
    public DeveloperPortalResourceImpl(ApiService apiService,
                                       PlanService planService, DevPortalService portalService,
                                       OrganizationService orgService,
                                       ISecurityContext securityContext) {
        this.apiService = apiService;
        this.planService = planService;
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
        return portalService.getFeaturedApis();
    }

    @Override
    public List<ApiVersionSummaryBean> listApiVersions(String orgId, String apiId) {
        return apiService.listApiVersions(orgId, apiId).stream()
                .filter(av -> securityContext.isDiscoverable(EntityType.API, orgId, apiId, null, PORTAL_DISCOVERABILITY))
                .collect(Collectors.toList());
    }

    @Override
    public ApiVersionBeanDto getApiVersion(String orgId, String apiId, String apiVersion) {
        mustBeDiscoverable(EntityType.API, orgId, apiId, apiVersion);
        // TODO(msavy): make new projection for this.
        return RestHelper.hideSensitiveDataFromApiVersionBean(apiService.getApiVersion(orgId, apiId, apiVersion));
    }

    @Override
    public List<DeveloperApiPlanSummaryDto> getApiVersionPlans(String orgId, String apiId, String version) {
        mustBeDiscoverable(EntityType.API, orgId, apiId, version);
        return portalService.getApiVersionPlans(orgId, apiId, version)
                       .stream()
                       .filter(avp -> securityContext.isDiscoverable(EntityType.PLAN, orgId, avp.getPlanId(), avp.getVersion(), PORTAL_DISCOVERABILITY))
                       .collect(Collectors.toList());
    }

    @Override
    public OrganizationBean createHomeOrgForDeveloper(NewOrganizationBean newOrg) {
        mustBeLoggedIn();
        if (!newOrg.getName().equals(securityContext.getCurrentUser())) {
            throw new NotAuthorizedException("A developer's default org must be the same as their username. This restriction may be lifted later.");
        }
        return orgService.createOrg(newOrg);
    }

    @Override
    public List<ApiVersionPolicySummaryDto> listApiPolicies(String orgId, String apiId, String apiVersion)
            throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        mustBeDiscoverable(EntityType.API, orgId, apiId, apiVersion);
        return portalService.getApiVersionPolicies(orgId, apiId, apiVersion);
    }

    @Override
    public Response getApiDefinition(String orgId, String apiId, String apiVersion) throws ApiVersionNotFoundException {
        mustBeDiscoverable(EntityType.API, orgId, apiId, apiVersion);
        ApiDefinitionStream apiDef = apiService.getApiDefinition(orgId, apiId, apiVersion);
        return Response.ok()
                .entity(apiDef.getDefinition())
                .type(apiDef.getDefinitionType().getMediaType())
                .build();
    }

    @Override
    public List<PolicySummaryBean> listPlanPolicies(String orgId, String planId, String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        mustBeDiscoverable(EntityType.PLAN, orgId, planId, version);
        return planService.listPlanPolicies(orgId, planId, version);
    }

    @Override
    public PolicyBean getPlanPolicy(String orgId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        mustBeDiscoverable(EntityType.PLAN, orgId, planId, version);
        return planService.getPlanPolicy(orgId, planId, version, policyId);
    }

    private void mustBeDiscoverable(EntityType entityType, String orgId, String apiId, String apiVersion) {
        if (!securityContext.isDiscoverable(entityType, orgId, apiId, apiVersion, Set.of(DiscoverabilityLevel.PORTAL))) {
            throw ExceptionFactory.apiVersionNotFoundException(apiId, apiVersion);
        }
    }

    private void mustBeLoggedIn() {
        if (securityContext.getCurrentUser() == null) {
            throw ExceptionFactory.notAuthorizedException();
        }
    }
}
