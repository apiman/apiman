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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.actions.ActionBean;
import org.overlord.apiman.dt.api.beans.actions.ActionType;
import org.overlord.apiman.dt.api.beans.apps.ApplicationStatus;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.Formatting;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;


/**
 * The "Application" page, with the Overview tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/app-overview.html#page")
@Page(path="app-overview")
@Dependent
public class AppOverviewPage extends AbstractAppPage {
    
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
    Anchor ttd_toConsumeServices;
    @Inject @DataField
    Anchor ttd_toNewContract;
    @Inject @DataField
    Anchor ttd_toNewVersion;

    @Inject @DataField
    AsyncActionButton registerButton;
    
    /**
     * Constructor.
     */
    public AppOverviewPage() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractAppPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();

        String consumeServicesHref = navHelper.createHrefToPage(ConsumerServicesPage.class, MultimapUtil.fromMultiple());
        ttd_toConsumeServices.setHref(consumeServicesHref);
        String newContractHref = navHelper.createHrefToPage(NewContractPage.class, MultimapUtil.fromMultiple("apporg", org, "app", app, "appv", versionBean.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ttd_toNewContract.setHref(newContractHref);
        String newVersionHref = navHelper.createHrefToPage(NewAppVersionPage.class, MultimapUtil.fromMultiple("org", org, "app", app)); //$NON-NLS-1$ //$NON-NLS-2$
        ttd_toNewVersion.setHref(newVersionHref);

        description.setText(applicationBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(applicationBean.getCreatedOn()));
        createdBy.setText(applicationBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserAppsPage.class,
                MultimapUtil.fromMultiple("user", applicationBean.getCreatedBy())); //$NON-NLS-1$
        createdBy.setHref(toUserHref);

        version.setText(versionBean.getVersion());
        status.setText(versionBean.getStatus().toString());
        versionCreatedOn.setText(Formatting.formatShortDate(versionBean.getCreatedOn()));
        versionCreatedBy.setText(versionBean.getCreatedBy());
        toUserHref = navHelper.createHrefToPage(UserAppsPage.class,
                MultimapUtil.fromMultiple("user", versionBean.getCreatedBy())); //$NON-NLS-1$
        versionCreatedBy.setHref(toUserHref);

        renderApplicationStatus();
    }

    /**
     * Sets the proper CSS class(es) on the label based on the application's status.
     * @param label
     * @param status
     */
    private static void setStatusLabelClass(InlineLabel label, ApplicationStatus status) {
        label.getElement().setClassName("apiman-label"); //$NON-NLS-1$
        switch (status) {
        case Created:
        case Ready:
            label.getElement().addClassName("apiman-label-warning"); //$NON-NLS-1$
            break;
        case Registered:
            label.getElement().addClassName("apiman-label-success"); //$NON-NLS-1$
            break;
        case Retired:
            label.getElement().addClassName("apiman-label-default"); //$NON-NLS-1$
            break;
        }
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        registerButton.reset();
        renderApplicationStatus();
    }

    /**
     * Updates various UI bits based on the status of the app.
     */
    protected void renderApplicationStatus() {
        setStatusLabelClass(status, versionBean.getStatus());

        boolean canRegister = versionBean.getStatus() == ApplicationStatus.Ready;
        registerButton.setEnabled(canRegister);
        boolean registeredOrRetired = versionBean.getStatus() == ApplicationStatus.Registered || versionBean.getStatus() == ApplicationStatus.Retired;
        registerButton.setVisible(!registeredOrRetired);
    }
    
    /**
     * Called when the user clicks the "Register" button.
     * @param event
     */
    @EventHandler("registerButton")
    public void onRegister(ClickEvent event) {
        registerButton.onActionStarted();

        ActionBean action = new ActionBean();
        action.setType(ActionType.registerApplication);
        action.setOrganizationId(versionBean.getApplication().getOrganizationId());
        action.setEntityId(versionBean.getApplication().getId());
        action.setEntityVersion(versionBean.getVersion());
        rest.performAction(action, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                registerButton.onActionComplete();
                versionBean.setStatus(ApplicationStatus.Registered);
                status.setText(ApplicationStatus.Registered.toString());
                renderApplicationStatus();
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
        return i18n.format(AppMessages.TITLE_APP_OVERVIEW, applicationBean.getName());
    }

}
