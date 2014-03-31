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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.overlord.apiman.dt.api.beans.idm.PermissionBean;

/**
 * A class that optimizes the user permissions for querying.
 *
 * @author eric.wittmann@redhat.com
 */
public class IndexedPermissions implements Serializable {
    
    private static final long serialVersionUID = -474966481686691421L;
    
    private Set<String> qualifiedPermissions = new HashSet<String>();
    private Map<String, Set<String>> permissionToOrgsMap = new HashMap<String, Set<String>>();

    /**
     * Constructor.
     * @param permissions
     */
    public IndexedPermissions(Set<PermissionBean> permissions) {
        index(permissions);
    }

    /**
     * Returns true if the qualified permission exists.
     * @param permissionName
     * @param orgQualifier
     */
    public boolean hasQualifiedPermission(String permissionName, String orgQualifier) {
        String key = createQualifiedPermissionKey(permissionName, orgQualifier);
        return qualifiedPermissions.contains(key);
    }
    
    /**
     * Given a permission name, returns all organization qualifiers.
     * @param permissionName
     */
    @SuppressWarnings("unchecked")
    public Set<String> getOrgQualifiers(String permissionName) {
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
            String permissionName = permissionBean.getName();
            String orgQualifier = permissionBean.getOrganizationId();
            String qualifiedPermission = createQualifiedPermissionKey(permissionName, orgQualifier);
            qualifiedPermissions.add(qualifiedPermission);
            Set<String> orgs = permissionToOrgsMap.get(permissionName);
            if (orgs == null) {
                orgs = new HashSet<String>();
                permissionToOrgsMap.put(permissionName, orgs);
            }
            orgs.add(orgQualifier);
        }
    }

    /**
     * Creates an indexed key for the permission + org qualifier.
     * @param permissionName
     * @param orgQualifier
     */
    protected String createQualifiedPermissionKey(String permissionName, String orgQualifier) {
        return permissionName + "||" + orgQualifier; //$NON-NLS-1$
    }
    
}
