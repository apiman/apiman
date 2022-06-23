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
package io.apiman.manager.api.beans.apis;

import io.apiman.manager.api.beans.apis.dto.UpdateApiPlanDto;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Bean used when updating a version of an API.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonInclude(Include.NON_NULL)
public class UpdateApiVersionBean implements Serializable {

    private static final long serialVersionUID = 4126848584932708146L;

    private String endpoint;
    private EndpointType endpointType;
    private EndpointContentType endpointContentType;
    private Map<String, String> endpointProperties;
    private Set<ApiGatewayBean> gateways;
    private Boolean parsePayload;
    private Boolean publicAPI;
    private Boolean disableKeysStrip;
    private @Valid LinkedHashSet<UpdateApiPlanDto> plans;
    private String extendedDescription;
    private DiscoverabilityLevel publicDiscoverability;

    /**
     * Constructor.
     */
    public UpdateApiVersionBean() {
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the endpointType
     */
    public EndpointType getEndpointType() {
        return endpointType;
    }

    /**
     * @param endpointType the endpointType to set
     */
    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }

    /**
     * @return the gateways
     */
    public Set<ApiGatewayBean> getGateways() {
        return gateways;
    }

    /**
     * @param gateways the gateways to set
     */
    public void setGateways(Set<ApiGatewayBean> gateways) {
        this.gateways = gateways;
    }

    /**
     * @return the publicAPI
     */
    public Boolean getPublicAPI() {
        return publicAPI;
    }

    /**
     * @param publicAPI the publicAPI to set
     */
    public void setPublicAPI(Boolean publicAPI) {
        this.publicAPI = publicAPI;
    }

    /**
     * @return the plans
     */
    public LinkedHashSet<UpdateApiPlanDto> getPlans() {
        return plans;
    }

    /**
     * @param plans the plans to set
     */
    public void setPlans(LinkedHashSet<UpdateApiPlanDto> plans) {
        this.plans = plans;
    }

    /**
     * @return the endpointProperties
     */
    public Map<String, String> getEndpointProperties() {
        return endpointProperties;
    }

    /**
     * @param endpointProperties the endpointProperties to set
     */
    public void setEndpointProperties(Map<String, String> endpointProperties) {
        this.endpointProperties = endpointProperties;
    }

    /**
     * @return the endpointContentType
     */
    public EndpointContentType getEndpointContentType() {
        return endpointContentType;
    }

    /**
     * @param endpointContentType the endpointContentType to set
     */
    public void setEndpointContentType(EndpointContentType endpointContentType) {
        this.endpointContentType = endpointContentType;
    }

    /**
     * @return the parsePayload
     */
    public Boolean getParsePayload() {
        return parsePayload;
    }

    /**
     * @param parsePayload the parsePayload to set
     */
    public void setParsePayload(Boolean parsePayload) {
        this.parsePayload = parsePayload;
    }

    /**
     * @return the disableKeysStrip
     */
    public Boolean getDisableKeysStrip() { return disableKeysStrip; }

    /**
     * @param disableKeysStrip the disableKeysStrip to set
     */
    public void setDisableKeysStrip(Boolean disableKeysStrip) { this.disableKeysStrip = disableKeysStrip; }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public UpdateApiVersionBean setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
        return this;
    }

    public DiscoverabilityLevel getPublicDiscoverability() {
        return publicDiscoverability;
    }

    public void setPublicDiscoverability(DiscoverabilityLevel publicDiscoverability) {
        this.publicDiscoverability = publicDiscoverability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateApiVersionBean that = (UpdateApiVersionBean) o;
        return Objects.equals(endpoint, that.endpoint) && endpointType == that.endpointType && endpointContentType == that.endpointContentType
                       && Objects.equals(endpointProperties, that.endpointProperties) && Objects.equals(gateways, that.gateways)
                       && Objects.equals(parsePayload, that.parsePayload) && Objects.equals(publicAPI, that.publicAPI) && Objects.equals(
                disableKeysStrip, that.disableKeysStrip) && Objects.equals(plans, that.plans) && Objects.equals(extendedDescription,
                that.extendedDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, endpointType, endpointContentType, endpointProperties, gateways, parsePayload, publicAPI, disableKeysStrip, plans,
                extendedDescription);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UpdateApiVersionBean.class.getSimpleName() + "[", "]")
                .add("endpoint='" + endpoint + "'")
                .add("endpointType=" + endpointType)
                .add("endpointContentType=" + endpointContentType)
                .add("endpointProperties=" + endpointProperties)
                .add("gateways=" + gateways)
                .add("parsePayload=" + parsePayload)
                .add("publicAPI=" + publicAPI)
                .add("disableKeysStrip=" + disableKeysStrip)
                .add("plans=" + plans)
                .add("extendedDescription='" + extendedDescription + "'")
                .add("publicDiscoverability=" + publicDiscoverability)
                .toString();
    }
}
