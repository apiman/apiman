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
import io.apiman.manager.api.beans.clients.ClientBean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Models a developer
 * The Keycloak Username is used as ID
 */
@Entity
@Table(name = "developers")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeveloperBean implements Serializable {

    private static final long serialVersionUID = 7127400624541487145L;

    @Id
    @Column(nullable = false)
    private String id;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "developer")
    private Set<DeveloperMappingBean> clients = new LinkedHashSet<>();

    /**
     * Constructor
     */
    public DeveloperBean() {
    }

    /**
     * Get a list of clients
     *
     * @return The list of clients
     */
    public Set<DeveloperMappingBean> getClients() {
        return clients;
    }

    /**
     * Set a list of clients
     *
     * @param clients The list of clients
     */
    public void setClients(Set<DeveloperMappingBean> clients) {
        this.clients = clients;
    }

    /**
     * Get the id
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id
     *
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DeveloperBean [id=" + id + ",clients=" + clients + "]";
    }
}
