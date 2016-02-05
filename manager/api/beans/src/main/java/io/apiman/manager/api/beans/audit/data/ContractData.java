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
package io.apiman.manager.api.beans.audit.data;

import io.apiman.manager.api.beans.contracts.ContractBean;

import java.io.Serializable;

/**
 * The data saved along with the audit entry when a contract is created.
 *
 * @author eric.wittmann@redhat.com
 */
public class ContractData implements Serializable {

    private static final long serialVersionUID = -937575521565548994L;

    private String clientOrgId;
    private String clientId;
    private String clientVersion;
    private String apiOrgId;
    private String apiId;
    private String apiVersion;
    private String planId;
    private String planVersion;

    /**
     * Constructor.
     */
    public ContractData() {
    }

    /**
     * Constructor.
     * @param bean the contract
     */
    public ContractData(ContractBean bean) {
        setClientOrgId(bean.getClient().getClient().getOrganization().getId());
        setClientId(bean.getClient().getClient().getId());
        setClientVersion(bean.getClient().getVersion());
        setApiOrgId(bean.getApi().getApi().getOrganization().getId());
        setApiId(bean.getApi().getApi().getId());
        setApiVersion(bean.getApi().getVersion());
        setPlanId(bean.getPlan().getPlan().getId());
        setPlanVersion(bean.getPlan().getVersion());
    }

    /**
     * @return the clientOrgId
     */
    public String getClientOrgId() {
        return clientOrgId;
    }

    /**
     * @param clientOrgId the clientOrgId to set
     */
    public void setClientOrgId(String clientOrgId) {
        this.clientOrgId = clientOrgId;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "ContractData [clientOrgId=" + clientOrgId + ", clientId=" + clientId + ", clientVersion=" + clientVersion
                + ", apiOrgId=" + apiOrgId + ", apiId=" + apiId + ", apiVersion="
                + apiVersion + ", planId=" + planId + ", planVersion=" + planVersion + "]";
    }

}
