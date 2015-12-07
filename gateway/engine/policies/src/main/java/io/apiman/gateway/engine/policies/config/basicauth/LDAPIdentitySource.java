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
package io.apiman.gateway.engine.policies.config.basicauth;


/**
 * Information about the LDAP server to use to authenticate an inbound
 * user.  The DN pattern can be (for example) of the following form:
 *
 *   cn=${username},dc=${X-Authentication-Domain},dc=org
 *
 * Supported properties for interpolation/replacement in the pattern
 * include the BASIC authentication "username" and any header found
 * in the inbound API request.
 *
 * @author eric.wittmann@redhat.com
 */
public class LDAPIdentitySource {

    private String url;
    private String dnPattern;
    private LDAPBindAsType bindAs;
    private LDAPCredentials credentials;
    private LDAPUserSearch userSearch;
    private boolean extractRoles;
    private String membershipAttribute;
    private String rolenameAttribute;

    /**
     * Constructor.
     */
    public LDAPIdentitySource() {
    }

    /**
     * @return the dnPattern
     */
    public String getDnPattern() {
        return dnPattern;
    }

    /**
     * @param dnPattern the dnPattern to set
     */
    public void setDnPattern(String dnPattern) {
        this.dnPattern = dnPattern;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the bindAs
     */
    public LDAPBindAsType getBindAs() {
        return bindAs;
    }

    /**
     * @param bindAs the bindAs to set
     */
    public void setBindAs(LDAPBindAsType bindAs) {
        this.bindAs = bindAs;
    }

    /**
     * @return the credentials
     */
    public LDAPCredentials getCredentials() {
        return credentials;
    }

    /**
     * @param credentials the credentials to set
     */
    public void setCredentials(LDAPCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * @return the userSearch
     */
    public LDAPUserSearch getUserSearch() {
        return userSearch;
    }

    /**
     * @param userSearch the userSearch to set
     */
    public void setUserSearch(LDAPUserSearch userSearch) {
        this.userSearch = userSearch;
    }

    /**
     * @return the extractRoles
     */
    public boolean isExtractRoles() {
        return extractRoles;
    }

    /**
     * @param extractRoles the extractRoles to set
     */
    public void setExtractRoles(boolean extractRoles) {
        this.extractRoles = extractRoles;
    }

    /**
     * @return the membershipAttribute
     */
    public String getMembershipAttribute() {
        return membershipAttribute;
    }

    /**
     * @param membershipAttribute the membershipAttribute to set
     */
    public void setMembershipAttribute(String membershipAttribute) {
        this.membershipAttribute = membershipAttribute;
    }

    /**
     * @return the rolenameAttribute
     */
    public String getRolenameAttribute() {
        return rolenameAttribute;
    }

    /**
     * @param rolenameAttribute the rolenameAttribute to set
     */
    public void setRolenameAttribute(String rolenameAttribute) {
        this.rolenameAttribute = rolenameAttribute;
    }

}
