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
import io.apiman.manager.api.beans.services.ServiceStatus;
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
 * The "Service" page, with the Overview tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/service-overview.html#page")
@Page(path="service-overview")
@Dependent
public class ServiceOverviewPage extends AbstractServicePage {
    
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
    Anchor ttd_toNewContract;
    @Inject @DataField
    Anchor ttd_toNewServiceVersion;

    @Inject @DataField
    AsyncActionButton publishButton;
    
    /**
     * Constructor.
     */
    public ServiceOverviewPage() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        
        String newContractHref = navHelper.createHrefToPage(NewContractPage.class, MultimapUtil.fromMultiple("svcorg", org, "svc", service, "svcv", versionBean.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ttd_toNewContract.setHref(newContractHref);
        String newServiceVersionHref = navHelper.createHrefToPage(NewServiceVersionPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        ttd_toNewServiceVersion.setHref(newServiceVersionHref);
        
        description.setText(serviceBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(serviceBean.getCreatedOn()));
        createdBy.setText(serviceBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", serviceBean.getCreatedBy())); //$NON-NLS-1$
        createdBy.setHref(toUserHref);

        version.setText(versionBean.getVersion());
        status.setText(versionBean.getStatus().name());
        status.setTitle(getStatusDescription(versionBean.getStatus()));
        versionCreatedOn.setText(Formatting.formatShortDate(versionBean.getCreatedOn()));
        versionCreatedBy.setText(versionBean.getCreatedBy());
        toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", versionBean.getCreatedBy())); //$NON-NLS-1$
        versionCreatedBy.setHref(toUserHref);

        renderServiceStatus();
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        publishButton.reset();
        renderServiceStatus();
    }

    /**
     * Updates various UI bits based on the status of the app.
     */
    protected void renderServiceStatus() {
        setStatusLabelClass(status, versionBean.getStatus());

        boolean canRegister = versionBean.getStatus() == ServiceStatus.Ready;
        publishButton.setEnabled(canRegister);
        boolean publishedOrRetired = versionBean.getStatus() == ServiceStatus.Published || versionBean.getStatus() == ServiceStatus.Retired;
        publishButton.setVisible(!publishedOrRetired);
    }
    
    /**
     * Called when the user clicks the "Publish" button.
     * @param event
     */
    @EventHandler("publishButton")
    public void onPublish(ClickEvent event) {
        publishButton.onActionStarted();

        ActionBean action = new ActionBean();
        action.setType(ActionType.publishService);
        action.setOrganizationId(versionBean.getService().getOrganizationId());
        action.setEntityId(versionBean.getService().getId());
        action.setEntityVersion(versionBean.getVersion());
        rest.performAction(action, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                versionBean.setStatus(ServiceStatus.Published);
                publishButton.onActionComplete();
                status.setText(ServiceStatus.Published.toString());
                renderServiceStatus();
            }
            
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Sets the proper CSS class(es) on the label based on the service's status.
     * @param label
     * @param status
     */
    private static void setStatusLabelClass(InlineLabel label, ServiceStatus status) {
        label.getElement().setClassName("apiman-label"); //$NON-NLS-1$
        switch (status) {
        case Created:
        case Ready:
            label.getElement().addClassName("apiman-label-warning"); //$NON-NLS-1$
            break;
        case Published:
            label.getElement().addClassName("apiman-label-success"); //$NON-NLS-1$
            break;
        case Retired:
            label.getElement().addClassName("apiman-label-default"); //$NON-NLS-1$
            break;
        }
    }

    /**
     * Gets a description of the status.
     * @param status
     */
    private String getStatusDescription(ServiceStatus status) {
        switch (status) {
        case Created:
            return i18n.format(AppMessages.SERVICE_STATUS_CREATED);
        case Published:
            return i18n.format(AppMessages.SERVICE_STATUS_PUBLISHED);
        case Ready:
            return i18n.format(AppMessages.SERVICE_STATUS_READY);
        case Retired:
            return i18n.format(AppMessages.SERVICE_STATUS_RETIRED);
        default:
            return null;
        }
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SERVICE_OVERVIEW, serviceBean.getName());
    }

}
