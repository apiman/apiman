package io.apiman.manager.api.service;

import io.apiman.manager.api.beans.developers.ApiVersionPolicySummaryDto;
import io.apiman.manager.api.beans.developers.DeveloperApiPlanSummaryDto;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;

import java.util.List;
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
                .map(psb -> toDto(orgId, psb))
                .collect(Collectors.toList());
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
