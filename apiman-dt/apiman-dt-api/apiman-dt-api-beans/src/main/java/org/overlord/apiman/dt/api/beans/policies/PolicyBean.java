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
package org.overlord.apiman.dt.api.beans.policies;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * A Policy is the primary unit of work for the runtime engine, which is
 * essentially a chain of policies that are applied to the Request and
 * Response of a service.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@Entity
@Table(name = "policies")
public class PolicyBean implements Serializable {
    
    private static final long serialVersionUID = -8534463608508756791L;

    @Id @GeneratedValue
    private Long id;
    @Column(updatable=false, nullable=false)
    private PolicyType type;
    @Column(updatable=false, nullable=false)
    private String organizationId;
    @Column(updatable=false, nullable=false)
    private String entityId;
    @Column(updatable=false, nullable=false)
    private String entityVersion;
    private String name;
    @Lob
    private String description;
    @Column(updatable=false, nullable=false)
    private String policyImpl;
    @Lob
    @Column(updatable=true, nullable=true)
    private String configuration;
    @Column(updatable=false, nullable=false)
    private String createdBy;
    @Column(updatable=false, nullable=false)
    private Date createdOn;
    @Column(updatable=true, nullable=false)
    private String modifiedBy;
    @Column(updatable=true, nullable=false)
    private Date modifiedOn;

    /**
     * Constructor.
     */
    public PolicyBean() {
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
    public void setId(Long id) {
        this.id = id;
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
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
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
     * @return the policyImpl
     */
    public String getPolicyImpl() {
        return policyImpl;
    }

    /**
     * @param policyImpl the policyImpl to set
     */
    public void setPolicyImpl(String policyImpl) {
        this.policyImpl = policyImpl;
    }

    /**
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    /**
     * @return the modifiedBy
     */
    public String getModifiedBy() {
        return modifiedBy;
    }

    /**
     * @param modifiedBy the modifiedBy to set
     */
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    /**
     * @return the modifiedOn
     */
    public Date getModifiedOn() {
        return modifiedOn;
    }

    /**
     * @param modifiedOn the modifiedOn to set
     */
    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
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
    public void setDescription(String description) {
        this.description = description;
    }

}
