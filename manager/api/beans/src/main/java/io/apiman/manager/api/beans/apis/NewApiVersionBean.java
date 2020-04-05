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
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Bean used when creating a new version of an API.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonInclude(Include.NON_NULL)
public class NewApiVersionBean implements Serializable {

    private static final long serialVersionUID = 7207058698209555294L;

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

}
