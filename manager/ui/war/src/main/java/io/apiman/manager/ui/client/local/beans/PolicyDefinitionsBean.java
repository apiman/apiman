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
package io.apiman.manager.ui.client.local.beans;

import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;

import java.io.Serializable;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models a list of policy definitions as a bean.  Used only internally
 * to the UI when importing policy definitions.
 * 
 * @author eric.wittmann@redhat.com
 */
@Portable
public class PolicyDefinitionsBean implements Serializable {

    private static final long serialVersionUID = -2157445556536599783L;
    
    private List<PolicyDefinitionBean> definitions;

    /**
     * Constructor.
     */
    public PolicyDefinitionsBean() {
    }

    /**
     * @return the definitions
     */
    public List<PolicyDefinitionBean> getDefinitions() {
        return definitions;
    }

    /**
     * @param definitions the definitions to set
     */
    public void setDefinitions(List<PolicyDefinitionBean> definitions) {
        this.definitions = definitions;
    }
}
