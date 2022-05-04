package io.apiman.manager.api.service;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.manager.api.beans.BeanUtils;
import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.orgs.NewOrganizationBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.exceptions.OrganizationAlreadyExistsException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import com.google.common.collect.Lists;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class DevPortalService implements DataAccessUtilMixin {

    private static final IApimanLogger LOG = ApimanLoggerFactory.getLogger(DevPortalService.class);
    private OrganizationService orgService;
    private IStorage storage;
    private IStorageQuery query;
    private ISecurityContext securityContext;

    @Inject
    public DevPortalService(OrganizationService orgService, IStorage storage, IStorageQuery query, ISecurityContext securityContext) {
        this.orgService = orgService;
        this.storage = storage;
        this.query = query;
        this.securityContext = securityContext;
    }

    public DevPortalService() {
    }

    public List<DeveloperApiPlanSummaryDto> getApiVersionPlans(String orgId, String apiId, String apiVersion) {
        List<ApiPlanSummaryBean> apiVersionPlans = tryAction(() -> query.getApiVersionPlans(orgId, apiId, apiVersion));
        return apiVersionPlans.stream()
                .map(psb -> toDto(orgId, psb))
                .collect(Collectors.toList());
    }

    public List<ApiVersionPolicySummaryDto> getApiVersionPolicies(String orgId, String apiId, String apiVersion) {
        return tryAction(() -> Lists.newArrayList(storage.getAllPolicies(orgId, apiId, apiVersion, PolicyType.Api)))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public OrganizationBean createHomeOrg(NewOrganizationBean newOrg) {
        OrganizationBean existingOrg = tryAction(() -> storage.getOrganization(BeanUtils.idFromName(newOrg.getName())));
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
        LOG.info("Creating home org {0} for {1}...", newOrg.getName(), securityContext.getCurrentUser());
        return orgService.createOrg(newOrg);
    }
    // TODO Use mapstruct
    private DeveloperApiPlanSummaryDto toDto(String orgId, ApiPlanSummaryBean apiPsb) {
        List<PolicyBean> planPolicies = tryAction(() -> Lists.newArrayList(storage.getAllPolicies(orgId, apiPsb.getPlanId(), apiPsb.getVersion(), PolicyType.Plan)));
        return new DeveloperApiPlanSummaryDto()
                .setPlanId(apiPsb.getPlanId())
                .setPlanName(apiPsb.getPlanName())
                .setPlanDescription(apiPsb.getPlanDescription())
                .setVersion(apiPsb.getVersion())
                .setRequiresApproval(apiPsb.getRequiresApproval())
                .setPlanPolicies(planPolicies)
                .setDiscoverability(apiPsb.getDiscoverability());
    }

    // TODO use mapstruct
    private ApiVersionPolicySummaryDto toDto(PolicyBean psb) {
        ApiVersionPolicySummaryDto summary = new ApiVersionPolicySummaryDto();
        summary.setPolicyConfiguration(psb.getConfiguration());
        summary.setPolicyDefinitionId(psb.getDefinition().getId());
        summary.setId(psb.getId());
        summary.setName(psb.getName());
        summary.setDescription(psb.getDescription());
        summary.setIcon(psb.getDefinition().getIcon());
        summary.setCreatedBy(psb.getCreatedBy());
        summary.setCreatedOn(psb.getCreatedOn());
        return summary;
    }

}
