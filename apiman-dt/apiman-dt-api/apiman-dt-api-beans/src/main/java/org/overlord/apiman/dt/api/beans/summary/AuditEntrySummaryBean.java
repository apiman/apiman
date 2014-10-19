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
package org.overlord.apiman.dt.api.beans.summary;

import java.io.Serializable;
import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.overlord.apiman.dt.api.beans.audit.AuditEntityType;
import org.overlord.apiman.dt.api.beans.audit.AuditEntryType;

/**
 * A summary version of the audit entry.  This only contains the critical 
 * information required to display a list in a UI.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class AuditEntrySummaryBean implements Serializable {

    private static final long serialVersionUID = 3437756153418370536L;
    
    private Long id;
    private String who;
    private String organizationId;
    private String organizationName;
    private AuditEntityType entityType;
    private String entityId;
    private String entityName;
    private String entityVersion;
    private Date when;
    private AuditEntryType type;

    /**
     * Constructor.
     */
    public AuditEntrySummaryBean() {
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
     * @return the who
     */
    public String getWho() {
        return who;
    }

    /**
     * @param who the who to set
     */
    public void setWho(String who) {
        this.who = who;
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
     * @return the organizationName
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * @param organizationName the organizationName to set
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * @return the entityType
     */
    public AuditEntityType getEntityType() {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    public void setEntityType(AuditEntityType entityType) {
        this.entityType = entityType;
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
     * @return the entityName
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName the entityName to set
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
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
     * @return the when
     */
    public Date getWhen() {
        return when;
    }

    /**
     * @param when the when to set
     */
    public void setWhen(Date when) {
        this.when = when;
    }

    /**
     * @return the type
     */
    public AuditEntryType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(AuditEntryType type) {
        this.type = type;
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
        AuditEntrySummaryBean other = (AuditEntrySummaryBean) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}
