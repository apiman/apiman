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

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Bean used when creating a new version of an API.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonInclude(Include.NON_NULL)
@Schema(description = "Initial information to create a new API version.")
public class NewApiVersionBean implements Serializable {

    private static final long serialVersionUID = 7207058698209555294L;

    @NotBlank
    private String version;
    private boolean clone;
    private String cloneVersion;

    private String endpoint;
    private EndpointType endpointType;
    private EndpointContentType endpointContentType;
    private Boolean publicAPI;
    private Boolean parsePayload;
    private Boolean disableKeysStrip;
    private Set<ApiPlanBean> plans;
    private String definitionUrl;
    private ApiDefinitionType definitionType;
    private String extendedDescription;

    /**
     * Constructor.
     */
    public NewApiVersionBean() {
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
     * @return the clone
     */
    public boolean isClone() {
        return clone;
    }

    /**
     * @param clone the clone to set
     */
    public void setClone(boolean clone) {
        this.clone = clone;
    }

    /**
     * @return the cloneVersion
     */
    public String getCloneVersion() {
        return cloneVersion;
    }

    /**
     * @param cloneVersion the cloneVersion to set
     */
    public void setCloneVersion(String cloneVersion) {
        this.cloneVersion = cloneVersion;
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
    public Set<ApiPlanBean> getPlans() {
        return plans;
    }

    /**
     * @param plans the plans to set
     */
    public void setPlans(Set<ApiPlanBean> plans) {
        this.plans = plans;
    }

    /**
     * @return the definitionUrl
     */
    public String getDefinitionUrl() {
        return definitionUrl;
    }

    /**
     * @param definitionUrl the definitionUrl to set
     */
    public void setDefinitionUrl(String definitionUrl) {
        this.definitionUrl = definitionUrl;
    }

    /**
     * @return the definitionType
     */
    public ApiDefinitionType getDefinitionType() {
        return definitionType;
    }

    /**
     * @param definitionType the definitionType to set
     */
    public void setDefinitionType(ApiDefinitionType definitionType) {
        this.definitionType = definitionType;
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

    public NewApiVersionBean setExtendedDescription(String extendedDescription) {
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
        NewApiVersionBean that = (NewApiVersionBean) o;
        return clone == that.clone && Objects.equals(version, that.version) && Objects.equals(cloneVersion, that.cloneVersion)
                       && Objects.equals(endpoint, that.endpoint) && endpointType == that.endpointType && endpointContentType == that.endpointContentType
                       && Objects.equals(publicAPI, that.publicAPI) && Objects.equals(
                parsePayload, that.parsePayload) && Objects.equals(disableKeysStrip, that.disableKeysStrip) && Objects.equals(plans, that.plans)
                       && Objects.equals(definitionUrl, that.definitionUrl) && definitionType == that.definitionType && Objects.equals(
                extendedDescription, that.extendedDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, clone, cloneVersion, endpoint, endpointType, endpointContentType, publicAPI, parsePayload,
                disableKeysStrip,
                plans, definitionUrl, definitionType, extendedDescription);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NewApiVersionBean.class.getSimpleName() + "[", "]")
                .add("version='" + version + "'")
                .add("clone=" + clone)
                .add("cloneVersion='" + cloneVersion + "'")
                .add("endpoint='" + endpoint + "'")
                .add("endpointType=" + endpointType)
                .add("endpointContentType=" + endpointContentType)
                .add("publicAPI=" + publicAPI)
                .add("parsePayload=" + parsePayload)
                .add("disableKeysStrip=" + disableKeysStrip)
                .add("plans=" + plans)
                .add("definitionUrl='" + definitionUrl + "'")
                .add("definitionType=" + definitionType)
                .add("extendedDescription='" + extendedDescription + "'")
                .toString();
    }
}
