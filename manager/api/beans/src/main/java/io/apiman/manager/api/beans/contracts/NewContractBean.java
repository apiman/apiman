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
package io.apiman.manager.api.beans.contracts;

import java.io.Serializable;

/**
 * The bean used to create a new contract.
 *
 * @author eric.wittmann@redhat.com
 */
public class NewContractBean implements Serializable {

    private static final long serialVersionUID = -2326957716478467884L;

    private String apiOrgId;
    private String apiId;
    private String apiVersion;

    private String planId;
    private String apiKey;

    /**
     * Constructor.
     */
    public NewContractBean() {
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @return the apiOrgId
     */
    public String getApiOrgId() {
        return apiOrgId;
    }

    /**
     * @param apiOrgId the apiOrgId to set
     */
    public void setApiOrgId(String apiOrgId) {
        this.apiOrgId = apiOrgId;
    }

    /**
     * @return the apiId
     */
    public String getApiId() {
        return apiId;
    }

    /**
     * @param apiId the apiId to set
     */
    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    /**
     * @return the apiVersion
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * @param apiVersion the apiVersion to set
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return the planId
     */
    public String getPlanId() {
        return planId;
    }

    /**
     * @param planId the planId to set
     */
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    /**
     * @return the contractKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (planId == null ? 0 : planId.hashCode());
        result = prime * result + (apiId == null ? 0 : apiId.hashCode());
        result = prime * result + (apiOrgId == null ? 0 : apiOrgId.hashCode());
        result = prime * result + (apiVersion == null ? 0 : apiVersion.hashCode());
        result = prime * result + (apiKey == null ? 0 : apiKey.hashCode());
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
        NewContractBean other = (NewContractBean) obj;
        if (planId == null) {
            if (other.planId != null)
                return false;
        } else if (!planId.equals(other.planId))
            return false;
        if (apiId == null) {
            if (other.apiId != null)
                return false;
        } else if (!apiId.equals(other.apiId))
            return false;
        if (apiOrgId == null) {
            if (other.apiOrgId != null)
                return false;
        } else if (!apiOrgId.equals(other.apiOrgId))
            return false;
        if (apiVersion == null) {
            if (other.apiVersion != null)
                return false;
        } else if (!apiVersion.equals(other.apiVersion))
            return false;
        if (apiKey == null) {
            if (other.apiKey != null)
                return false;
        } else if (!apiKey.equals(other.apiKey))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "NewContractBean [apiOrgId=" + apiOrgId + ", apiId=" + apiId + ", apiVersion=" + apiVersion
                + ", planId=" + planId + ", apiKey=" + apiKey + "]";
    }

}
