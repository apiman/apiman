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
package io.apiman.manager.api.beans.plans;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Bean used when creating a plan.
 * @author eric.wittmann@redhat.com
 */
@Portable
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class UpdatePlanBean implements Serializable {
    
    private static final long serialVersionUID = 5836879095486293752L;
    
    private String description;
    
    /**
     * Constructor.
     */
    public UpdatePlanBean() {
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
    
}
