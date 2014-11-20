/*
 * Copyright 2013 JBoss Inc
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

package io.apiman.common.auth;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of a principal, but also includes roles.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthPrincipal implements Principal {
    
    private String username;
    private Set<String> roles = new HashSet<String>();
    
    /**
     * Constructor.
     */
    public AuthPrincipal(String username) {
        this.username = username;
    }

    /**
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return username;
    }
    
    /**
     * Adds a role.
     * @param role
     */
    public void addRole(String role) {
        roles.add(role);
    }
    
    /**
     * Adds multiple roles.
     * @param roles
     */
    public void addRoles(Set<String> roles) {
        this.roles.addAll(roles);
    }
    
    /**
     * @return the roles
     */
    public Set<String> getRoles() {
        return roles;
    }
}
