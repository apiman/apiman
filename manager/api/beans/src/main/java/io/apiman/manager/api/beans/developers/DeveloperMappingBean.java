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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Models the mapping data for a developer
 */
@Entity
@Table(name = "developer_clients")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperMappingBean implements Serializable {

    private static final long serialVersionUID = -5334196591430185705L;

    @Id
    private Long id;
    private String clientId;
    private String organizationId;


    /**
     * Constructor
     */
    public DeveloperMappingBean() {
    }

    /**
     * Get the id
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the id
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
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
     * Get the organiztaion id
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

