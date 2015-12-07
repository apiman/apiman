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
public class JDBCIdentitySource {

    private JDBCType type = JDBCType.datasource;

    /*
     * If using a datasource connection.
     */
    private String datasourcePath;

    /*
     * If using a URL connection
     */
    private String jdbcUrl;
    private String username;
    private String password;

    private String query;
    private PasswordHashAlgorithmType hashAlgorithm;
    private boolean extractRoles;
    private String roleQuery;

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
     * @return the roleQuery
     */
    public String getRoleQuery() {
        return roleQuery;
    }

    /**
     * @param roleQuery the roleQuery to set
     */
    public void setRoleQuery(String roleQuery) {
        this.roleQuery = roleQuery;
    }

    /**
     * @return the type
     */
    public JDBCType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(JDBCType type) {
        this.type = type;
    }

    /**
     * @return the jdbcUrl
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * @param jdbcUrl the jdbcUrl to set
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
