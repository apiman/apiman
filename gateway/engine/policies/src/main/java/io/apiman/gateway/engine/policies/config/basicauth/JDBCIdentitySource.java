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
public class JDBCIdentitySource {

    private String datasourcePath;
    private String query;
    private PasswordHashAlgorithmType hashAlgorithm;
    
    /**
     * Constructor.
     */
    public JDBCIdentitySource() {
    }

    /**
     * @return the datasource
     */
    public String getDatasourcePath() {
        return datasourcePath;
    }

    /**
     * @param datasource the datasource to set
     */
    public void setDatasourcePath(String datasource) {
        this.datasourcePath = datasource;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the hashAlgorithm
     */
    public PasswordHashAlgorithmType getHashAlgorithm() {
        return hashAlgorithm;
    }

    /**
     * @param hashAlgorithm the hashAlgorithm to set
     */
    public void setHashAlgorithm(PasswordHashAlgorithmType hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

}
