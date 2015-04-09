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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A class that optimizes the user permissions for querying.
 *
 * @author eric.wittmann@redhat.com
 */
public class IndexedPermissions implements Serializable {
    
    private static final long serialVersionUID = -474966481686691421L;
    
    private Set<String> qualifiedPermissions = new HashSet<>();
    private Map<PermissionType, Set<String>> permissionToOrgsMap = new HashMap<>();

    /**
     * Constructor.
     * @param permissions the permissions
     */
    public IndexedPermissions(Set<PermissionBean> permissions) {
        index(permissions);
    }

    /**
     * Returns true if the qualified permission exists.
     * @param permissionName the permission name
     * @param orgQualifier the org qualifier
     * @return true if has qualified permission
     */
    public boolean hasQualifiedPermission(PermissionType permissionName, String orgQualifier) {
        String key = createQualifiedPermissionKey(permissionName, orgQualifier);
        return qualifiedPermissions.contains(key);
    }
    
    /**
     * Given a permission name, returns all organization qualifiers.
     * @param permissionName the permission type
     * @return set of org qualifiers
     */
    public Set<String> getOrgQualifiers(PermissionType permissionName) {
        Set<String> orgs = permissionToOrgsMap.get(permissionName);
        if (orgs == null)
            orgs = Collections.EMPTY_SET;
        return Collections.unmodifiableSet(orgs);
    }

    /**
     * Index the permissions.
     * @param bean
     */
    private void index(Set<PermissionBean> permissions) {
        for (PermissionBean permissionBean : permissions) {
            PermissionType permissionName = permissionBean.getName();
            String orgQualifier = permissionBean.getOrganizationId();
            String qualifiedPermission = createQualifiedPermissionKey(permissionName, orgQualifier);
            qualifiedPermissions.add(qualifiedPermission);
            Set<String> orgs = permissionToOrgsMap.get(permissionName);
            if (orgs == null) {
                orgs = new HashSet<>();
                permissionToOrgsMap.put(permissionName, orgs);
            }
            orgs.add(orgQualifier);
        }
    }

    /**
     * Creates an indexed key for the permission + org qualifier.
     * @param permissionName the permission name
     * @param orgQualifier the org qualifier
     */
    protected String createQualifiedPermissionKey(PermissionType permissionName, String orgQualifier) {
        return permissionName.name() + "||" + orgQualifier; //$NON-NLS-1$
    }
    
}
