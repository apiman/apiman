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
package org.overlord.apiman.dt.api.security.impl;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.overlord.apiman.dt.api.beans.idm.PermissionBean;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.persist.IIdmStorage;
import org.overlord.apiman.dt.api.persist.StorageException;
import org.overlord.apiman.dt.api.security.ISecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic/default implementation of a security context.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class DefaultSecurityContext implements ISecurityContext {

    private static Logger logger = LoggerFactory.getLogger(DefaultSecurityContext.class);

    private static final ThreadLocal<HttpServletRequest> servletRequest = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<IndexedPermissions> permissions = new ThreadLocal<IndexedPermissions>();
    
    @Inject IIdmStorage idmStorage;
    
    /**
     * Constructor.
     */
    public DefaultSecurityContext() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.security.ISecurityContext#getCurrentUser()
     */
    @Override
    public String getCurrentUser() {
        return servletRequest.get().getRemoteUser();
    }
    
    /**
     * @see org.overlord.apiman.dt.api.security.ISecurityContext#isAdmin()
     */
    @Override
    public boolean isAdmin() {
        // TODO warning - hard coded role value here
        return servletRequest.get().isUserInRole("apiadmin");
    }

    /**
     * @see org.overlord.apiman.dt.api.security.ISecurityContext#hasPermission(org.overlord.apiman.dt.api.beans.idm.PermissionType, java.lang.String)
     */
    @Override
    public boolean hasPermission(PermissionType permission, String organizationId) {
        // Admins can do everything.
        if (isAdmin())
            return true;
        return getPermissions().hasQualifiedPermission(permission.toString(), organizationId);
    }

    /**
     * @see org.overlord.apiman.dt.api.security.ISecurityContext#getPermittedOrganizations(org.overlord.apiman.dt.api.beans.idm.PermissionType)
     */
    @Override
    public Set<String> getPermittedOrganizations(PermissionType permission) {
        return getPermissions().getOrgQualifiers(permission.toString());
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
        String userId = servletRequest.get().getRemoteUser();
        try {
            return new IndexedPermissions(idmStorage.getPermissions(userId));
        } catch (StorageException e) {
            logger.error("Error loading permissions for: " + userId, e);
            return new IndexedPermissions(new HashSet<PermissionBean>());
        }
    }

    /**
     * Called to set the current context http servlet request.
     * @param request
     */
    protected static void setServletRequest(HttpServletRequest request) {
        servletRequest.set(request);
    }
    
    /**
     * Called to clear the current thread local permissions bean.
     */
    protected static void clearPermissions() {
        permissions.remove();
    }
    
    /**
     * Called to clear the context http servlet request.
     */
    protected static void clearServletRequest() {
        servletRequest.remove();
    }

}
