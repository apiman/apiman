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

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * A single entry in the {@link ApiRegistryBean}.
 *
 * @author eric.wittmann@redhat.com
 */
@XmlRootElement(name = "api")
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ApiEntryBean implements Serializable {

    private static final long serialVersionUID = -7578173174922025902L;

    private String apiOrgId;
    private String apiOrgName;
    private String apiId;
    private String apiName;
    private String apiVersion;

    private String planId;
    private String planName;
    private String planVersion;

    private String httpEndpoint;
    private String apiKey;

    private String gatewayId;

    /**
     * Constructor.
     */
    public ApiEntryBean() {
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
     * @return the apiOrgName
     */
    public String getApiOrgName() {
        return apiOrgName;
    }

    /**
     * @param apiOrgName the apiOrgName to set
     */
    public void setApiOrgName(String apiOrgName) {
        this.apiOrgName = apiOrgName;
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
     * @return the httpEndpoint
     */
    public String getHttpEndpoint() {
        return httpEndpoint;
    }

    /**
     * @param httpEndpoint the httpEndpoint to set
     */
    public void setHttpEndpoint(String httpEndpoint) {
        this.httpEndpoint = httpEndpoint;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the gatewayId
     */
    public String getGatewayId() {
        return gatewayId;
    }

    /**
     * @param gatewayId the gatewayId to set
     */
    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "ApiEntryBean [apiOrgId=" + apiOrgId + ", apiOrgName=" + apiOrgName
                + ", apiId=" + apiId + ", apiName=" + apiName + ", apiVersion="
                + apiVersion + ", planId=" + planId + ", planName=" + planName + ", planVersion="
                + planVersion + ", httpEndpoint=" + httpEndpoint + ", apiKey=" + apiKey + ", gatewayId="
                + gatewayId + "]";
    }

}
