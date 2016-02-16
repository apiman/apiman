/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.gateway.engine.policies.config;

/**
 * A single authorization rule consisting of a verb, path pattern, and role name.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuthorizationRule {

    private String verb;
    private String pathPattern;
    private String role;

    /**
     * Constructor.
     */
    public AuthorizationRule() {
    }

    /**
     * @return the verb
     */
    public String getVerb() {
        return verb;
    }

    /**
     * @param verb the verb to set
     */
    public void setVerb(String verb) {
        this.verb = verb;
    }

    /**
     * @return the pathPattern
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * @param pathPattern the pathPattern to set
     */
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        AuthorizationRule rule2 = (AuthorizationRule) obj;
        return this.getVerb().equals(rule2.getVerb()) && this.getPathPattern().equals(rule2.getPathPattern())
                && this.getRole().equals(rule2.getRole());
    }

    @Override
    public int hashCode() {
        int result = this.verb != null ? this.verb.hashCode() : 0;
        result = 31 * result + (this.pathPattern != null ? this.pathPattern.hashCode() : 0);
        result = 31 * result + (this.role != null ? this.role.hashCode() : 0);
        return result;
    }
}
