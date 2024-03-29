package io.apiman.manager.api.security.impl;

import io.apiman.manager.api.beans.idm.DiscoverabilityEntity;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.security.ISecurityContext.EntityType;
import io.apiman.manager.api.security.impl.IndexedDiscoverabilities.DILookupResult;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class IndexedDiscoverabilitiesOrderingTest {
    IndexedDiscoverabilities index = new IndexedDiscoverabilities();

    @Test
    public void is_order_insensitive_AB() {
        index.index(List.of(
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.PORTAL
                ),
                newDe(
                        "Org-Id-1",
                        null,
                        null,
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.ORG_MEMBERS
                )
        ));

        DILookupResult result = index.isDiscoverable(EntityType.API, "Org-Id-1", "Api-Id-1", "Api-Version-1", Set.of(DiscoverabilityLevel.PORTAL));
        assertThat(result).isEqualTo(DILookupResult.DISCOVERABLE);
    }

    @Test
    public void is_order_insensitive_BA() {
        index.index(List.of(
                newDe(
                        "Org-Id-1",
                        null,
                        null,
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.ORG_MEMBERS
                ),
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.PORTAL
                )
        ));

        DILookupResult result = index.isDiscoverable(EntityType.API, "Org-Id-1", "Api-Id-1", "Api-Version-1", Set.of(DiscoverabilityLevel.PORTAL));
        assertThat(result).isEqualTo(DILookupResult.DISCOVERABLE);
    }

    @Test
    public void use_most_discoverable_level_for_parent() {
        index.index(List.of(
                newDe(
                        "Org-Id-1",
                        null,
                        null,
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.PORTAL
                ),
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.ORG_MEMBERS
                )
        ));

        DILookupResult result = index.isDiscoverable(EntityType.API, "Org-Id-1", "Api-Id-1", "Api-Version-1", Set.of(DiscoverabilityLevel.PORTAL));
        assertThat(result).isEqualTo(DILookupResult.DISCOVERABLE);
    }

    @Test
    public void use_most_discoverable_level_for_parent_2() {
        index.index(List.of(
                newDe(
                        "Org-Id-1",
                        null,
                        null,
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.FULL_PLATFORM_MEMBERS
                ),
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.PORTAL
                )
        ));

        DILookupResult result = index.isDiscoverable(EntityType.API, "Org-Id-1", "Api-Id-1", "Api-Version-1", Set.of(DiscoverabilityLevel.PORTAL));
        assertThat(result).isEqualTo(DILookupResult.DISCOVERABLE);
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
