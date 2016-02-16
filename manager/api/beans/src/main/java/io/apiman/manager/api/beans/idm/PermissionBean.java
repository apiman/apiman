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
package io.apiman.manager.api.beans.idm;

import java.io.Serializable;

/**
 * Models a single qualified permission assigned to a user.  All permissions
 * are granted to users by membership in one or more roles.  Membership in a
 * role is qualified by organization, allowing a user to have different roles
 * in different organizations.
 *
 * @author eric.wittmann@redhat.com
 */
public class PermissionBean implements Serializable {

    private static final long serialVersionUID = 6005936454144731711L;

    private PermissionType name;
    private String organizationId;

    /**
     * Constructor.
     */
    public PermissionBean() {
    }

    /**
     * @return the name
     */
    public PermissionType getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(PermissionType name) {
        this.name = name;
    }

    /**
     * @return the organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (organizationId == null ? 0 : organizationId.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PermissionBean other = (PermissionBean) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (organizationId == null) {
            if (other.organizationId != null)
                return false;
        } else if (!organizationId.equals(other.organizationId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "PermissionBean [name=" + name + ", organizationId=" + organizationId + "]";
    }

}
