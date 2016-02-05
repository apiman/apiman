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
package io.apiman.manager.api.es.beans;

import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;

import java.util.ArrayList;
import java.util.List;

/**
 * Bundles up a bunch of policies into a single bean.  A policies bean
 * represents all of the policies configured for a particular entity (plan,
 * API, app).
 *
 * @author eric.wittmann@redhat.com
 */
public class PoliciesBean {

    private PolicyType type;
    private String organizationId;
    private String entityId;
    private String entityVersion;
    private List<PolicyBean> policies = new ArrayList<>();

    /**
     * Constructor.
     */
    public PoliciesBean() {
    }

    /**
     * Create a new Policies object from the given policy bean instance.
     * @param policy the policy
     * @return the policies
     */
    public static final PoliciesBean from(PolicyBean policy) {
        PoliciesBean rval = new PoliciesBean();
        rval.setType(policy.getType());
        rval.setOrganizationId(policy.getOrganizationId());
        rval.setEntityId(policy.getEntityId());
        rval.setEntityVersion(policy.getEntityVersion());
        rval.getPolicies().add(policy);
        return rval;
    }

    /**
     * Creates a new, empty {@link PoliciesBean} from some basic information.
     * @param type the policy type
     * @param organizationId the organization id
     * @param entityId the entity id
     * @param entityVersion the entity version
     * @return the policies
     */
    public static final PoliciesBean from(PolicyType type, String organizationId, String entityId, String entityVersion) {
        PoliciesBean rval = new PoliciesBean();
        rval.setType(type);
        rval.setOrganizationId(organizationId);
        rval.setEntityId(entityId);
        rval.setEntityVersion(entityVersion);
        return rval;
    }

    /**
     * @return the type
     */
    public PolicyType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(PolicyType type) {
        this.type = type;
    }

    /**
     * @return the organizationId
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * @return the entityId
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * @param entityId the entityId to set
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * @return the entityVersion
     */
    public String getEntityVersion() {
        return entityVersion;
    }

    /**
     * @param entityVersion the entityVersion to set
     */
    public void setEntityVersion(String entityVersion) {
        this.entityVersion = entityVersion;
    }

    /**
     * @return the policies
     */
    public List<PolicyBean> getPolicies() {
        return policies;
    }

    /**
     * @param policies the policies to set
     */
    public void setPolicies(List<PolicyBean> policies) {
        this.policies = policies;
    }
}
