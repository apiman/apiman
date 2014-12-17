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
package io.apiman.manager.ui.client.local.services;

import io.apiman.manager.ui.client.local.pages.policy.DefaultPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.pages.policy.IPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.BasicAuthPolicyConfigForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.IPListPolicyConfigForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.IgnoredResourcesPolicyConfigForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.RateLimitingPolicyConfigForm;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

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
    Instance<IPListPolicyConfigForm> ipListFormFactory;
    @Inject
    Instance<BasicAuthPolicyConfigForm> basicAuthFormFactory;
    @Inject
    Instance<RateLimitingPolicyConfigForm> rateLimitingFormFactory;
    @Inject
    Instance<DefaultPolicyConfigurationForm> defaultFormFactory;
    @Inject
    Instance<IgnoredResourcesPolicyConfigForm> ignoredResourcesFormFactory;
    
    /**
     * Constructor.
     */
    public PolicyConfigurationFormFactory() {
    }
    
    /**
     * Creates a proper configuration form.
     * @param policyDefId
     */
    public IPolicyConfigurationForm createForm(String policyDefId) {
        if ("IPWhitelistPolicy".equals(policyDefId)) { //$NON-NLS-1$
            return ipListFormFactory.get();
        }
        if ("IPBlacklistPolicy".equals(policyDefId)) { //$NON-NLS-1$
            return ipListFormFactory.get();
        }
        if ("BASICAuthenticationPolicy".equals(policyDefId)) { //$NON-NLS-1$
            return basicAuthFormFactory.get();
        }
        if ("RateLimitingPolicy".equals(policyDefId)) { //$NON-NLS-1$
            return rateLimitingFormFactory.get();
        }
        if ("IgnoredResourcesPolicy".equals(policyDefId)) { //$NON-NLS-1$
        	return ignoredResourcesFormFactory.get();
        }
        return defaultFormFactory.get();
    }

}
