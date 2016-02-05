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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.apiman.manager.api.beans.apis.ApiDefinitionType;
import io.apiman.manager.api.beans.apis.EndpointType;

import java.io.Serializable;


/**
 * A bean modeling an API available in one of the configured API catalogs.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class AvailableApiBean implements Serializable {

    private static final long serialVersionUID = 8809181555186141741L;

    private String id;
    private String icon;
    private String endpoint;
    private String routeEndpoint;
    private EndpointType endpointType = EndpointType.rest;
    private String name;
    private String description;
    private String definitionUrl;
    private String routeDefinitionUrl;
    private ApiDefinitionType definitionType;

    /**
     * Constructor.
     */
    public AvailableApiBean() {
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
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getRouteEndpoint() {
        return routeEndpoint;
    }

    public void setRouteEndpoint(String routeEndpoint) {
        this.routeEndpoint = routeEndpoint;
    }

    public String getRouteDefinitionUrl() {
        return routeDefinitionUrl;
    }

    public void setRouteDefinitionUrl(String routeDefinitionUrl) {
        this.routeDefinitionUrl = routeDefinitionUrl;
    }

}
