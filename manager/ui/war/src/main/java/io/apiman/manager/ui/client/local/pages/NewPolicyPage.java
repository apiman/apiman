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

import io.apiman.manager.api.beans.policies.NewPolicyBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent;
import io.apiman.manager.ui.client.local.events.IsFormValidEvent.Handler;
import io.apiman.manager.ui.client.local.pages.policy.IPolicyConfigurationForm;
import io.apiman.manager.ui.client.local.pages.policy.PolicyDefinitionSelectBox;
import io.apiman.manager.ui.client.local.services.PolicyConfigurationFormFactory.IFormLoadedHandler;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;
import io.apiman.manager.ui.client.local.widgets.H3Label;

import java.util.List;

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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;


/**
 * Page that lets the user create a new Policy.  This page allows the user
 * to create a policy for applications, services, and plans.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/new-policy.html#page")
@Page(path="add-policy")
@Dependent
public class NewPolicyPage extends AbstractPolicyPage {

    @DataField @Inject
    H3Label policyHeading;
    @DataField @Inject
    PolicyDefinitionSelectBox typeSelector;
    @DataField @Inject
    FlowPanel policyFormWrapper;
    @Inject @DataField
    AsyncActionButton createButton;

    IPolicyConfigurationForm policyForm;
    
    List<PolicyDefinitionSummaryBean> policyDefBeans;
    
    /**
     * Constructor.
     */
    public NewPolicyPage() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int size = super.doLoadPageData();
        rest.listPolicyDefinitions(new IRestInvokerCallback<List<PolicyDefinitionSummaryBean>>() {
            @Override
            public void onSuccess(List<PolicyDefinitionSummaryBean> response) {
                policyDefBeans = response;
                // Add "null" at entry 0 to indicate no selection
                policyDefBeans.add(0, null);
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
        typeSelector.addValueChangeHandler(new ValueChangeHandler<PolicyDefinitionSummaryBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<PolicyDefinitionSummaryBean> event) {
                onPolicyTypeChange(typeSelector.getValue());
            }
        });
        policyFormWrapper.setVisible(false);
        policyFormWrapper.clear();
        policyHeading.setVisible(false);
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        createButton.reset();
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        typeSelector.setOptions(policyDefBeans);
        createButton.setEnabled(false);
    }

    /**
     * Called when the user changes the type of policy she wants to add.
     * @param value
     */
    protected void onPolicyTypeChange(final PolicyDefinitionSummaryBean value) {
        if (value == null) {
            policyFormWrapper.setVisible(false);
            policyFormWrapper.clear();
            policyHeading.setVisible(false);
        } else {
            formFactory.createForm(value, new IFormLoadedHandler() {
                @Override
                public void onFormLoaded(IPolicyConfigurationForm form) {
                    policyForm = form;
                    policyForm.addIsFormValidHandler(new Handler() {
                        @Override
                        public void onIsFormValid(IsFormValidEvent event) {
                            createButton.setEnabled(event.isValid());
                        }
                    });
                    policyForm.setValue(null);
                    policyFormWrapper.clear();
                    policyFormWrapper.add(policyForm);
                    policyFormWrapper.setVisible(true);
                    
                    String heading = value.getName() + " "  + "Configuration"; //$NON-NLS-1$ //$NON-NLS-2$
                    policyHeading.setText(heading);
                    policyHeading.setVisible(true);
                }
                @Override
                public void onFormError(Throwable e) {
                    dataPacketError(e);
                }
            });
        }
    }
    
    /**
     * Called when the user clicks the Create Policy button.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        createButton.onActionStarted();
        final PolicyType policyType = PolicyType.valueOf(type);
        final PolicyDefinitionSummaryBean policyDef = typeSelector.getValue();
        
        NewPolicyBean bean = new NewPolicyBean();
        bean.setConfiguration(this.policyForm.getValue());
        bean.setDefinitionId(policyDef.getId());
        rest.createPolicy(policyType, org, id, ver, bean, new IRestInvokerCallback<PolicyBean>() {
            @Override
            public void onSuccess(PolicyBean response) {
                createButton.onActionComplete();
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
        return i18n.format(AppMessages.TITLE_NEW_POLICY);
    }

}
