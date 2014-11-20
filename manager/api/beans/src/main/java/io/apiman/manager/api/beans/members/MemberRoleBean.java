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
package io.apiman.manager.api.beans.members;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models a user's membership in a role for a given organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class MemberRoleBean implements Serializable {

    private static final long serialVersionUID = 7645338035552144540L;
    
    private String roleId;
    private String roleName;
    
    /**
     * Constructor.
     */
    public MemberRoleBean() {
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
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @param roleName the roleName to set
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
