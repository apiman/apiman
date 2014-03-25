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
package org.overlord.apiman.rt.engine.beans;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Models an Application registered with the API Management runtime.
 *
 * @author eric.wittmann@redhat.com
 */
public class Application implements Serializable {
    
    private static final long serialVersionUID = 4515000941548789924L;
    
    private String organizationId;
    private String applicationId;
    private String version;
    private Set<Contract> contracts = new HashSet<Contract>();
    
    /**
     * Constructor.
     */
    public Application() {
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
     * @return the applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * @param applicationId the applicationId to set
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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
     * @return a unique application key useful for indexing
     */
    public String getApplicationKey() {
        return this.organizationId + "|" + this.getApplicationId() + "|" + this.version;
    }

    /**
     * @return the contracts
     */
    public Set<Contract> getContracts() {
        return contracts;
    }
    
    /**
     * @param contract the contract to add
     */
    public void addContract(Contract contract) {
        contracts.add(contract);
    }

    /**
     * @param contracts the contracts to set
     */
    public void setContracts(Set<Contract> contracts) {
        this.contracts = contracts;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((organizationId == null) ? 0 : organizationId.hashCode());
        result = prime * result + ((getApplicationId() == null) ? 0 : getApplicationId().hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        Application other = (Application) obj;
        if (organizationId == null) {
            if (other.organizationId != null)
                return false;
        } else if (!organizationId.equals(other.organizationId))
            return false;
        if (getApplicationId() == null) {
            if (other.getApplicationId() != null)
                return false;
        } else if (!getApplicationId().equals(other.getApplicationId()))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

}
