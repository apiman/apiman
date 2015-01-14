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

import io.apiman.manager.api.beans.actions.ActionBean;
import io.apiman.manager.api.beans.actions.ActionType;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.common.Breadcrumb;
import io.apiman.manager.ui.client.local.pages.common.VersionSelector;
import io.apiman.manager.ui.client.local.services.ContextKeys;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.Formatting;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;


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

    PlanBean planBean;
    List<PlanVersionSummaryBean> versionBeans;
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
    Label description;
    @Inject @DataField
    InlineLabel createdOn;
    @Inject @DataField
    Anchor createdBy;
    @Inject @DataField
    InlineLabel status;

    @Inject @DataField
    Anchor ttd_toNewVersion;
    @Inject @DataField
    AsyncActionButton lockButton;

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

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#isAuthorized()
     */
    @Override
    protected boolean isAuthorized() {
        return hasPermission(PermissionType.planView);
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getOrganizationId()
     */
    @Override
    protected String getOrganizationId() {
        return org;
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getEntityStatus()
     */
    @Override
    protected String getEntityStatus() {
        return versionBean.getStatus().name();
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
        rest.getPlanVersions(org, plan, new IRestInvokerCallback<List<PlanVersionSummaryBean>>() {
            @Override
            public void onSuccess(List<PlanVersionSummaryBean> response) {
                versionBeans = response;
                // If no version is specified in the URL, use the most recent (first in the list)
                if (version == null) {
                    loadPlanVersion(response.get(0).getVersion());
                }
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        if (version != null) {
            loadPlanVersion(version);
        }
        return rval + 2;
    }

    /**
     * Loads a version of a plan.
     * @param version
     */
    protected void loadPlanVersion(String version) {
        rest.getPlanVersion(org, plan, version, new IRestInvokerCallback<PlanVersionBean>() {
            @Override
            public void onSuccess(PlanVersionBean response) {
                versionBean = response;
                planBean = versionBean.getPlan();
                currentContext.setAttribute(ContextKeys.CURRENT_PLAN, planBean);
                currentContext.setAttribute(ContextKeys.CURRENT_PLAN_VERSION, versionBean);
                dataPacketLoaded();
                onPlanVersionLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
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
        breadcrumb.addItem(orgPlansHref, "shield", versionBean.getPlan().getOrganization().getName()); //$NON-NLS-1$
        breadcrumb.addActiveItem("bar-chart-o", planBean.getName()); //$NON-NLS-1$

        String newVersionHref = navHelper.createHrefToPage(NewPlanVersionPage.class, MultimapUtil.fromMultiple("org", org, "plan", plan)); //$NON-NLS-1$ //$NON-NLS-2$
        ttd_toNewVersion.setHref(newVersionHref);

        description.setText(planBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(versionBean.getCreatedOn()));
        createdBy.setText(versionBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", planBean.getCreatedBy())); //$NON-NLS-1$
        createdBy.setHref(toUserHref);
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        super.onPageLoaded();
        lockButton.reset();
        renderPlanStatus();
    }

    /**
     * Updates various UI bits based on the status of the plan.
     */
    protected void renderPlanStatus() {
        status.setText(versionBean.getStatus().toString());
        status.setTitle(getStatusDescription(versionBean.getStatus()));
        setStatusLabelClass(status, versionBean.getStatus());

        boolean canLock = versionBean.getStatus() == PlanStatus.Ready || versionBean.getStatus() == PlanStatus.Created;
        boolean locked = versionBean.getStatus() == PlanStatus.Locked;
        lockButton.setEnabled(canLock);
        lockButton.setVisible(!locked);
    }

    /**
     * Called when the user clicks the "Lock" button.
     * @param event
     */
    @EventHandler("lockButton")
    public void onLock(ClickEvent event) {
        lockButton.onActionStarted();

        ActionBean action = new ActionBean();
        action.setType(ActionType.lockPlan);
        action.setOrganizationId(versionBean.getPlan().getOrganization().getId());
        action.setEntityId(versionBean.getPlan().getId());
        action.setEntityVersion(versionBean.getVersion());
        rest.performAction(action, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                lockButton.onActionComplete();
                versionBean.setStatus(PlanStatus.Locked);
                status.setText(PlanStatus.Locked.toString());
                renderPlanStatus();
                hideElementsBasedOnStatus();
            }
            
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Sets the proper CSS class(es) on the label based on the plan's status.
     * @param label
     * @param status
     */
    private static void setStatusLabelClass(InlineLabel label, PlanStatus status) {
        label.getElement().setClassName("apiman-label"); //$NON-NLS-1$
        switch (status) {
        case Created:
        case Ready:
            label.getElement().addClassName("apiman-label-warning"); //$NON-NLS-1$
            break;
        case Locked:
            label.getElement().addClassName("apiman-label-success"); //$NON-NLS-1$
            break;
        }
    }

    /**
     * Gets an explanation of the status.
     * @param status
     */
    private String getStatusDescription(PlanStatus status) {
        switch (status) {
        case Created:
            return i18n.format(AppMessages.PLAN_STATUS_CREATED);
        case Ready:
            return i18n.format(AppMessages.PLAN_STATUS_CREATED);
        case Locked:
            return i18n.format(AppMessages.PLAN_STATUS_LOCKED);
        default:
            return null;
        }
    }
    
    /**
     * @return a list of versions
     */
    private List<String> getVersions() {
        List<String> v = new ArrayList<String>();
        for (PlanVersionSummaryBean versionBean : versionBeans) {
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
