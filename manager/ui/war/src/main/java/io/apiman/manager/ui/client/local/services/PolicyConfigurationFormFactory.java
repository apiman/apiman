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

import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyFormType;
import io.apiman.manager.ui.client.local.pages.policy.DefaultPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.pages.policy.IPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.BasicAuthPolicyConfigForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.CachingPolicyConfigForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.IPListPolicyConfigForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.IgnoredResourcesPolicyConfigForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.JsonSchemaPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.pages.policy.forms.RateLimitingPolicyConfigForm;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * A factory for creating policy configuration forms.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class PolicyConfigurationFormFactory {
    
    @Inject
    RestInvokerService rest;
    
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
    @Inject
    Instance<CachingPolicyConfigForm> cachingPolicyFormFactory;
    
    private Map<PolicyDefinitionSummaryBean, String> policyDefSchemas = new HashMap<PolicyDefinitionSummaryBean, String>();
    
    /**
     * Constructor.
     */
    public PolicyConfigurationFormFactory() {
    }
    
    /**
     * Creates a proper configuration form.
     * @param policyDefId
     */
    public void createForm(PolicyDefinitionSummaryBean policyDefinition, IFormLoadedHandler handler) {
        if (policyDefinition.getFormType() == PolicyFormType.JsonSchema && policyDefinition.getPluginId() != null) {
            String schema = policyDefSchemas.get(policyDefinition);
            if (schema == null) {
                loadJsonSchemaForm(policyDefinition, handler);
            } else {
                JsonSchemaPolicyConfigurationForm form = new JsonSchemaPolicyConfigurationForm();
                form.init(schema);
                handler.onFormLoaded(form);
            }
            return;
        } else {
            String policyDefId = policyDefinition.getId();
            if ("IPWhitelistPolicy".equals(policyDefId)) { //$NON-NLS-1$
                handler.onFormLoaded(ipListFormFactory.get());
            } else if ("IPBlacklistPolicy".equals(policyDefId)) { //$NON-NLS-1$
                handler.onFormLoaded(ipListFormFactory.get());
            } else if ("BASICAuthenticationPolicy".equals(policyDefId)) { //$NON-NLS-1$
                handler.onFormLoaded(basicAuthFormFactory.get());
            } else if ("RateLimitingPolicy".equals(policyDefId)) { //$NON-NLS-1$
                handler.onFormLoaded(rateLimitingFormFactory.get());
            } else if ("IgnoredResourcesPolicy".equals(policyDefId)) { //$NON-NLS-1$
                handler.onFormLoaded(ignoredResourcesFormFactory.get());
            } else if ("CachingPolicy".equals(policyDefId)) { //$NON-NLS-1$
               handler.onFormLoaded(cachingPolicyFormFactory.get());
            } else {
                handler.onFormLoaded(defaultFormFactory.get());
            }
        }
    }

    /**
     * Fetchs the json schema for this policy definition, caches the result, creates a
     * json schema form, and invokes the handler.
     * @param policyDefinition
     * @param handler
     */
    private void loadJsonSchemaForm(final PolicyDefinitionSummaryBean policyDefinition, final IFormLoadedHandler handler) {
        rest.getPluginPolicySchema(policyDefinition.getPluginId(), policyDefinition.getId(), new IRestInvokerCallback<String>() {
            @Override
            public void onSuccess(String schema) {
                policyDefSchemas.put(policyDefinition, schema);
                JsonSchemaPolicyConfigurationForm form = new JsonSchemaPolicyConfigurationForm();
                form.init(schema);
                handler.onFormLoaded(form);
            }
            
            @Override
            public void onError(Throwable error) {
                handler.onFormError(error);
            }
        });
    }

    /**
     * Handler used when loading forms.  Forms are loaded asynchronously
     * so that we can fetch remote resources if necessary (e.g. plugin
     * policies).
     * @author eric.wittmann@redhat.com
     */
    public static interface IFormLoadedHandler {
        
        public void onFormLoaded(IPolicyConfigurationForm form);
        
        public void onFormError(Throwable e);
        
    }
}
