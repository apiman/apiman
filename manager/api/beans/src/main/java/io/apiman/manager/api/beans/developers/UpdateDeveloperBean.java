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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

/**
 * Models the data that are used to create or update a developer
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateDeveloperBean {
    private Set<DeveloperMappingBean> clients;

    /**
     * Constructor
     */
    public UpdateDeveloperBean() {
    }

    /**
     * Get the clients
     *
     * @return list of the clients
     */
    public Set<DeveloperMappingBean> getClients() {
        return clients;
    }

    /**
     * Set the clients
     *
     * @param clients the list of clients
     */
    public void setClients(Set<DeveloperMappingBean> clients) {
        this.clients = clients;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UpdateDeveloperBean [clients=" + clients + "]";
    }
}
