/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.idm;

import java.util.EnumSet;
import java.util.Set;

/**
 * Discoverability is an implicit permissions system that allows entities to be selectively exposed non-members in a granular way.
 *
 * @see DiscoverabilityEntity
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public enum DiscoverabilityLevel {

    /**
     * Entity is exposed in the developer portal
     */
    PORTAL,

    /**
     * Anonymous access is allowed
     */
    ANONYMOUS,

    /**
     * Only users registered & logged in with the IDM solution that are full members of the platform.
     *
     * The precise meaning of this is defined by the user's setup (e.g: IDM role mappings, visibility config, etc.)
     */
    FULL_PLATFORM_MEMBERS,

    /**
     * Only users with explicit org view permissions.
     */
    ORG_MEMBERS;

    public static final Set<DiscoverabilityLevel> NON_MEMBERS = EnumSet.of(PORTAL, ANONYMOUS, FULL_PLATFORM_MEMBERS);

    public static DiscoverabilityLevel toValue(String value) {
        return Enum.valueOf(DiscoverabilityLevel.class, value.toUpperCase());
    }
}
