/*
 * Copyright 2022. Black Parrot Labs Ltd
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

import io.apiman.manager.api.beans.idm.DiscoverabilityEntity;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;
import io.apiman.manager.api.security.ISecurityContext.EntityType;
import io.apiman.manager.api.security.impl.IndexedDiscoverabilities.DILookupResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import static io.apiman.manager.api.beans.idm.DiscoverabilityLevel.FULL_PLATFORM_MEMBERS;
import static io.apiman.manager.api.beans.idm.DiscoverabilityLevel.ORG_MEMBERS;
import static io.apiman.manager.api.beans.idm.DiscoverabilityLevel.PORTAL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Test cases for <a href="https://github.com/apiman/apiman/issues/2209">apiman#2209</a>.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class IndexedDiscoverabilitiesCombinedTest {
    IndexedDiscoverabilities index = new IndexedDiscoverabilities();

    @Test
    public void Basic_plan_attached_to_multiple_api_versions_should_inherit_highest_visibility_in_index() {
        index.index(List.of(
                discovEntity("Petstore:Metrics:1.0","Petstore","Metrics","1.0",null, null,ORG_MEMBERS), // Public API

                discovEntity("Petstore:Metrics:1.0:Gold:1.0","Petstore","Metrics","1.0","Gold","1.0",ORG_MEMBERS),
                discovEntity("Petstore:Metrics:1.0:Sandbox:1.0","Petstore","Metrics","1.0","Sandbox","1.0",ORG_MEMBERS),

                discovEntity("Petstore:Petstore:1.0","Petstore","Petstore","1.0", null, null,ORG_MEMBERS), // Public API
                discovEntity("Petstore:Petstore:1.0:Basic:1.0","Petstore","Petstore","1.0","Basic","1.0", PORTAL), // <-- important (same plan)

                discovEntity("Petstore:Test:1.0","Petstore","Test","1.0","","",ORG_MEMBERS),
                discovEntity("Petstore:Test:1.0:Basic:1.0","Petstore","Test","1.0","Basic","1.0",ORG_MEMBERS) // <-- important (same plan)
        ));

        DILookupResult apiResult = index.isAnyDiscoverable(EntityType.API, "Petstore", "Petstore", DiscoverabilityLevel.NON_MEMBERS);
        assertThat(apiResult).isEqualTo(DILookupResult.DISCOVERABLE);

        DILookupResult planResult = index.isAnyDiscoverable(EntityType.PLAN, "Petstore", "Basic", DiscoverabilityLevel.NON_MEMBERS);
        assertThat(planResult).isEqualTo(DILookupResult.DISCOVERABLE);
    }

    @Test
    public void Basic_plan_attached_to_multiple_api_versions_should_inherit_highest_visibility_in_index_2() {
        index.index(List.of(
                discovEntity("Petstore:Metrics:1.0","Petstore","Metrics","1.0",null, null,ORG_MEMBERS), // Public API

                discovEntity("Petstore:Metrics:1.0:Gold:1.0","Petstore","Metrics","1.0","Gold","1.0",ORG_MEMBERS),
                discovEntity("Petstore:Metrics:1.0:Sandbox:1.0","Petstore","Metrics","1.0","Sandbox","1.0",ORG_MEMBERS),

                discovEntity("Petstore:Petstore:1.0","Petstore","Petstore","1.0", null, null,ORG_MEMBERS), // Public API
                discovEntity("Petstore:Petstore:1.0:Basic:1.0","Petstore","Petstore","1.0","Basic","1.0", PORTAL), // <-- important (same plan)
                discovEntity("Petstore:Petstore2:1.0:Basic:1.0","Petstore","Petstore2","1.0","Basic","1.0", FULL_PLATFORM_MEMBERS), // <-- important (same plan)
                discovEntity("Petstore:Petstore:2.0:Basic:1.0","Petstore","Petstore","2.0","Basic","1.0", ORG_MEMBERS), // <-- important (same plan)

                discovEntity("Petstore:Test:1.0","Petstore","Test","1.0","","",ORG_MEMBERS),
                discovEntity("Petstore:Test:1.0:Basic:1.0","Petstore","Test","1.0","Basic","1.0",ORG_MEMBERS) // <-- important (same plan)
        ));

        DILookupResult apiResult = index.isAnyDiscoverable(EntityType.API, "Petstore", "Petstore", DiscoverabilityLevel.NON_MEMBERS);
        assertThat(apiResult).isEqualTo(DILookupResult.DISCOVERABLE);

        DILookupResult planResult = index.isAnyDiscoverable(EntityType.PLAN, "Petstore", "Basic", DiscoverabilityLevel.NON_MEMBERS);
        assertThat(planResult).isEqualTo(DILookupResult.DISCOVERABLE);
    }


    private DiscoverabilityEntity discovEntity(String id, String orgId, String apiId, String apiVersion, String planId, String planVersion, DiscoverabilityLevel discoverability) {
        return new DiscoverabilityEntity()
                       .setId(id)
                       .setOrgId(orgId)
                       .setApiId(apiId)
                       .setApiVersion(apiVersion)
                       .setPlanId(planId)
                       .setPlanVersion(planVersion)
                       .setDiscoverability(discoverability);
    }
}
