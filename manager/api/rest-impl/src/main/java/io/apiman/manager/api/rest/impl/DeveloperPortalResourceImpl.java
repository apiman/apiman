package io.apiman.manager.api.rest.impl;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.apis.dto.ApiPlanBeanDto;
import io.apiman.manager.api.beans.apis.dto.ApiVersionBeanDto;
import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
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
import io.apiman.manager.api.rest.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.OrganizationNotFoundException;
import io.apiman.manager.api.rest.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.exceptions.PolicyNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.PermissionsHelper;
import io.apiman.manager.api.rest.impl.util.RestHelper;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.ISecurityContext.EntityType;
import io.apiman.manager.api.service.ApiService;
import io.apiman.manager.api.service.ApiService.ApiDefinitionStream;
import io.apiman.manager.api.service.DevPortalService;
import io.apiman.manager.api.service.OrganizationService;
import io.apiman.manager.api.service.PlanService;
import io.apiman.manager.api.service.SearchService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private ApiService apiService;
    private PlanService planService;
    private DevPortalService portalService;
    private OrganizationService orgService;
    private SearchService searchService;
    private ISecurityContext securityContext;

    @Inject
    public DeveloperPortalResourceImpl(ApiService apiService,
                                       PlanService planService,
                                       DevPortalService portalService,
                                       OrganizationService orgService,
                                       SearchService searchService,
                                       ISecurityContext securityContext) {
        this.apiService = apiService;
        this.planService = planService;
        this.portalService = portalService;
        this.orgService = orgService;
        this.searchService = searchService;
        this.securityContext = securityContext;
    }

    public DeveloperPortalResourceImpl() {
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> searchApis(SearchCriteriaBean criteria) throws OrganizationNotFoundException, InvalidSearchCriteriaException {
        LOG.debug("Searching for APIs by criteria {0}", criteria);
        return searchService.findApis(criteria, PermissionsHelper.orgConstraints(securityContext, PermissionType.apiView));
    }

    @Override
    public SearchResultsBean<ApiSummaryBean> getFeaturedApis() {
        LOG.debug("Getting all featured APIs");
        return searchService.findAllFeaturedApis(PermissionsHelper.orgConstraints(securityContext, PermissionType.apiView));
    }

    @Override
    public List<ApiVersionSummaryBean> listApiVersions(String orgId, String apiId) {
        LOG.debug("Listing all API versions");
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.API,
                orgId,
                apiId,
                Set.of(PermissionType.apiView)
        );

        return apiService.listApiVersions(orgId, apiId).stream()
                .filter(av -> securityContext.hasPermissionsOrDiscoverable(EntityType.API, orgId, apiId, av.getVersion(), Set.of(PermissionType.apiView)))
                .collect(Collectors.toList());
    }

    @Override
    public ApiVersionBeanDto getApiVersion(String orgId, String apiId, String apiVersion) {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.API,
                orgId,
                apiId,
                apiVersion,
                Set.of(PermissionType.apiView)
        );

        ApiVersionBeanDto v = apiService.getApiVersion(orgId, apiId, apiVersion);

        // TODO(msavy): probably a nicer way of doing this.
        Set<ApiPlanBeanDto> filteredPlans = v.getPlans()
                       .stream()
                       .filter(ap -> securityContext.hasPermission(PermissionType.planView, orgId) || permittedDiscoverability(ap.getDiscoverability()))
                       .collect(Collectors.toSet());

        v.setPlans(filteredPlans);

        // TODO(msavy): make new projection for this.
        return RestHelper.hideSensitiveDataFromApiVersionBean(v);
    }

    @Override
    public List<DeveloperApiPlanSummaryDto> getApiVersionPlans(String orgId, String apiId, String apiVersion) {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.API,
                orgId,
                apiId,
                apiVersion,
                Set.of(PermissionType.apiView)
        );
        return portalService.getApiVersionPlans(orgId, apiId, apiVersion)
                       .stream()
                       .filter(ap -> securityContext.hasPermission(PermissionType.planView, orgId) || permittedDiscoverability(ap.getDiscoverability()))
                       .collect(Collectors.toList());
    }

    @Override
    public Response createHomeOrgForDeveloper(NewOrganizationBean newOrg) {
        mustBeLoggedIn();
        if (!newOrg.getName().equals(securityContext.getCurrentUser())) {
            return Response.status(422, "A developer's default org must be the same as their username. This restriction may be lifted later.").build();
        }

        OrganizationBean existingOrg;
        try {
            existingOrg = orgService.getOrg(BeanUtils.idFromName(newOrg.getName()));
        } catch (OrganizationNotFoundException onfe) {
            existingOrg = null;
        }

        if (existingOrg != null) {
            // First check who owns the existing organization, otherwise we could get into trouble by letting people spam create orgs.
            if (securityContext.hasPermission(PermissionType.clientEdit, existingOrg.getId())) {
                OrganizationAlreadyExistsException ex = ExceptionFactory.organizationAlreadyExistsException(existingOrg.getName());
                LOG.error(ex, "Tried to create a new home org for the developer, but one already exists where they have clientEdit permissions");
                throw ex;
            }
            // Use a name with a randomised suffix in the case that someone already created an organization with a user's name (e.g. FooUser-70ac3d)
            String newOrgId = newOrg.getName() + UUID.randomUUID().toString().substring(0, 6);
            LOG.warn("We tried to create a home organization for the user {0}, but it already existed. "
                             + "This is likely due to another user coincidentally creating an org with the same name "
                             + "An organization with a random suffix will be created: {1}.", securityContext.getCurrentUser(), newOrgId);
            newOrg.setName(newOrgId);
        }
        LOG.info("Created home org {0} for {1}", newOrg.getName(), securityContext.getCurrentUser());
        return Response.ok(orgService.createOrg(newOrg)).build();
    }

    @Override
    public List<ApiVersionPolicySummaryDto> listApiPolicies(String orgId, String apiId, String apiVersion)
            throws OrganizationNotFoundException, ApiVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.API,
                orgId,
                apiId,
                apiVersion,
                Set.of(PermissionType.apiView)
        );
        return portalService.getApiVersionPolicies(orgId, apiId, apiVersion);
    }

    @Override
    public Response getApiDefinition(String orgId, String apiId, String apiVersion) throws ApiVersionNotFoundException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.API,
                orgId,
                apiId,
                apiVersion,
                Set.of(PermissionType.apiView)
        );
        ApiDefinitionStream apiDef = apiService.getApiDefinition(orgId, apiId, apiVersion);
        return Response.ok()
                .entity(apiDef.getDefinition())
                .type(apiDef.getDefinitionType().getMediaType())
                .build();
    }

    @Override
    public ApiVersionEndpointSummaryBean getApiVersionEndpointInfo(String orgId, String apiId, String apiVersion) throws ApiVersionNotFoundException, InvalidApiStatusException, GatewayNotFoundException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.API,
                orgId,
                apiId,
                apiVersion,
                Set.of(PermissionType.apiView)
        );
        return apiService.getApiVersionEndpointInfo(orgId, apiId, apiVersion);
    }

    @Override
    public List<PolicySummaryBean> listPlanPolicies(String orgId, String planId, String version)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                orgId,
                planId,
                version,
                Set.of(PermissionType.planView)
        );
        return planService.listPlanPolicies(orgId, planId, version);
    }

    @Override
    public PolicyBean getPlanPolicy(String orgId, String planId, String version, long policyId)
            throws OrganizationNotFoundException, PlanVersionNotFoundException, PolicyNotFoundException, NotAuthorizedException {
        securityContext.checkPermissionsOrDiscoverability(
                EntityType.PLAN,
                orgId,
                planId,
                version,
                Set.of(PermissionType.planView)
        );
        return planService.getPlanPolicy(orgId, planId, version, policyId);
    }

    private void mustBeLoggedIn() {
        if (securityContext.getCurrentUser() == null) {
            throw ExceptionFactory.notAuthorizedException();
        }
    }

    boolean permittedDiscoverability(DiscoverabilityLevel dl) {
        return securityContext.getPermittedDiscoverabilities().contains(dl);
    }

}
