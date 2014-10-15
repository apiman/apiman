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
package org.overlord.apiman.engine.policies.config;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.overlord.apiman.engine.policies.config.basicauth.StaticIdentitySource;


/**
 * Configuration object for the IP blacklist policy.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class BasicAuthenticationConfig {
    
    private String realm;
    private String forwardIdentityHttpHeader;
    
    private StaticIdentitySource staticIdentity;
    
    /**
     * Constructor.
     */
    public BasicAuthenticationConfig() {
    }

    /**
     * @return the staticIdentity
     */
    public StaticIdentitySource getStaticIdentity() {
        return staticIdentity;
    }

    /**
     * @param staticIdentity the staticIdentity to set
     */
    public void setStaticIdentity(StaticIdentitySource staticIdentity) {
        this.staticIdentity = staticIdentity;
    }

    /**
     * @return the realm
     */
    public String getRealm() {
        return realm;
    }

    /**
     * @param realm the realm to set
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * @return the forwardIdentityHttpHeader
     */
    public String getForwardIdentityHttpHeader() {
        return forwardIdentityHttpHeader;
    }

    /**
     * @param forwardIdentityHttpHeader the forwardIdentityHttpHeader to set
     */
    public void setForwardIdentityHttpHeader(String forwardIdentityHttpHeader) {
        this.forwardIdentityHttpHeader = forwardIdentityHttpHeader;
    }

}
