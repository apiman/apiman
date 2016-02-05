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
package io.apiman.manager.api.beans.summary;

import java.io.Serializable;
import java.util.Date;

/**
 * A summary bean for a contract.  Includes information useful for displaying
 * the contract in a list in a UI.
 *
 * @author eric.wittmann@redhat.com
 */
public class ContractSummaryBean implements Serializable {

    private static final long serialVersionUID = 1412354024017539782L;

    private Long contractId;
    private String apikey;
    private String clientOrganizationId;
    private String clientOrganizationName;
    private String clientId;
    private String clientName;
    private String clientVersion;
    private String apiOrganizationId;
    private String apiOrganizationName;
    private String apiId;
    private String apiName;
    private String apiVersion;
    private String apiDescription;
    private String planName;
    private String planId;
    private String planVersion;
    private Date createdOn;

    /**
     * Constructor.
     */
    public ContractSummaryBean() {
    }

    /**
     * @return the clientOrganizationId
     */
    public String getClientOrganizationId() {
        return clientOrganizationId;
    }

    /**
     * @param clientOrganizationId the clientOrganizationId to set
     */
    public void setClientOrganizationId(String clientOrganizationId) {
        this.clientOrganizationId = clientOrganizationId;
    }

    /**
     * @return the clientOrganizationName
     */
    public String getClientOrganizationName() {
        return clientOrganizationName;
    }

    /**
     * @param clientOrganizationName the clientOrganizationName to set
     */
    public void setClientOrganizationName(String clientOrganizationName) {
        this.clientOrganizationName = clientOrganizationName;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the clientVersion
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     * @param clientVersion the clientVersion to set
     */
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    /**
     * @return the apiOrganizationId
     */
    public String getApiOrganizationId() {
        return apiOrganizationId;
    }

    /**
     * @param apiOrganizationId the apiOrganizationId to set
     */
    public void setApiOrganizationId(String apiOrganizationId) {
        this.apiOrganizationId = apiOrganizationId;
    }

    /**
     * @return the apiOrganizationName
     */
    public String getApiOrganizationName() {
        return apiOrganizationName;
    }

    /**
     * @param apiOrganizationName the apiOrganizationName to set
     */
    public void setApiOrganizationName(String apiOrganizationName) {
        this.apiOrganizationName = apiOrganizationName;
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
     * @return the apiDescription
     */
    public String getApiDescription() {
        return apiDescription;
    }

    /**
     * @param apiDescription the apiDescription to set
     */
    public void setApiDescription(String apiDescription) {
        this.apiDescription = apiDescription;
    }

    /**
     * @return the planName
     */
    public String getPlanName() {
        return planName;
    }

    /**
     * @param planName the planName to set
     */
    public void setPlanName(String planName) {
        this.planName = planName;
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
     * @return the planVersion
     */
    public String getPlanVersion() {
        return planVersion;
    }

    /**
     * @param planVersion the planVersion to set
     */
    public void setPlanVersion(String planVersion) {
        this.planVersion = planVersion;
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
     * @return the contractId
     */
    public Long getContractId() {
        return contractId;
    }

    /**
     * @param contractId the contractId to set
     */
    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    /**
     * @return the clientName
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param clientName the clientName to set
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
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
     * @return the apiName
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * @param apiName the apiName to set
     */
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    /**
     * @return the apikey
     */
    public String getApikey() {
        return apikey;
    }

    /**
     * @param apikey the apikey to set
     */
    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contractId == null) ? 0 : contractId.hashCode());
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
        ContractSummaryBean other = (ContractSummaryBean) obj;
        if (contractId == null) {
            if (other.contractId != null)
                return false;
        } else if (!contractId.equals(other.contractId))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "ContractSummaryBean [contractId=" + contractId + ", apikey=" + apikey
                + ", clientOrganizationId=" + clientOrganizationId + ", clientOrganizationName=" + clientOrganizationName
                + ", clientId=" + clientId + ", clientName=" + clientName + ", clientVersion=" + clientVersion
                + ", apiOrganizationId=" + apiOrganizationId + ", apiOrganizationName="
                + apiOrganizationName + ", apiId=" + apiId + ", apiName=" + apiName
                + ", apiVersion=" + apiVersion + ", apiDescription=" + apiDescription
                + ", planName=" + planName + ", planId=" + planId + ", planVersion=" + planVersion
                + ", createdOn=" + createdOn + "]";
    }

}
