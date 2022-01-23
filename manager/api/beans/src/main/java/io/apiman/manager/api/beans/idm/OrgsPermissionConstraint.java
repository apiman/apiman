package io.apiman.manager.api.beans.idm;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
// TODO(msavy): record candidate
public final class OrgsPermissionConstraint {
    private boolean constrained;
    private Set<String> permittedOrgs = Collections.emptySet();
    private PermissionType permissionType;
    private Set<DiscoverabilityLevel> allowedVisibilities = Collections.emptySet();

    public OrgsPermissionConstraint() {
    }

    public static OrgsPermissionConstraint constrained() {
        return new OrgsPermissionConstraint()
                .setConstrained(true);
    }

    public static OrgsPermissionConstraint unconstrained() {
        return new OrgsPermissionConstraint()
                .setConstrained(false);
    }

    public boolean isConstrained() {
        return constrained;
    }

    public OrgsPermissionConstraint setConstrained(boolean constrained) {
        this.constrained = constrained;
        return this;
    }

    @NotNull
    public Set<String> getPermittedOrgs() {
        return permittedOrgs;
    }

    public OrgsPermissionConstraint setPermittedOrgs(@NotNull Set<String> permittedOrgs) {
        this.permittedOrgs = permittedOrgs;
        return this;
    }

    @Nullable
    public PermissionType getPermissionType() {
        return permissionType;
    }

    public OrgsPermissionConstraint setPermissionType(@NotNull PermissionType permissionType) {
        this.permissionType = permissionType;
        return this;
    }

    public Set<DiscoverabilityLevel> getAllowedDiscoverabilities() {
        return allowedVisibilities;
    }

    public OrgsPermissionConstraint setAllowedDiscoverabilities(Set<DiscoverabilityLevel> allowedVisibilities) {
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
        OrgsPermissionConstraint that = (OrgsPermissionConstraint) o;
        return constrained == that.constrained && Objects.equals(permittedOrgs, that.permittedOrgs) && permissionType == that.permissionType
                       && Objects.equals(allowedVisibilities, that.allowedVisibilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constrained, permittedOrgs, permissionType, allowedVisibilities);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrgsPermissionConstraint.class.getSimpleName() + "[", "]")
                .add("constrained=" + constrained)
                .add("permittedOrgs=" + permittedOrgs)
                .add("permissionType=" + permissionType)
                .add("allowedVisibilities=" + allowedVisibilities)
                .toString();
    }
}