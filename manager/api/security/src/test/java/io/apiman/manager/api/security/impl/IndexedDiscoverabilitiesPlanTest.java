package io.apiman.manager.api.security.impl;

import io.apiman.manager.api.beans.idm.DiscoverabilityEntity;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.security.ISecurityContext.EntityType;
import io.apiman.manager.api.security.impl.IndexedDiscoverabilities.DILookupResult;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class IndexedDiscoverabilitiesPlanTest {

    IndexedDiscoverabilities index = new IndexedDiscoverabilities();

    @Test
    public void empty_index_returns_not_in_index() {
        index.index(Collections.emptyList());
        DILookupResult discoverability = index.isDiscoverable(EntityType.PLAN, "non-existent", "a", "a", Set.of(DiscoverabilityLevel.ANONYMOUS));
        assertThat(discoverability).isEqualTo(DILookupResult.NOT_IN_INDEX);
    }

    @Test
    public void null_orgId_throws_NPE() {
        index.index(Collections.emptyList());

        Assertions.assertThrows(NullPointerException.class, () -> {
            index.isDiscoverable(EntityType.PLAN, null, "a", "a", Set.of(DiscoverabilityLevel.ANONYMOUS));
        });
    }

    @Test
    public void blank_orgId_throws_IAE() {
        index.index(Collections.emptyList());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            index.isDiscoverable(EntityType.PLAN, "", "a", "a", Set.of(DiscoverabilityLevel.ANONYMOUS));
        });
    }

    @Test
    public void null_entityId_throws_NPE() {
        index.index(Collections.emptyList());

        Assertions.assertThrows(NullPointerException.class, () -> {
            index.isDiscoverable(EntityType.PLAN, "test", null, "a", Set.of(DiscoverabilityLevel.ANONYMOUS));
        });
    }

    @Test
    public void blank_entityId_throws_IAE() {
        index.index(Collections.emptyList());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            index.isDiscoverable(EntityType.PLAN, "test", "", "a", Set.of(DiscoverabilityLevel.ANONYMOUS));
        });
    }

    @Test
    public void REGISTERED_USERS_query_with_org_apiId_with_REGISTERED_USERS_discoverability_is_DISCOVERABLE() {
        index.index(List.of(
            newDe(
                    "Org-Id-1",
                    "Plan-Id-1",
                    "Plan-Version-1",
                    "Api-Id-1",
                    "Api-Version-1",
                    DiscoverabilityLevel.FULL_PLATFORM_MEMBERS
            )
        ));

        DILookupResult result = index.isAnyDiscoverable(EntityType.PLAN, "Org-Id-1", "Plan-Id-1", Set.of(DiscoverabilityLevel.FULL_PLATFORM_MEMBERS));
        assertThat(result).isEqualTo(DILookupResult.DISCOVERABLE);
    }

    @Test
    public void REGISTERED_USERS_query_with_org_apiId_with_ORG_MEMBERS_discoverability_is_NOT_VISIBLE() {
        index.index(List.of(
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.ORG_MEMBERS
                )
        ));

        DILookupResult result = index.isAnyDiscoverable(EntityType.PLAN, "Org-Id-1", "Plan-Id-1", Set.of(DiscoverabilityLevel.FULL_PLATFORM_MEMBERS));
        assertThat(result).isEqualTo(DILookupResult.NOT_DISCOVERABLE);
    }

    @Test
    public void ORG_MEMBERS_query_with_org_apiId_apiVersion_with_ORG_MEMBERS_discoverability_is_DISCOVERABLE() {
        index.index(List.of(
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.ORG_MEMBERS
                )
        ));

        DILookupResult result = index.isDiscoverable(EntityType.PLAN, "Org-Id-1", "Plan-Id-1", "Plan-Version-1", Set.of(DiscoverabilityLevel.ORG_MEMBERS));
        assertThat(result).isEqualTo(DILookupResult.DISCOVERABLE);
    }

    @Test
    public void NON_MEMBERS_query_with_org_apiId_with_ORG_MEMBERS_discoverability_returns_NOT_VISIBLE() {
        index.index(List.of(
                newDe(
                        "Org-Id-1",
                        "Plan-Id-1",
                        "Plan-Version-1",
                        "Api-Id-1",
                        "Api-Version-1",
                        DiscoverabilityLevel.ORG_MEMBERS
                )
        ));

        DILookupResult result = index.isAnyDiscoverable(EntityType.PLAN, "Org-Id-1", "Plan-Id-1", DiscoverabilityLevel.NON_MEMBERS);
        assertThat(result).isEqualTo(DILookupResult.NOT_DISCOVERABLE);
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