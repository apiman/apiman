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
package io.apiman.manager.api.beans.summary;

import java.io.Serializable;
import java.util.Date;

/**
 * Summary of policy info.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicySummaryBean implements Serializable {

    private static final long serialVersionUID = 1208106756423327108L;

    private String policyDefinitionId;
    private Long id;
    private String name;
    private String description;
    private String icon;
    private String createdBy;
    private Date createdOn;

    /**
     * Constructor.
     */
    public PolicySummaryBean() {
    }

    /**
     * @return the policyDefinitionId
     */
    public String getPolicyDefinitionId() {
        return policyDefinitionId;
    }

    /**
     * @param policyDefinitionId the policyDefinitionId to set
     */
    public PolicySummaryBean setPolicyDefinitionId(String policyDefinitionId) {
        this.policyDefinitionId = policyDefinitionId;
        return this;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public PolicySummaryBean setId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public PolicySummaryBean setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public PolicySummaryBean setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public PolicySummaryBean setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public PolicySummaryBean setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public PolicySummaryBean setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "PolicySummaryBean [policyDefinitionId=" + policyDefinitionId + ", id=" + id + ", name="
                + name + ", description=" + description + ", icon=" + icon + ", createdBy=" + createdBy
                + ", createdOn=" + createdOn + "]";
    }

}
