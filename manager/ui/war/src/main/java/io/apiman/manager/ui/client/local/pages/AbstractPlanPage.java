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

import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.common.Breadcrumb;
import io.apiman.manager.ui.client.local.pages.common.VersionSelector;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;


/**
 * Base class for all Plan pages.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPlanPage extends AbstractPage {
    
    @PageState
    protected String org;
    @PageState
    protected String plan;
    @PageState
    protected String version;
    
    OrganizationBean organizationBean;
    PlanBean planBean;
    List<PlanVersionBean> versionBeans;
    PlanVersionBean versionBean;
    
    @Inject @DataField
    Breadcrumb breadcrumb;

    @Inject @DataField
    Anchor thePlan;
    @Inject @DataField
    VersionSelector versions;
    @Inject @DataField
    Anchor toNewPlanVersion;
    
    @Inject @DataField
    Anchor toPlanOverview;
    @Inject @DataField
    Anchor toPlanPolicies;
    @Inject @DataField
    Anchor toPlanActivity;

    /**
     * Constructor.
     */
    public AbstractPlanPage() {
    }
    
    @PostConstruct
    protected void _aapPostConstruct() {
        versions.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onVersionSelected(event.getValue());
            }
        });
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getOrganization(org, new IRestInvokerCallback<OrganizationBean>() {
            @Override
            public void onSuccess(OrganizationBean response) {
                organizationBean = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        rest.getPlanVersions(org, plan, new IRestInvokerCallback<List<PlanVersionBean>>() {
            @Override
            public void onSuccess(List<PlanVersionBean> response) {
                versionBeans = response;
                // If no version is specified in the URL, use the most recent (first in the list)
                if (version == null) {
                    versionBean = response.get(0);
                } else {
                    for (PlanVersionBean avb : response) {
                        if (avb.getVersion().equals(version)) {
                            versionBean = avb;
                        }
                    }
                }
                if (versionBean == null) {
                    try {
                        throw new PlanVersionNotFoundException();
                    } catch (Throwable t) {
                        dataPacketError(t);
                    }
                } else {
                    planBean = versionBean.getPlan();
                    dataPacketLoaded();
                    onPlanVersionLoaded();
                }
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 2;
    }

    /**
     * Called when the plan version is successfully loaded.  This provides a 
     * way for subclasses to start their own data fetching if they require the plan
     * version to do it.
     */
    protected void onPlanVersionLoaded() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.emptyMap());
        String orgPlansHref = navHelper.createHrefToPage(OrgPlansPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String planOverviewHref = navHelper.createHrefToPage(PlanOverviewPage.class, MultimapUtil.fromMultiple("org", org, "plan", plan, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String planPoliciesHref = navHelper.createHrefToPage(PlanPoliciesPage.class, MultimapUtil.fromMultiple("org", org, "plan", plan, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String planActivityHref = navHelper.createHrefToPage(PlanActivityPage.class, MultimapUtil.fromMultiple("org", org, "plan", plan, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String newPlanVersionHref = navHelper.createHrefToPage(NewPlanVersionPage.class, MultimapUtil.fromMultiple("org", org, "plan", plan, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        toPlanOverview.setHref(planOverviewHref);
        toPlanPolicies.setHref(planPoliciesHref);
        toPlanActivity.setHref(planActivityHref);
        toNewPlanVersion.setHref(newPlanVersionHref);

        thePlan.setHref(planOverviewHref);
        thePlan.setText(planBean.getName());
        
        versions.setVersions(getVersions());
        versions.setValue(this.versionBean.getVersion());
        
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addItem(orgPlansHref, "shield", organizationBean.getName()); //$NON-NLS-1$
        breadcrumb.addActiveItem("bar-chart-o", planBean.getName()); //$NON-NLS-1$
    }

    /**
     * @return a list of versions
     */
    private List<String> getVersions() {
        List<String> v = new ArrayList<String>();
        for (PlanVersionBean versionBean : versionBeans) {
            v.add(versionBean.getVersion());
        }
        return v;
    }

    /**
     * Called when the user switches versions.
     * @param value
     */
    protected void onVersionSelected(String value) {
        navigation.goTo(getClass(), MultimapUtil.fromMultiple("org", org, "plan", plan, "version", value)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
