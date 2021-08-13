/*
 * Copyright 2020 Scheer PAS Schweiz AG
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

package io.apiman.manager.api.beans.developers;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Models the mapping data for a developer
 */
@Entity
@Table(name = "developermappings")
public class DeveloperMappingBean implements Serializable {

    private static final long serialVersionUID = -5334196591430185705L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(nullable = false)
    private String id;
    @Column(name = "client_id", nullable = false)
    private String clientId;
    @Column(name = "organization_id")
    private String organizationId;

    /**
     * Constructor
     */
    public DeveloperMappingBean() {
    }

    public String getId() {
        return id;
    }

    public DeveloperMappingBean setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the client id
     * @return the client id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set the client id
     * @param clientId the client id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Get the organization id
     * @return the organization id
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set the organization id
     * @param organizationId the organization id
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeveloperMappingBean [clientId=" + clientId + ",organizationId=" + organizationId + "]";
    }

}

