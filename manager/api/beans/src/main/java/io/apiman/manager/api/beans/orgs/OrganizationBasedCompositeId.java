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
package io.apiman.manager.api.beans.orgs;

import java.io.Serializable;

/**
 * Composite key for entities that are owned by an organization.
 *
 * @author eric.wittmann@redhat.com
 */
public class OrganizationBasedCompositeId implements Serializable {
    
    private static final long serialVersionUID = 7313295981342740517L;
    
    private OrganizationBean organization;
    private String id;
    
    /**
     * Constructor.
     */
    public OrganizationBasedCompositeId() {
    }

    /**
     * Constructor.
     * @param organization the organization
     * @param id the id
     */
    public OrganizationBasedCompositeId(OrganizationBean organization, String id) {
        this.setOrganization(organization);
        this.setId(id);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the organization
     */
    public OrganizationBean getOrganization() {
        return organization;
    }

    /**
     * @param organization the organization to set
     */
    public void setOrganization(OrganizationBean organization) {
        this.organization = organization;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((organization == null) ? 0 : organization.getId().hashCode());
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
        OrganizationBasedCompositeId other = (OrganizationBasedCompositeId) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (organization == null) {
            if (other.organization != null)
                return false;
        } else if (!organization.getId().equals(other.organization.getId()))
            return false;
        return true;
    }
    
    

}
