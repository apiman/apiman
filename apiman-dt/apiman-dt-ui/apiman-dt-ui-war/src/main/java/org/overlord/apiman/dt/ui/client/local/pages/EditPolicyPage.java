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
package org.overlord.apiman.dt.ui.client.local.pages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyDefinitionBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyType;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.policy.DefaultPolicyConfigurationForm;
import org.overlord.apiman.dt.ui.client.local.pages.policy.IPolicyConfigurationForm;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.apiman.dt.ui.client.local.widgets.H3Label;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * Page that lets the user create a new Policy.  This page allows the user
 * to create a policy for applications, services, and plans.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/edit-policy.html#page")
@Page(path="edit-policy")
@Dependent
public class EditPolicyPage extends AbstractPage {

    @Inject
    TransitionTo<AppPoliciesPage> toAppPolicies;
    @Inject
    TransitionTo<ServicePoliciesPage> toServicePolicies;
    @Inject
    TransitionTo<PlanPoliciesPage> toPlanPolicies;

    @PageState
    String org;
    @PageState
    String id;
    @PageState
    String ver;
    @PageState
    String type;
    @PageState
    String policy;
    
    @DataField @Inject
    H3Label policyHeading;
    @DataField @Inject
    FlowPanel policyFormWrapper;
    @Inject @DataField
    AsyncActionButton updateButton;
    
    @Inject
    Instance<DefaultPolicyConfigurationForm> defaultFormFactory;
    
    IPolicyConfigurationForm policyForm;
    
    PolicyBean policyBean;
    
    /**
     * Constructor.
     */
    public EditPolicyPage() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int size = super.loadPageData();
        PolicyType pt = PolicyType.valueOf(type);
        rest.getPolicy(pt, org, id, ver, new Long(policy), new IRestInvokerCallback<PolicyBean>() {
            @Override
            public void onSuccess(PolicyBean response) {
                policyBean = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return size + 1;
    }
    
    @PostConstruct
    protected void postConstruct() {
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        updateButton.reset();
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        policyForm = createPolicyConfigForm(policyBean.getDefinition());
        policyForm.setValue(policyBean.getConfiguration());
        policyFormWrapper.clear();
        policyFormWrapper.add(policyForm);

        String heading = policyBean.getName() + " "  + "Configuration"; //$NON-NLS-1$ //$NON-NLS-2$
        policyHeading.setText(heading);
        policyHeading.setVisible(true);
    }

    /**
     * Create the right policy config form for the type of policy selected by the user.
     * @param value
     */
    private IPolicyConfigurationForm createPolicyConfigForm(PolicyDefinitionBean value) {
        return defaultFormFactory.get();
    }
    
    /**
     * Called when the user clicks the Update Policy button.
     * @param event
     */
    @EventHandler("updateButton")
    public void onUpdate(ClickEvent event) {
        updateButton.onActionStarted();
        final PolicyType policyType = PolicyType.valueOf(type);
        final PolicyBean updatedPolicyConfig = new PolicyBean();
        updatedPolicyConfig.setConfiguration(policyForm.getValue());
        
        rest.updatePolicy(policyType, org, id, ver, new Long(policy), updatedPolicyConfig, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                updateButton.onActionComplete();
                if (policyType == PolicyType.Application) {
                    toAppPolicies.go(MultimapUtil.fromMultiple("app", id, "org", org, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else if (policyType == PolicyType.Service) {
                    toServicePolicies.go(MultimapUtil.fromMultiple("service", id, "org", org, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else if (policyType == PolicyType.Plan) {
                    toServicePolicies.go(MultimapUtil.fromMultiple("plan", id, "org", org, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_EDIT_POLICY);
    }

}
