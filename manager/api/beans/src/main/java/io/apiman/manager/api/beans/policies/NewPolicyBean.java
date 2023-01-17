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
package io.apiman.manager.api.beans.policies;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * Bean used when creating a new policy for a plan, API, or app.
 *
 * @author eric.wittmann@redhat.com
 */
@Schema(description = "Bean used when creating a new policy for a Plan, API, or Client")
public class NewPolicyBean implements Serializable {

    private static final long serialVersionUID = -3616888650365376571L;

    private String definitionId;
    private String configuration;

    /**
     * Constructor.
     */
    public NewPolicyBean() {
    }

    /**
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    /**
     * @return the definitionId
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * @param definitionId the definitionId to set
     */
    public void setDefinitionId(String definitionId) {
        this.definitionId = definitionId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "NewPolicyBean [definitionId=" + definitionId + ", configuration=***]";
    }
}
