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

import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.services.ContextKeys;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user create a new Plan Version.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/new-planversion.html#page")
@Page(path="new-planversion")
@Dependent
public class NewPlanVersionPage extends AbstractPage {
    
    @PageState
    String org;
    @PageState
    String plan;
    
    @Inject
    TransitionTo<PlanOverviewPage> toPlan;
    
    @Inject @DataField
    TextBox version;
    @Inject @DataField
    SimpleCheckBox cloneCB;
    
    @Inject @DataField
    AsyncActionButton createButton;
    
    private int totalPolicies;
    private int policyCounter;
    
    /**
     * Constructor.
     */
    public NewPlanVersionPage() {
    }

    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler kph = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onFormUpdated();
            }
        };
        version.addKeyUpHandler(kph);
        PlanVersionBean oldPlan = (PlanVersionBean) currentContext.getAttribute(ContextKeys.CURRENT_PLAN_VERSION);
        if (oldPlan != null) {
            cloneCB.setValue(true);
        } else {
            cloneCB.setValue(false);
            cloneCB.setEnabled(false);
        }
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        version.setFocus(true);
        createButton.reset();
        createButton.setEnabled(false);
    }
    
    /**
     * Called when the user clicks the Create Organization button.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        createButton.onActionStarted();
        
        if (cloneCB.getValue()) {
            createAndClone();
        } else {
            create();
        }
    }

    /**
     * Create a new version of the plan and clone the previous version.
     */
    private void createAndClone() {
        PlanVersionBean newVersion = new PlanVersionBean();
        final String ver = version.getValue();
        
        PlanVersionBean oldPlan = (PlanVersionBean) currentContext.getAttribute(ContextKeys.CURRENT_PLAN_VERSION);
        final String oldVer = oldPlan.getVersion();
        newVersion.setVersion(ver);
        rest.createPlanVersion(org, plan, newVersion, new IRestInvokerCallback<PlanVersionBean>() {
            @Override
            public void onSuccess(PlanVersionBean response) {
                rest.getPlanPolicies(org, plan, oldVer, new IRestInvokerCallback<List<PolicySummaryBean>>() {
                    @Override
                    public void onSuccess(List<PolicySummaryBean> response) {
                        totalPolicies = response.size();
                        if (totalPolicies == 0) {
                            toPlan.go(MultimapUtil.fromMultiple("org", org, "plan", plan, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        } else {
                            for (PolicySummaryBean policySummaryBean : response) {
                                clonePolicy(oldVer, policySummaryBean);
                            }
                        }
                    }
                    @Override
                    public void onError(Throwable error) {
                        dataPacketError(error);
                    }
                });
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called to clone a single policy.  This fetches the policy and makes a copy of it
     * in the newly created plan.
     * @param oldVersion
     * @param policySummaryBean
     */
    protected void clonePolicy(String oldVersion, PolicySummaryBean policySummaryBean) {
        final String ver = version.getValue();
        rest.getPolicy(PolicyType.Plan, org, plan, oldVersion, policySummaryBean.getId(), new IRestInvokerCallback<PolicyBean>() {
            @Override
            public void onSuccess(PolicyBean response) {
                PolicyBean clonedPolicy = new PolicyBean();
                clonedPolicy.setConfiguration(response.getConfiguration());
                clonedPolicy.setDefinition(response.getDefinition());
                clonedPolicy.setName(response.getName());
                clonedPolicy.setOrderIndex(response.getOrderIndex());
                rest.createPolicy(PolicyType.Plan, org, plan, ver, clonedPolicy, new IRestInvokerCallback<PolicyBean>() {
                    @Override
                    public void onSuccess(PolicyBean response) {
                        policyCounter++;
                        if (totalPolicies == policyCounter) {
                            toPlan.go(MultimapUtil.fromMultiple("org", org, "plan", plan, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                    }
                    @Override
                    public void onError(Throwable error) {
                        dataPacketError(error);
                    }
                });
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Create a new version of the plan without cloning anything.
     */
    private void create() {
        PlanVersionBean newVersion = new PlanVersionBean();
        final String ver = version.getValue();
        newVersion.setVersion(ver);
        rest.createPlanVersion(org, plan, newVersion, new IRestInvokerCallback<PlanVersionBean>() {
            @Override
            public void onSuccess(PlanVersionBean response) {
                toPlan.go(MultimapUtil.fromMultiple("org", org, "plan", plan, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        return i18n.format(AppMessages.TITLE_NEW_PLAN_VERSION);
    }

    /**
     * Called whenever the user modifies the form.  Checks for form validity and then
     * enables or disables the Create button as appropriate.
     */
    protected void onFormUpdated() {
        boolean formComplete = true;
        if (version.getValue() == null || version.getValue().trim().length() == 0)
            formComplete = false;
        createButton.setEnabled(formComplete);
    }

}
