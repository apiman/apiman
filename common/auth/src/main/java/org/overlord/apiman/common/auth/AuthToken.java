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
package org.overlord.apiman.common.auth;

import java.util.Date;
import java.util.Set;

/**
 * A simple authentication token.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthToken {
    
    private Date issuedOn;
    private Date expiresOn;
    private String principal;
    private Set<String> roles;
    private String signature;
    
    /**
     * Constructor.
     */
    public AuthToken() {
    }

    /**
     * @return the issuedOn
     */
    public Date getIssuedOn() {
        return issuedOn;
    }

    /**
     * @param issuedOn the issuedOn to set
     */
    public void setIssuedOn(Date issuedOn) {
        this.issuedOn = issuedOn;
    }

    /**
     * @return the expiresOn
     */
    public Date getExpiresOn() {
        return expiresOn;
    }

    /**
     * @param expiresOn the expiresOn to set
     */
    public void setExpiresOn(Date expiresOn) {
        this.expiresOn = expiresOn;
    }

    /**
     * @return the principal
     */
    public String getPrincipal() {
        return principal;
    }

    /**
     * @param principal the principal to set
     */
    public void setPrincipal(String principal) {
        this.principal = principal;
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

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

}
