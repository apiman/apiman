/*
 * Copyright 2022 Black Parrot Labs Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.security.impl;

import io.apiman.manager.api.beans.idm.DiscoverabilityDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityEntity;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.beans.idm.DiscoverabilityMapper;
import io.apiman.manager.api.security.ISecurityContext.EntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.apiman.manager.api.security.ISecurityContext.EntityType.API;
import static io.apiman.manager.api.security.ISecurityContext.EntityType.PLAN;

/**
 * Index of 'discoverabilities' to allow rapid and lookup based upon:
 *
 * <ul>
 *     <li>Organization ID (orgId)</li>
 *     <li>Organization ID (orgId) + entity type (API, Plan)</li>
 *     <li>Organization ID (orgId) + entity type (API, Plan) + entity's unique identifier (entityId)</li>
 *     <li>Organization ID (orgId) + entity type (API, Plan) + entity's unique identifier (entityId) + entity's version (entityVersion)</li>
 * </ul>
 *
 * <p>The current implementation uses a PATRICIA trie to optimise space lookup without compromising lookup speed much.
 * This is a specialised type of radix or prefix trie.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ParametersAreNonnullByDefault
public class IndexedDiscoverabilities {

    private final PatriciaTrie<DiscoverabilityDto> discoverabilityIndex = new PatriciaTrie<>();
    private final DiscoverabilityMapper mapper = DiscoverabilityMapper.INSTANCE;

    public IndexedDiscoverabilities() {
    }

    /**
     * Add some DiscoverabilityDtos to the index
     *
     * @param discoverabilityEntities views to index
     */
    public void index(Collection<DiscoverabilityEntity> discoverabilityEntities) {

        List<DiscoverabilityDto> dtos = new ArrayList<>(mapper.toDto(discoverabilityEntities));
        Collections.sort(dtos, Comparator.comparing(DiscoverabilityDto::getId));

        for (DiscoverabilityDto DiscoverabilityDto : dtos) {
            discoverabilityIndex.put(createApiLookupKey(DiscoverabilityDto), DiscoverabilityDto);
            // If it's a public API, it could be that it has no attached plans.
            if (DiscoverabilityDto.getPlanId() != null && DiscoverabilityDto.getPlanVersion() != null) {
                discoverabilityIndex.put(createPlanLookupKey(DiscoverabilityDto), DiscoverabilityDto);
            }
        }
    }

    public @NotNull SortedMap<String, DiscoverabilityDto> getAll(String orgId) {
        Validate.notBlank(orgId, "orgId must not be blank");
        return discoverabilityIndex.prefixMap(createLookupKey(orgId));
    }

    public @NotNull SortedMap<String, DiscoverabilityDto> getAll(EntityType entityType, String orgId) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Validate.notBlank(orgId, "orgId must not be blank");
        return discoverabilityIndex.prefixMap(createLookupKey(entityType, orgId));
    }

    public @NotNull SortedMap<String, DiscoverabilityDto> getAll(EntityType entityType, String orgId, String entityId) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Validate.notBlank(orgId, "orgId must not be blank");
        return discoverabilityIndex.prefixMap(createLookupKey(entityType, orgId, entityId));
    }

    public @Nullable DiscoverabilityDto get(EntityType entityType, String orgId, String entityId, String entityVersion) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Validate.notBlank(orgId, "orgId must not be blank");
        return discoverabilityIndex.get(createLookupKey(entityType, orgId, entityId, entityVersion));
    }

    public @NotNull IndexedDiscoverabilities.DILookupResult isDiscoverable(EntityType entityType, String orgId, String entityId, String entityVersion, Set<DiscoverabilityLevel> discoverabilities) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Validate.notBlank(orgId, "orgId must not be blank");
        Validate.notBlank(entityId, "entityId must not be blank");
        Validate.notBlank(entityVersion, "entityVersion must not be blank");

        String key = createLookupKey(entityType, orgId, entityId, entityVersion);
        DiscoverabilityDto result = discoverabilityIndex.get(key);
        if (result == null) {
            return DILookupResult.NOT_IN_INDEX;
        } else {
            if (discoverabilities.contains(result.getDiscoverability())) {
                return DILookupResult.DISCOVERABLE;
            } else {
                return DILookupResult.NOT_DISCOVERABLE;
            }
        }
    }

    public @NotNull IndexedDiscoverabilities.DILookupResult isAnyDiscoverable(EntityType entityType, String orgId, String entityId, Set<DiscoverabilityLevel> discoverabilities) {
        Objects.requireNonNull(entityType, "entityType must not be null");
        Validate.notBlank(orgId, "orgId must not be blank");
        Validate.notBlank(entityId, "entityId must not be blank");

        String key = createLookupKey(entityType, orgId, entityId);

        // TODO(msavy): can we do this check without creating a new map? Would be nice...
        List<DiscoverabilityLevel> prefixDiscoverabilities = discoverabilityIndex.prefixMap(key).values()
                .stream()
                .map(DiscoverabilityDto::getDiscoverability)
                .collect(Collectors.toList());

        if (prefixDiscoverabilities.isEmpty()) {
            return DILookupResult.NOT_IN_INDEX;
        } else {
            if (Collections.disjoint(prefixDiscoverabilities, discoverabilities)) {
                return DILookupResult.NOT_DISCOVERABLE;
            } else {
                return DILookupResult.DISCOVERABLE;
            }
        }
    }

    private String createLookupKey(String orgId) {
        return String.join(".", orgId);
    }

    private String createLookupKey(EntityType entityType, String orgId) {
        return String.join(".", orgId, entityType.name());
    }

    private String createLookupKey(EntityType entityType, String orgId, String entityId) {
        return String.join(".", orgId, entityType.name(), entityId);
    }

    private String createLookupKey(EntityType entityType, String orgId, String entityId, String entityVersion) {
        return String.join(".", orgId, entityType.name(), entityId, "VERSION", entityVersion);
    }

    /**
     * OrgId.api.ApiId.version.ApiVersion. For example, FooOrg.api.CoolApi.version.1
     */
    private String createApiLookupKey(DiscoverabilityDto DiscoverabilityDto) {
        return String.join(".", DiscoverabilityDto.getOrgId(), API.name(), DiscoverabilityDto.getApiId(), "VERSION", DiscoverabilityDto.getApiVersion());
    }

    /**
     * OrgId.plan.PlanId.version.PlanVersion. For example, FooOrg.plan.Plan123.version.1
     */
    private String createPlanLookupKey(DiscoverabilityDto DiscoverabilityDto) {
        return  String.join(".", DiscoverabilityDto.getOrgId(), PLAN.name(), DiscoverabilityDto.getPlanId(), "VERSION", DiscoverabilityDto.getPlanVersion());
    }

    /**
     * Discoverability index lookup result
     */
    public enum DILookupResult {

        /**
         * Has a visibility of queried values
         */
        DISCOVERABLE,

        /**
         * Has a visibility of queried values
         */

        NOT_DISCOVERABLE,

        /**
         * No entry exists for query (may need to be re-queried or is 404).
         */
        NOT_IN_INDEX
    }
}
