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
package org.overlord.apiman.dt.ui.client.local.services;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.ui.client.local.pages.policy.DefaultPolicyConfigurationForm;
import org.overlord.apiman.dt.ui.client.local.pages.policy.IPolicyConfigurationForm;
import org.overlord.apiman.dt.ui.client.local.pages.policy.forms.IPListPolicyConfigurationForm;

/**
 * A factory for creating policy configuration forms.
 * 
 * TODO allow some form of dynamic-ish contribution of form impls
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PolicyConfigurationFormFactory {
    
    @Inject
    Instance<IPListPolicyConfigurationForm> ipListFormFactory;
    @Inject
    Instance<DefaultPolicyConfigurationForm> defaultFormFactory;
    
    /**
     * Constructor.
     */
    public PolicyConfigurationFormFactory() {
    }
    
    /**
     * Creates a proper configuration form.
     * @param policyDef
     */
    public IPolicyConfigurationForm createForm(PolicyDefinitionBean policyDef) {
        if ("IPWhitelistPolicy".equals(policyDef.getId())) { //$NON-NLS-1$
            return ipListFormFactory.get();
        }
        if ("IPBlacklistPolicy".equals(policyDef.getId())) { //$NON-NLS-1$
            return ipListFormFactory.get();
        }
        return defaultFormFactory.get();
    }

}
