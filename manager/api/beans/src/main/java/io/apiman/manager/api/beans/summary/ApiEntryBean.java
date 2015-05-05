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

    private String serviceOrgId;
    private String serviceOrgName;
    private String serviceId;
    private String serviceName;
    private String serviceVersion;

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
     * @return the serviceOrgId
     */
    public String getServiceOrgId() {
        return serviceOrgId;
    }

    /**
     * @param serviceOrgId the serviceOrgId to set
     */
    public void setServiceOrgId(String serviceOrgId) {
        this.serviceOrgId = serviceOrgId;
    }

    /**
     * @return the serviceOrgName
     */
    public String getServiceOrgName() {
        return serviceOrgName;
    }

    /**
     * @param serviceOrgName the serviceOrgName to set
     */
    public void setServiceOrgName(String serviceOrgName) {
        this.serviceOrgName = serviceOrgName;
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
        return "ApiEntryBean [serviceOrgId=" + serviceOrgId + ", serviceOrgName=" + serviceOrgName
                + ", serviceId=" + serviceId + ", serviceName=" + serviceName + ", serviceVersion="
                + serviceVersion + ", planId=" + planId + ", planName=" + planName + ", planVersion="
                + planVersion + ", httpEndpoint=" + httpEndpoint + ", apiKey=" + apiKey + ", gatewayId="
                + gatewayId + "]";
    }

}
