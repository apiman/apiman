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

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Information about the LDAP server to use to authenticate an inbound
 * user.  The DN pattern can be (for example) of the following form:
 * 
 *   cn=${username},dc=${X-Authentication-Domain},dc=org
 * 
 * Supported properties for interpolation/replacement in the pattern 
 * include the BASIC authentication "username" and any header found
 * in the inbound service request.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class LDAPIdentitySource {
    
    private String url;
    private String dnPattern;
    
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

}
