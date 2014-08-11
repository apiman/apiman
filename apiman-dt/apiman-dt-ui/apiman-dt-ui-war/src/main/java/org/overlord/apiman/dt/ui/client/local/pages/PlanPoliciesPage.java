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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyType;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.events.RemovePolicyEvent;
import org.overlord.apiman.dt.ui.client.local.pages.common.PolicyList;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.user.client.ui.Anchor;


/**
 * The "Plan" page, with the Policies tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/plan-policies.html#page")
@Page(path="plan-policies")
@Dependent
public class PlanPoliciesPage extends AbstractPlanPage {

    private List<PolicyBean> policyBeans;

    @Inject @DataField
    Anchor toNewPolicy;
    @Inject @DataField
    PolicyList policies;
    
    /**
     * Constructor.
     */
    public PlanPoliciesPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        policies.addRemovePolicyHandler(new RemovePolicyEvent.Handler() {
            @Override
            public void onRemovePolicy(RemovePolicyEvent event) {
                doRemovePolicy(event.getPolicy());
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        // we'll trigger an additional load after the service version has been loaded (hence the +1)
        return rval + 1;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPlanPage#onPlanVersionLoaded()
     */
    @Override
    protected void onPlanVersionLoaded() {
        String orgId = org;
        String planId = plan;
        String planVersion = versionBean.getVersion();
        rest.getPlanPolicies(orgId, planId, planVersion, new IRestInvokerCallback<List<PolicyBean>>() {
            @Override
            public void onSuccess(List<PolicyBean> response) {
                policyBeans = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractAppPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        
        String newPolicyHref = navHelper.createHrefToPage(NewPolicyPage.class,
                MultimapUtil.fromMultiple("org", this.planBean.getOrganizationId(), "id", this.planBean.getId(),  //$NON-NLS-1$ //$NON-NLS-2$
                        "ver", this.versionBean.getVersion(), "type", PolicyType.Plan.toString())); //$NON-NLS-1$ //$NON-NLS-2$
        toNewPolicy.setHref(newPolicyHref);
        policies.setValue(policyBeans);
    }

    /**
     * Called when the user chooses to remove a policy.
     * @param policy
     */
    protected void doRemovePolicy(final PolicyBean policy) {
        rest.removePolicy(policy.getType(), org, versionBean.getPlan().getId(), versionBean.getVersion(),
                policy.getId(), new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                policyBeans.remove(policy);
                policies.setValue(policyBeans);
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
        return i18n.format(AppMessages.TITLE_PLAN_POLICIES, planBean.getName());
    }

}
