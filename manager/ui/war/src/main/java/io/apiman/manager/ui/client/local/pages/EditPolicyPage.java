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

import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent.Handler;
import io.apiman.manager.ui.client.local.pages.policy.IPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;
import io.apiman.manager.ui.client.local.widgets.H3Label;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * Page that lets the user create a new Policy.  This page allows the user
 * to create a policy for applications, services, and plans.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/edit-policy.html#page")
@Page(path="edit-policy")
@Dependent
public class EditPolicyPage extends AbstractPolicyPage {

    @DataField @Inject
    H3Label policyHeading;
    @DataField @Inject
    FlowPanel policyFormWrapper;
    @Inject @DataField
    AsyncActionButton updateButton;
    @Inject @DataField
    Anchor cancelButton;
    
    IPolicyConfigurationForm policyForm;
    
    PolicyBean policyBean;
    
    /**
     * Constructor.
     */
    public EditPolicyPage() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int size = super.doLoadPageData();
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        policyForm = formFactory.createForm(policyBean.getDefinition().getId());
        policyForm.addIsFormValidHandler(new Handler() {
            @Override
            public void onIsFormValid(IsFormValidEvent event) {
                updateButton.setEnabled(event.isValid());
            }
        });
        policyFormWrapper.clear();
        policyFormWrapper.add(policyForm);
        policyForm.setValue(policyBean.getConfiguration());

        String heading = policyBean.getName() + " "  + "Configuration"; //$NON-NLS-1$ //$NON-NLS-2$
        policyHeading.setText(heading);
        policyHeading.setVisible(true);
        
        final PolicyType policyType = PolicyType.valueOf(type);
        PermissionType requiredPermission = null;
        if (policyType == PolicyType.Application) {
            requiredPermission = PermissionType.appEdit;
        } else if (policyType == PolicyType.Service) {
            requiredPermission = PermissionType.svcEdit;
        } else if (policyType == PolicyType.Plan) {
            requiredPermission = PermissionType.planEdit;
        }
        if (!hasPermission(requiredPermission)) {
            updateButton.setVisible(false);
            cancelButton.setVisible(false);
        }
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
                    toPlanPolicies.go(MultimapUtil.fromMultiple("plan", id, "org", org, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_EDIT_POLICY);
    }

}
