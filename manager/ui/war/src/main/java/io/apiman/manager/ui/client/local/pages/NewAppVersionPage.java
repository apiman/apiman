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

import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.apps.NewApplicationVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
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
 * Page that lets the user create a new Application Version.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/new-appversion.html#page")
@Page(path="new-appversion")
@Dependent
public class NewAppVersionPage extends AbstractPage {
    
    @PageState
    String org;
    @PageState
    String app;
    
    @Inject
    TransitionTo<AppOverviewPage> toApp;
    
    @Inject @DataField
    TextBox version;
    @Inject @DataField
    SimpleCheckBox cloneCB;
    
    @Inject @DataField
    AsyncActionButton createButton;
    
    private int totalPolicies;
    private int policyCounter;
    private int totalContracts;
    private int contractCounter;

    /**
     * Constructor.
     */
    public NewAppVersionPage() {
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
        ApplicationVersionBean oldApplication = (ApplicationVersionBean) currentContext.getAttribute(ContextKeys.CURRENT_APPLICATION_VERSION);
        if (oldApplication != null) {
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
     * Creates a new cloned version of the app.
     */
    private void createAndClone() {
        NewApplicationVersionBean clonedVersion = new NewApplicationVersionBean();
        final String ver = version.getValue();
        
        ApplicationVersionBean oldApplication = (ApplicationVersionBean) currentContext.getAttribute(ContextKeys.CURRENT_APPLICATION_VERSION);
        final String oldVer = oldApplication.getVersion();
        clonedVersion.setVersion(ver);
        rest.createApplicationVersion(org, app, clonedVersion, new IRestInvokerCallback<ApplicationVersionBean>() {
            @Override
            public void onSuccess(ApplicationVersionBean response) {
                rest.getApplicationPolicies(org, app, oldVer, new IRestInvokerCallback<List<PolicySummaryBean>>() {
                    @Override
                    public void onSuccess(List<PolicySummaryBean> response) {
                        clonePolicies(oldVer, response);
                    }
                    @Override
                    public void onError(Throwable error) {
                        dataPacketError(error);
                    }
                });
                rest.getApplicationContracts(org, app, oldVer, new IRestInvokerCallback<List<ContractSummaryBean>>() {
                    @Override
                    public void onSuccess(List<ContractSummaryBean> response) {
                        cloneContracts(oldVer, response);
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
     * Clones the list of policies.
     * @param oldVersion
     * @param policies
     */
    protected void clonePolicies(final String oldVersion, List<PolicySummaryBean> policies) {
        final String ver = version.getValue();
        totalPolicies = policies.size();
        if (totalPolicies == 0) {
            toApp.go(MultimapUtil.fromMultiple("org", org, "app", app, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            policyCounter = 0;
            for (PolicySummaryBean policySummaryBean : policies) {
                clonePolicy(oldVersion, policySummaryBean);
            }
        }
    }

    /**
     * Called to clone a single policy.  This fetches the policy and makes a copy of it
     * in the newly created application.
     * @param oldVersion
     * @param policySummaryBean
     */
    protected void clonePolicy(String oldVersion, PolicySummaryBean policySummaryBean) {
        final String ver = version.getValue();
        rest.getPolicy(PolicyType.Application, org, app, oldVersion, policySummaryBean.getId(), new IRestInvokerCallback<PolicyBean>() {
            @Override
            public void onSuccess(PolicyBean response) {
                PolicyBean clonedPolicy = new PolicyBean();
                clonedPolicy.setConfiguration(response.getConfiguration());
                clonedPolicy.setDefinition(response.getDefinition());
                clonedPolicy.setName(response.getName());
                clonedPolicy.setOrderIndex(response.getOrderIndex());
                rest.createPolicy(PolicyType.Application, org, app, ver, clonedPolicy, new IRestInvokerCallback<PolicyBean>() {
                    @Override
                    public void onSuccess(PolicyBean response) {
                        policyCounter++;
                        if (contractCounter == totalContracts && policyCounter == totalPolicies) {
                            toApp.go(MultimapUtil.fromMultiple("org", org, "app", app, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                    }
                    @Override
                    public void onError(Throwable error) {
                        // Don't care - just do our best.
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
     * Clones the app's contracts.
     * @param oldVersion
     * @param contracts
     */
    private void cloneContracts(String oldVersion, List<ContractSummaryBean> contracts) {
        totalContracts = contracts.size();
        contractCounter = 0;
        for (ContractSummaryBean csb : contracts) {
            cloneContract(oldVersion, csb);
        }
    }

    /**
     * @param oldVersion
     * @param csb
     */
    private void cloneContract(String oldVersion, ContractSummaryBean contractSummaryBean) {
        final String ver = version.getValue();
        NewContractBean contract = new NewContractBean();
        contract.setPlanId(contractSummaryBean.getPlanId());
        contract.setServiceId(contractSummaryBean.getServiceId());
        contract.setServiceOrgId(contractSummaryBean.getServiceOrganizationId());
        contract.setServiceVersion(contractSummaryBean.getServiceVersion());
        rest.createContract(org, app, ver, contract, new IRestInvokerCallback<ContractBean>() {
            @Override
            public void onSuccess(ContractBean response) {
                contractCounter++;
                if (contractCounter == totalContracts && policyCounter == totalPolicies) {
                    toApp.go(MultimapUtil.fromMultiple("org", org, "app", app, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
            @Override
            public void onError(Throwable error) {
                // Don't care!
            }
        });
    }

    /**
     * Creates a new empty version of the app.
     */
    private void create() {
        createButton.onActionStarted();
        NewApplicationVersionBean newVersion = new NewApplicationVersionBean();
        final String ver = version.getValue();
        newVersion.setVersion(ver);
        rest.createApplicationVersion(org, app, newVersion, new IRestInvokerCallback<ApplicationVersionBean>() {
            @Override
            public void onSuccess(ApplicationVersionBean response) {
                toApp.go(MultimapUtil.fromMultiple("org", org, "app", app, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        return i18n.format(AppMessages.TITLE_NEW_APP_VERSION);
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
