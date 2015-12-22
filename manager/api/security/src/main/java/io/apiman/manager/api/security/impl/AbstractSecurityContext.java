/*
 * Copyright 2014 JBoss Inc
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

import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.security.ISecurityContext;
import io.apiman.manager.api.security.i18n.Messages;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for security context implementations.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractSecurityContext implements ISecurityContext {

    private static Logger logger = LoggerFactory.getLogger(AbstractSecurityContext.class);

    private static final ThreadLocal<IndexedPermissions> permissions = new ThreadLocal<>();

    @Inject
    private IStorageQuery query;

    /**
     * Constructor.
     */
    public AbstractSecurityContext() {
    }

    /**
     * @see io.apiman.manager.api.security.ISecurityContext#hasPermission(io.apiman.manager.api.beans.idm.PermissionType, java.lang.String)
     */
    @Override
    public boolean hasPermission(PermissionType permission, String organizationId) {
        // Admins can do everything.
        if (isAdmin())
            return true;
        return getPermissions().hasQualifiedPermission(permission, organizationId);
    }

    /**
     * @see io.apiman.manager.api.security.ISecurityContext#isMemberOf(java.lang.String)
     */
    @Override
    public boolean isMemberOf(String organizationId) {
        return getPermissions().isMemberOf(organizationId);
    }

    /**
     * @see io.apiman.manager.api.security.ISecurityContext#getPermittedOrganizations(io.apiman.manager.api.beans.idm.PermissionType)
     */
    @Override
    public Set<String> getPermittedOrganizations(PermissionType permission) {
        return getPermissions().getOrgQualifiers(permission);
    }

    /**
     * @return the user permissions for the current user
     */
    private IndexedPermissions getPermissions() {
        IndexedPermissions rval = permissions.get();
        if (rval == null) {
            rval = loadPermissions();
            permissions.set(rval);
        }
        return rval;
    }

    /**
     * Loads the current user's permissions into a thread local variable.
     */
    private IndexedPermissions loadPermissions() {
        String userId = getCurrentUser();
        try {
            return new IndexedPermissions(getQuery().getPermissions(userId));
        } catch (StorageException e) {
            logger.error(Messages.getString("AbstractSecurityContext.ErrorLoadingPermissions") + userId, e); //$NON-NLS-1$
            return new IndexedPermissions(new HashSet<PermissionBean>());
        }
    }

    /**
     * Called to clear the current thread local permissions bean.
     */
    protected static void clearPermissions() {
        permissions.remove();
    }

    /**
     * @return the query
     */
    public IStorageQuery getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(IStorageQuery query) {
        this.query = query;
    }

}
