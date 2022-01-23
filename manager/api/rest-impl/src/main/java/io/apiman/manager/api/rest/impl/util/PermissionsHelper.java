package io.apiman.manager.api.rest.impl.util;

import io.apiman.manager.api.beans.idm.OrgsPermissionConstraint;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.security.ISecurityContext;

/**
 *
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class PermissionsHelper {
    public static OrgsPermissionConstraint orgConstraints(ISecurityContext securityContext, PermissionType permissionType) {
        return new OrgsPermissionConstraint()
                .setConstrained(!securityContext.isAdmin())
                .setAllowedDiscoverabilities(securityContext.getPermittedDiscoverabilities())
                .setPermittedOrgs(securityContext.getPermittedOrganizations(permissionType))
                .setPermissionType(permissionType);
    }
}
