package io.apiman.manager.api.service;

import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UpdateUserBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.rest.exceptions.UserNotFoundException;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.rest.impl.util.DataAccessUtilMixin;
import io.apiman.manager.api.security.impl.IndexedPermissions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Some of these arguably live in OrgService
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
@ApplicationScoped
@Transactional
public class UserService implements DataAccessUtilMixin {

    private IStorage storage;
    private IStorageQuery query;

    @Inject
    public UserService(IStorage storage, IStorageQuery query) {
        this.storage = storage;
        this.query = query;
    }

    public UserService() {
    }

    public UserBean getUserById(String userId) {
        return tryAction(() -> storage.getUser(userId));
    }

    // TODO(msavy): for IDM-driven solutions, we should lock this off.
    public void update(String userId, UpdateUserBean user) throws UserNotFoundException {
        UserBean updatedUser = tryAction(() -> storage.getUser(userId));
        if (updatedUser == null) {
            throw ExceptionFactory.userNotFoundException(userId);
        }
        if (user.getEmail() != null) {
            updatedUser.setEmail(user.getEmail());
        }
        if (user.getFullName() != null) {
            updatedUser.setFullName(user.getFullName());
        }
        if (user.getLocale() != null) {
            updatedUser.setLocale(user.getLocale());
        }
        tryAction(() -> storage.updateUser(updatedUser));
    }

    public List<OrganizationSummaryBean> getPermittedOrgs(String userId) {
        return tryAction(() -> {
            Set<String> permittedOrganizations = query.getUserMemberships(userId)
                 .stream()
                 .map(RoleMembershipBean::getOrganizationId)
                 .collect(Collectors.toSet());
            return query.getOrgs(permittedOrganizations);
        });
    }

    public Set<PermissionBean> getPermissions(String userId) {
        return tryAction(() -> query.getPermissions(userId));
    }

}
