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

import io.apiman.manager.api.beans.summary.PolicySummaryBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models the list of policies that would get applied if a service were invoked
 * via a particular plan.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class PolicyChainBean implements Serializable {

    private static final long serialVersionUID = -497197512733345793L;
    
    private List<PolicySummaryBean> policies = new ArrayList<>();
    
    /**
     * Constructor.
     */
    public PolicyChainBean() {
    }

    /**
     * @return the policies
     */
    public List<PolicySummaryBean> getPolicies() {
        return policies;
    }

    /**
     * @param policies the policies to set
     */
    public void setPolicies(List<PolicySummaryBean> policies) {
        this.policies = policies;
    }

}
