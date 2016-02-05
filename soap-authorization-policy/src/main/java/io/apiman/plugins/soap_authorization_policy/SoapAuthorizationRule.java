/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.plugins.soap_authorization_policy;

/**
 * A single authorization rule consisting of an action, path pattern, and role name.
 *
 * @author eric.wittmann@redhat.com
 * @author rachel.yordan@redhat.com
 */
public class SoapAuthorizationRule {

    private String action;
    private String pathPattern;
    private String role;

    public SoapAuthorizationRule() {
    }

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * @return pathPattern
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * @param pathPattern
     */
    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    /**
     * @return role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        SoapAuthorizationRule rule2 = (SoapAuthorizationRule) obj;
        return this.getAction().equals(rule2.getAction()) && this.getPathPattern().equals(rule2.getPathPattern())
                && this.getRole().equals(rule2.getRole());
    }

}
