/*
 * Copyright 2022 Scheer PAS Schweiz AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.beans.apis.dto;

import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.EndpointContentType;
import io.apiman.manager.api.beans.apis.EndpointType;
import io.apiman.manager.api.beans.idm.DiscoverabilityLevel;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO for {@link io.apiman.manager.api.beans.apis.ApiVersionBean}.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
// TODO(msavy): record candidate
public class ApiVersionBeanDto {
    @NotNull
    private Long id;
    @NotNull
    private ApiBeanDto api;
    @NotBlank
    private String version;
    @NotNull
    private ApiStatus status;
    private String endpoint;
    @NotNull
    private EndpointType endpointType;
    @NotNull
    private EndpointContentType endpointContentType;
    private Map<String, String> endpointProperties;
    private Set<ApiGatewayBean> gateways;
    private boolean publicAPI;
    @NotNull
    private DiscoverabilityLevel publicDiscoverability;
    private Set<ApiPlanBeanDto> plans;
    @NotBlank
    private String createdBy;
    @NotNull
    private Date createdOn;
    @NotBlank
    private String modifiedBy;
    @NotNull
    private Date modifiedOn;
    @NotNull
    private Date publishedOn;
    @NotNull
    private Date retiredOn;
    @NotNull
    private ApiDefinitionType definitionType;
    private boolean parsePayload;
    private boolean disableKeysStrip;
    private String definitionUrl;
    private String extendedDescription;

    public ApiVersionBeanDto() {
    }

    public Long getId() {
        return id;
    }

    public ApiVersionBeanDto setId(Long id) {
        this.id = id;
        return this;
    }

    public ApiBeanDto getApi() {
        return api;
    }

    public ApiVersionBeanDto setApi(ApiBeanDto api) {
        this.api = api;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ApiVersionBeanDto setVersion(String version) {
        this.version = version;
        return this;
    }

    public ApiStatus getStatus() {
        return status;
    }

    public ApiVersionBeanDto setStatus(ApiStatus status) {
        this.status = status;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public ApiVersionBeanDto setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public EndpointType getEndpointType() {
        return endpointType;
    }

    public ApiVersionBeanDto setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
        return this;
    }

    public EndpointContentType getEndpointContentType() {
        return endpointContentType;
    }

    public ApiVersionBeanDto setEndpointContentType(EndpointContentType endpointContentType) {
        this.endpointContentType = endpointContentType;
        return this;
    }

    public Map<String, String> getEndpointProperties() {
        return endpointProperties;
    }

    public ApiVersionBeanDto setEndpointProperties(Map<String, String> endpointProperties) {
        this.endpointProperties = endpointProperties;
        return this;
    }

    public Set<ApiGatewayBean> getGateways() {
        return gateways;
    }

    public ApiVersionBeanDto setGateways(Set<ApiGatewayBean> gateways) {
        this.gateways = gateways;
        return this;
    }

    public boolean isPublicAPI() {
        return publicAPI;
    }

    public ApiVersionBeanDto setPublicAPI(boolean publicAPI) {
        this.publicAPI = publicAPI;
        return this;
    }

    public DiscoverabilityLevel getPublicDiscoverability() {
        return publicDiscoverability;
    }

    public ApiVersionBeanDto setPublicDiscoverability(DiscoverabilityLevel publicDiscoverability) {
        this.publicDiscoverability = publicDiscoverability;
        return this;
    }

    public Set<ApiPlanBeanDto> getPlans() {
        return plans;
    }

    public ApiVersionBeanDto setPlans(Set<ApiPlanBeanDto> plans) {
        this.plans = plans;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public ApiVersionBeanDto setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public ApiVersionBeanDto setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public ApiVersionBeanDto setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
        return this;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public ApiVersionBeanDto setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
        return this;
    }

    public Date getPublishedOn() {
        return publishedOn;
    }

    public ApiVersionBeanDto setPublishedOn(Date publishedOn) {
        this.publishedOn = publishedOn;
        return this;
    }

    public Date getRetiredOn() {
        return retiredOn;
    }

    public ApiVersionBeanDto setRetiredOn(Date retiredOn) {
        this.retiredOn = retiredOn;
        return this;
    }

    public ApiDefinitionType getDefinitionType() {
        return definitionType;
    }

    public ApiVersionBeanDto setDefinitionType(ApiDefinitionType definitionType) {
        this.definitionType = definitionType;
        return this;
    }

    public boolean isParsePayload() {
        return parsePayload;
    }

    public ApiVersionBeanDto setParsePayload(boolean parsePayload) {
        this.parsePayload = parsePayload;
        return this;
    }

    public boolean getDisableKeysStrip() {
        return disableKeysStrip;
    }

    public boolean isDisableKeysStrip() {
        return disableKeysStrip;
    }

    public ApiVersionBeanDto setDisableKeysStrip(boolean disableKeysStrip) {
        this.disableKeysStrip = disableKeysStrip;
        return this;
    }

    public String getDefinitionUrl() {
        return definitionUrl;
    }

    public ApiVersionBeanDto setDefinitionUrl(String definitionUrl) {
        this.definitionUrl = definitionUrl;
        return this;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public ApiVersionBeanDto setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApiVersionBeanDto that = (ApiVersionBeanDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ApiVersionBeanDto.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("api=" + api)
                .add("version='" + version + "'")
                .add("status=" + status)
                .add("endpoint='" + endpoint + "'")
                .add("endpointType=" + endpointType)
                .add("endpointContentType=" + endpointContentType)
                .add("endpointProperties=" + endpointProperties)
                .add("gateways=" + gateways)
                .add("publicAPI=" + publicAPI)
                .add("publicDiscoverability=" + publicDiscoverability)
                .add("plans=" + plans)
                .add("createdBy='" + createdBy + "'")
                .add("createdOn=" + createdOn)
                .add("modifiedBy='" + modifiedBy + "'")
                .add("modifiedOn=" + modifiedOn)
                .add("publishedOn=" + publishedOn)
                .add("retiredOn=" + retiredOn)
                .add("definitionType=" + definitionType)
                .add("parsePayload=" + parsePayload)
                .add("disableKeysStrip=" + disableKeysStrip)
                .add("definitionUrl='" + definitionUrl + "'")
                .add("extendedDescription='" + extendedDescription + "'")
                .toString();
    }
}
