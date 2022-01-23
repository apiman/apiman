package io.apiman.manager.api.beans.idm;

import java.util.EnumSet;
import java.util.Set;

//TODO(msavy): rename to Discoverability?
/**
 *
 *
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
