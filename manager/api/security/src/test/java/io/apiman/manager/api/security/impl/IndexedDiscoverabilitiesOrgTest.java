package io.apiman.manager.api.security.impl;

import io.apiman.manager.api.beans.idm.DiscoverabilityEntity;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class IndexedDiscoverabilitiesOrgTest {

    IndexedDiscoverabilities index = new IndexedDiscoverabilities();

    @Test
    public void get_all_org_discoverabilities() {
        List<DiscoverabilityEntity> oap = List.of(
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.FULL_PLATFORM_MEMBERS
                ),
                newDe(
                        "Org-Id-1",
                        "Plan-Id-2",
                        "Plan-Version-2",
                        "Api-Id-2",
                        "Api-Version-2",
                        DiscoverabilityLevel.ANONYMOUS
                ),
                newDe(
                        "Org-Id-X",
                        "Plan-Id-2",
                        "Plan-Version-2",
                        "Api-Id-2",
                        "should not find this one because the org id does not match",
                        DiscoverabilityLevel.ANONYMOUS
                )
        );
        index.index(oap);
        Map<String, DiscoverabilityLevel> orgVis = index.getAll("Org-Id-1").entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().getDiscoverability()
                ));

        assertThat(orgVis).contains(
                entry("Org-Id-1.API.Api-Id-1.VERSION.Api-Version-1", DiscoverabilityLevel.FULL_PLATFORM_MEMBERS),
                entry("Org-Id-1.PLAN.Plan-Id-1.VERSION.Plan-Version-1", DiscoverabilityLevel.FULL_PLATFORM_MEMBERS),
                entry("Org-Id-1.API.Api-Id-2.VERSION.Api-Version-2", DiscoverabilityLevel.ANONYMOUS),
                entry("Org-Id-1.PLAN.Plan-Id-2.VERSION.Plan-Version-2", DiscoverabilityLevel.ANONYMOUS)
        );
    }

    private DiscoverabilityEntity newDe(String orgId, String planId, String planVersion, String apiId, String apiVersion, DiscoverabilityLevel discoverability) {
        String id;
        if (planId == null) {
            id = String.join(":", orgId, apiId, apiVersion);
        } else {
            id = String.join(":", orgId, apiId, apiVersion, planId, planVersion);
        }
        return new DiscoverabilityEntity()
                .setId(id)
                .setOrgId(orgId)
                .setPlanId(planId)
                .setPlanVersion(planVersion)
                .setApiId(apiId)
                .setApiVersion(apiVersion)
                .setDiscoverability(discoverability);
    }
}
