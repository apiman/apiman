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
package io.apiman.manager.api.beans.plans;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Models a single version of a plan "version".  Every plan in
 * APIMan has basic meta-data stored in {@link PlanBean}.  All
 * other specifics of the plan, such as endpoint information
 * and configured policies are associated with a particular version
 * of that Plan.  This class represents that version.
 *
 * @author eric.wittmann@redhat.com
 */
@Entity
@Table(name = "plan_versions",
       uniqueConstraints = { @UniqueConstraint(columnNames = { "plan_id", "plan_org_id", "version" }) })
public class PlanVersionBean implements Serializable, Cloneable {

    private static final long serialVersionUID = -2218697175049442690L;

    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="plan_id", referencedColumnName="id"),
        @JoinColumn(name="plan_org_id", referencedColumnName="organization_id")
    })
    private PlanBean plan;
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private PlanStatus status;
    @Column(updatable=false, nullable=false)
    private String version;
    @Column(name = "created_by", updatable=false, nullable=false)
    private String createdBy;
    @Column(name = "created_on", updatable=false, nullable=false)
    private Date createdOn;
    @Column(name = "modified_by", updatable=true, nullable=false)
    private String modifiedBy;
    @Column(name = "modified_on", updatable=true, nullable=false)
    private Date modifiedOn;
    @Column(name = "locked_on")
    private Date lockedOn;

    /**
     * Constructor.
     */
    public PlanVersionBean() {
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
     * @return the plan
     */
    public PlanBean getPlan() {
        return plan;
    }

    /**
     * @param plan the plan to set
     */
    public void setPlan(PlanBean plan) {
        this.plan = plan;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
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
     * @return the status
     */
    public PlanStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    /**
     * @return the lockedOn
     */
    public Date getLockedOn() {
        return lockedOn;
    }

    /**
     * @param lockedOn the lockedOn to set
     */
    public void setLockedOn(Date lockedOn) {
        this.lockedOn = lockedOn;
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlanVersionBean other = (PlanVersionBean) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "PlanVersionBean [id=" + id + ", plan=" + plan + ", status=" + status + ", version=" + version
                + ", createdBy=" + createdBy + ", createdOn=" + createdOn + ", modifiedBy=" + modifiedBy
                + ", modifiedOn=" + modifiedOn + ", lockedOn=" + lockedOn + "]";
    }
    
    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
