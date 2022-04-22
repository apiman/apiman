package io.apiman.manager.api.service;

import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.beans.idm.PermissionConstraint;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.rest.impl.util.SearchCriteriaUtil;

import java.util.List;
import java.util.Set;
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

    private final PermissionConstraint PORTAL_CONSTRAINTS = PermissionConstraint.constrained().setAllowedDiscoverabilities(Set.of(DiscoverabilityLevel.PORTAL));

    private IStorage storage;
    private IStorageQuery query;

    @Inject
    public DevPortalService(IStorage storage, IStorageQuery query) {
        this.storage = storage;
        this.query = query;
    }

    public DevPortalService() {
    }

    public List<DeveloperApiPlanSummaryDto> getApiVersionPlans(String orgId, String apiId, String apiVersion) {
        List<ApiPlanSummaryBean> apiVersionPlans = tryAction(() -> query.getApiVersionPlans(orgId, apiId, apiVersion));
        return apiVersionPlans.stream()
                .filter(avp -> avp.getDiscoverability() == DiscoverabilityLevel.PORTAL)
                .map(psb -> toDto(orgId, psb))
                .collect(Collectors.toList());
    }

    public SearchResultsBean<ApiSummaryBean> findExposedApis(SearchCriteriaBean criteria) {
        SearchCriteriaUtil.validateSearchCriteria(criteria);
        return tryAction(() -> query.findApis(criteria, PORTAL_CONSTRAINTS, true));
    }

    /**
     * Get the featured APIs
     * @return list of featured ApiSummaries
     */
    public List<ApiSummaryBean> getFeaturedApis() {
        SearchCriteriaBean searchCriteria = new SearchCriteriaBean()
                .addFilter("tags.key", "featured", SearchCriteriaFilterOperator.eq);
        return tryAction(() -> query.findApis(searchCriteria, PORTAL_CONSTRAINTS, false).getBeans());
    }

    public List<ApiVersionPolicySummaryDto> getApiVersionPolicies(String orgId, String apiId, String apiVersion) {
        return tryAction(() -> Lists.newArrayList(storage.getAllPolicies(orgId, apiId, apiVersion, PolicyType.Api)))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
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
