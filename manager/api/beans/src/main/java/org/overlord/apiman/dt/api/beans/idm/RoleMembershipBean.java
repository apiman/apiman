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
package org.overlord.apiman.dt.api.beans.idm;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Index;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A single, qualified, role granted to the user.  Roles in the system
 * might include things like "Service Owner", "Application Developer", etc.
 * A role is qualified by an Organization ID.  The purpose of a role is
 * to grant permissions to a user.  A role might grant CREATE-APPLICATION 
 * and VIEW-SERVICE permissions for a particular Organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@Entity
@Table(name = "memberships",
       uniqueConstraints={@UniqueConstraint(columnNames={"user_id","role_id","org_id"})})
public class RoleMembershipBean implements Serializable {
    
    private static final long serialVersionUID = 7798709783947356888L;

    public static final RoleMembershipBean create(String userId, String roleId, String organizationId) {
        RoleMembershipBean bean = new RoleMembershipBean();
        bean.setUserId(userId);
        bean.setRoleId(roleId);
        bean.setOrganizationId(organizationId);
        return bean;
    }

    @Id @GeneratedValue
    private Long id;
    @Column(name="user_id")
    @Index(name="userIdIdx")
    private String userId;
    @Column(name="role_id")
    private String roleId;
    @Column(name="org_id")
    @Index(name="orgIdIdx")
    private String organizationId;
    private Date createdOn;

    /**
     * Constructor.
     */
    public RoleMembershipBean() {
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the roleId
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * @param roleId the roleId to set
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
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
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        RoleMembershipBean other = (RoleMembershipBean) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
