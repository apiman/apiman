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
package io.apiman.gateway.engine.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Models an API contract published to the API Management runtime.
 *
 * @author eric.wittmann@redhat.com
 */
public class Contract implements Serializable {

    private static final long serialVersionUID = 8344360785926823601L;

    private String apiOrgId;
    private String apiId;
    private String apiVersion;
    private String plan;
    private List<Policy> policies = new ArrayList<>();

    /**
     * Constructor.
     */
    public Contract() {
    }

    /**
     * @return the apiOrgId
     */
    public String getApiOrgId() {
        return apiOrgId;
    }

    /**
     * @param apiOrgId the apiOrgId to set
     */
    public void setApiOrgId(String apiOrgId) {
        this.apiOrgId = apiOrgId;
    }

    /**
     * @return the apiId
     */
    public String getApiId() {
        return apiId;
    }

    /**
     * @param apiId the apiId to set
     */
    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * @return the apiVersion
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * @param apiVersion the apiVersion to set
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return the policies
     */
    public List<Policy> getPolicies() {
        return policies;
    }

    /**
     * @param policies the policies to set
     */
    public void setPolicies(List<Policy> policies) {
        this.policies = policies;
    }

    /**
     * @return the plan
     */
    public String getPlan() {
        return plan;
    }

    /**
     * @param plan the plan to set
     */
    public void setPlan(String plan) {
        this.plan = plan;
    }

    /**
     * Checks the API unique identifier against what this contract expects (org id, id, version).
     * Returns true if they match.
     * @param request
     * @return true if the given request matches this contract
     */
    public boolean matches(ApiRequest request) {
        String apiOrgId = request.getApiOrgId();
        String apiId = request.getApiId();
        String apiVersion = request.getApiVersion();
        return matches(apiOrgId, apiId, apiVersion);
    }

    /**
     * Checks the API unique identifier against what this contract expects (org id, id, version).
     * Returns true if they match.
     * @param apiOrgId
     * @param apiId
     * @param apiVersion
     * @return true if the given request matches this contract
     */
    public boolean matches(String apiOrgId, String apiId, String apiVersion) {
        return this.apiOrgId.equals(apiOrgId) && this.apiId.equals(apiId) && this.apiVersion.equals(apiVersion);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        final int maxLen = 10;
        return "Contract [apiOrgId=" + apiOrgId + ", apiId=" + apiId + ", apiVersion=" + apiVersion + ", plan=" + plan + ", policies="
                + (policies != null ? policies.subList(0, Math.min(policies.size(), maxLen)) : null) + "]";
    }

}
