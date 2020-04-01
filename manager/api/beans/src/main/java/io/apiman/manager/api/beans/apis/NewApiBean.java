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
 * Bean used when creating an API.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonInclude(Include.NON_NULL)
public class NewApiBean implements Serializable {

    private static final long serialVersionUID = 8811488441452291116L;

    private String name;
    private String description;

    private String initialVersion;

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
    public NewApiBean() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the initialVersion
     */
    public String getInitialVersion() {
        return initialVersion;
    }

    /**
     * @param initialVersion the initialVersion to set
     */
    public void setInitialVersion(String initialVersion) {
        this.initialVersion = initialVersion;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "NewApiBean [name=" + name + ", description=" + description + ", initialVersion="
                + initialVersion + "]";
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
