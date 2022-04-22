package io.apiman.manager.api.rest.impl.util;

import io.apiman.manager.api.beans.idm.PermissionConstraint;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.security.ISecurityContext;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class PermissionsHelper {
    public static PermissionConstraint orgConstraints(ISecurityContext securityContext, PermissionType permissionType) {
        return new PermissionConstraint()
                .setConstrained(!securityContext.isAdmin())
                .setAllowedDiscoverabilities(securityContext.getPermittedDiscoverabilities())
                .setPermittedOrgs(securityContext.getPermittedOrganizations(permissionType))
                .setPermissionType(permissionType);
    }
}
