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

/**
 * A summary bean for a contract.  Includes information useful for displaying 
 * the contract in a list in a UI.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ContractSummaryBean implements Serializable {
    
    private static final long serialVersionUID = 1412354024017539782L;
    
    private Long contractId;
    private String key;
    private String appOrganizationId;
    private String appOrganizationName;
    private String appId;
    private String appName;
    private String appVersion;
    private String serviceOrganizationId;
    private String serviceOrganizationName;
    private String serviceId;
    private String serviceName;
    private String serviceVersion;
    private String serviceDescription;
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
     * @return the appOrganizationId
     */
    public String getAppOrganizationId() {
        return appOrganizationId;
    }

    /**
     * @param appOrganizationId the appOrganizationId to set
     */
    public void setAppOrganizationId(String appOrganizationId) {
        this.appOrganizationId = appOrganizationId;
    }

    /**
     * @return the appOrganizationName
     */
    public String getAppOrganizationName() {
        return appOrganizationName;
    }

    /**
     * @param appOrganizationName the appOrganizationName to set
     */
    public void setAppOrganizationName(String appOrganizationName) {
        this.appOrganizationName = appOrganizationName;
    }

    /**
     * @return the appId
     */
    public String getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * @return the appVersion
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * @param appVersion the appVersion to set
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * @return the serviceOrganizationId
     */
    public String getServiceOrganizationId() {
        return serviceOrganizationId;
    }

    /**
     * @param serviceOrganizationId the serviceOrganizationId to set
     */
    public void setServiceOrganizationId(String serviceOrganizationId) {
        this.serviceOrganizationId = serviceOrganizationId;
    }

    /**
     * @return the serviceOrganizationName
     */
    public String getServiceOrganizationName() {
        return serviceOrganizationName;
    }

    /**
     * @param serviceOrganizationName the serviceOrganizationName to set
     */
    public void setServiceOrganizationName(String serviceOrganizationName) {
        this.serviceOrganizationName = serviceOrganizationName;
    }

    /**
     * @return the serviceVersion
     */
    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * @param serviceVersion the serviceVersion to set
     */
    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    /**
     * @return the serviceDescription
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /**
     * @param serviceDescription the serviceDescription to set
     */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
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
     * @return the appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @param appName the appName to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * @return the serviceId
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @param serviceId the serviceId to set
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName the serviceName to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
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

}
