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
package io.apiman.manager.api.beans.services;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Bean used when updating a service definition via a URL.
 *
 * @author eric.wittmann@redhat.com
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class NewServiceDefinitionBean implements Serializable {

    private static final long serialVersionUID = 8305206288757642775L;

    private String definitionUrl;
    private ServiceDefinitionType definitionType;

    /**
     * Constructor.
     */
    public NewServiceDefinitionBean() {
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
    public ServiceDefinitionType getDefinitionType() {
        return definitionType;
    }

    /**
     * @param definitionType the definitionType to set
     */
    public void setDefinitionType(ServiceDefinitionType definitionType) {
        this.definitionType = definitionType;
    }

}
