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
package io.apiman.manager.ui.client.local.pages;

import io.apiman.manager.ui.client.local.services.PolicyConfigurationFormFactory;

import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;

/**
 * Base class for all Policy pages.
 *
 * @author rubenrm1@gmail.com
 */
public abstract class AbstractPolicyPage extends AbstractPage {
    
    @Inject
    protected TransitionTo<AppPoliciesPage> toAppPolicies;
    @Inject
    protected TransitionTo<ServicePoliciesPage> toServicePolicies;
    @Inject
    protected TransitionTo<PlanPoliciesPage> toPlanPolicies;

    @PageState
    protected String org;
    @PageState
    protected String id;
    @PageState
    protected String ver;
    @PageState
    protected String type;
    
    @Inject
    protected PolicyConfigurationFormFactory formFactory;
    
    @Override
    protected String getOrganizationId() {
        return org;
    }
    
}
