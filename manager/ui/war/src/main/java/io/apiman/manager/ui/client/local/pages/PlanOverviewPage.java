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
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.Formatting;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;


/**
 * The "Plan" page, with the Overview tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/plan-overview.html#page")
@Page(path="plan-overview")
@Dependent
public class PlanOverviewPage extends AbstractPlanPage {
    
    @Inject @DataField
    Label description;
    @Inject @DataField
    InlineLabel createdOn;
    @Inject @DataField
    Anchor createdBy;
    
    @Inject @DataField
    InlineLabel version;
    @Inject @DataField
    InlineLabel status;
    @Inject @DataField
    InlineLabel versionCreatedOn;
    @Inject @DataField
    Anchor versionCreatedBy;
    
    @Inject @DataField
    Anchor ttd_toNewVersion;
    @Inject @DataField
    AsyncActionButton lockButton;

    /**
     * Constructor.
     */
    public PlanOverviewPage() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPlanPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        
        String newVersionHref = navHelper.createHrefToPage(NewPlanVersionPage.class, MultimapUtil.fromMultiple("org", org, "plan", plan)); //$NON-NLS-1$ //$NON-NLS-2$
        ttd_toNewVersion.setHref(newVersionHref);

        description.setText(planBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(planBean.getCreatedOn()));
        createdBy.setText(planBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", planBean.getCreatedBy())); //$NON-NLS-1$
        createdBy.setHref(toUserHref);

        version.setText(versionBean.getVersion());
        status.setText(versionBean.getStatus().toString());
        status.setTitle(getStatusDescription(versionBean.getStatus()));
        versionCreatedOn.setText(Formatting.formatShortDate(versionBean.getCreatedOn()));
        versionCreatedBy.setText(versionBean.getCreatedBy());
        toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", versionBean.getCreatedBy())); //$NON-NLS-1$
        versionCreatedBy.setHref(toUserHref);
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_PLAN_OVERVIEW, planBean.getName());
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        lockButton.reset();
        renderPlanStatus();
    }

    /**
     * Updates various UI bits based on the status of the plan.
     */
    protected void renderPlanStatus() {
        setStatusLabelClass(status, versionBean.getStatus());

        boolean canLock = versionBean.getStatus() == PlanStatus.Ready || versionBean.getStatus() == PlanStatus.Created;
        boolean locked = versionBean.getStatus() == PlanStatus.Locked;
        lockButton.setEnabled(canLock);
        lockButton.setVisible(!locked);
    }

    /**
     * Called when the user clicks the "Register" button.
     * @param event
     */
    @EventHandler("lockButton")
    public void onRegister(ClickEvent event) {
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
    
}
