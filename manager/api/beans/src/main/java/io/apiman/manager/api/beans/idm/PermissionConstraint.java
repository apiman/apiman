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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes organization-scoped permission constraints.
 * This is used for various types of search service to constrain the results to entities that are returned.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
// TODO(msavy): record candidate
@ParametersAreNonnullByDefault
public final class PermissionConstraint {
    private boolean constrained;
    private Set<String> permittedOrgs = Collections.emptySet();
    private PermissionType permissionType;
    private Set<DiscoverabilityLevel> allowedVisibilities = Collections.emptySet();

    public PermissionConstraint() {
    }

    /**
     * @return a constrained {@link PermissionConstraint}
     */
    public static PermissionConstraint constrained() {
        return new PermissionConstraint()
                .setConstrained(true);
    }

    /**
     * @return no constraints (e.g. for admins).
     */
    public static PermissionConstraint unconstrained() {
        return new PermissionConstraint()
                .setConstrained(false);
    }

    /**
     * @return true if there are any constraints
     */
    public boolean isConstrained() {
        return constrained;
    }

    /**
     * @param constrained true if permissions constrained
     */
    public PermissionConstraint setConstrained(boolean constrained) {
        this.constrained = constrained;
        return this;
    }

    /**
     * @return the IDs of all organizations the user is allowed to access
     */
    @NotNull
    public Set<String> getPermittedOrgs() {
        return permittedOrgs;
    }

    /**
     * @param permittedOrgs the IDs of all organizations the user is allowed to access
     */
    public PermissionConstraint setPermittedOrgs(Set<String> permittedOrgs) {
        this.permittedOrgs = permittedOrgs;
        return this;
    }

    /**
     * @return the permissions type that this constraint pertains to (may be null if unconstrained)
     */
    @Nullable
    public PermissionType getPermissionType() {
        return permissionType;
    }

    /**
     * @param permissionType the permission type this constraint pertains to
     */
    public PermissionConstraint setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
        return this;
    }

    /**
     * @return the allowed discoverability levels
     */
    public Set<DiscoverabilityLevel> getAllowedDiscoverabilities() {
        return allowedVisibilities;
    }

    /**
     * @param allowedVisibilities the set of discoverabilities allowed
     */
    public PermissionConstraint setAllowedDiscoverabilities(Set<DiscoverabilityLevel> allowedVisibilities) {
        this.allowedVisibilities = allowedVisibilities;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionConstraint that = (PermissionConstraint) o;
        return constrained == that.constrained && Objects.equals(permittedOrgs, that.permittedOrgs) && permissionType == that.permissionType
                       && Objects.equals(allowedVisibilities, that.allowedVisibilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constrained, permittedOrgs, permissionType, allowedVisibilities);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PermissionConstraint.class.getSimpleName() + "[", "]")
                .add("constrained=" + constrained)
                .add("permittedOrgs=" + permittedOrgs)
                .add("permissionType=" + permissionType)
                .add("allowedVisibilities=" + allowedVisibilities)
                .toString();
    }
}