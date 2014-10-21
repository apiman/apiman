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
package org.overlord.apiman.dt.api.beans.audit.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * The data saved along with the audit entry when membership is modified
 * for a user+organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class MembershipData implements Serializable {
    
    private static final long serialVersionUID = 3424852746654173415L;
    
    private Set<String> roles = new HashSet<String>();

    /**
     * Constructor.
     */
    public MembershipData() {
    }
    
    /**
     * @param role
     */
    public void addRole(String role) {
        getRoles().add(role);
    }

    /**
     * @return the roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

}
