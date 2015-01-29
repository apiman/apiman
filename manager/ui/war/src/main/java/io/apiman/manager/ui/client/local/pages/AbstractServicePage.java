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
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceStatus;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.services.UpdateServiceBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent.Handler;
import io.apiman.manager.ui.client.local.pages.common.Breadcrumb;
import io.apiman.manager.ui.client.local.pages.common.VersionSelector;
import io.apiman.manager.ui.client.local.services.ContextKeys;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.Formatting;
import io.apiman.manager.ui.client.local.util.MultimapUtil;
import io.apiman.manager.ui.client.local.widgets.ConfirmationDialog;
import io.apiman.manager.ui.client.local.widgets.InlineEditableLabel;

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


/**
 * Base class for all Service pages.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractServicePage extends AbstractPage {
    
    @PageState
    protected String service;
    @PageState
    protected String org;
    @PageState
    protected String version;
    
    ServiceBean serviceBean;
    List<ServiceVersionSummaryBean> versionBeans;
    ServiceVersionBean versionBean;
    
    @Inject @DataField
    Breadcrumb breadcrumb;

    @Inject @DataField
    Anchor serviceName;
    @Inject @DataField
    VersionSelector versions;
    @Inject @DataField
    Anchor toNewServiceVersion;

    @Inject @DataField
    InlineEditableLabel description;
    @Inject @DataField
    InlineLabel createdOn;
    @Inject @DataField
    Anchor createdBy;
    @Inject @DataField
    InlineLabel status;

    @Inject @DataField
    Anchor ttd_toNewContract;
    @Inject @DataField
    Anchor ttd_toNewServiceVersion;

    @Inject @DataField
    AsyncActionButton publishButton;
    @Inject @DataField
    AsyncActionButton retireButton;

    @Inject @DataField
    Anchor toServiceOverview;
    @Inject @DataField
    Anchor toServicePlans;
    @Inject @DataField
    Anchor toServiceImpl;
    @Inject @DataField
    Anchor toServicePolicies;
    @Inject @DataField
    Anchor toServiceContracts;
    @Inject @DataField
    Anchor toServiceEndpoint;
    @Inject @DataField
    Anchor toServiceActivity;

    /**
     * Constructor.
     */
    public AbstractServicePage() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#isAuthorized()
     */
    @Override
    protected boolean isAuthorized() {
        return hasPermission(PermissionType.svcView);
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
    protected void _aspPostConstruct() {
        versions.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onVersionSelected(event.getValue());
            }
        });
        description.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String newDescription = event.getValue();
                updateServiceDescription(newDescription);
            }
        });
        description.setEmptyValueMessage(i18n.format(AppMessages.NO_DESCRIPTION));
    }

    /**
     * @param newDescription
     */
    protected void updateServiceDescription(final String newDescription) {
        UpdateServiceBean update = new UpdateServiceBean();
        update.setDescription(newDescription);;
        rest.updateService(serviceBean.getOrganization().getId(), serviceBean.getId(), update, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                serviceBean.setDescription(newDescription);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getServiceVersions(org, service, new IRestInvokerCallback<List<ServiceVersionSummaryBean>>() {
            @Override
            public void onSuccess(List<ServiceVersionSummaryBean> response) {
                versionBeans = response;
                // If no version is specified in the URL, use the most recent (first in the list)
                if (version == null) {
                    loadServiceVersion(response.get(0).getVersion());
                }
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        if (version != null) {
            loadServiceVersion(version);
        }
        return rval + 2;
    }

    /**
     * Loads the given version of the service.
     */
    protected void loadServiceVersion(String version) {
        rest.getServiceVersion(org, service, version, new IRestInvokerCallback<ServiceVersionBean>() {
            @Override
            public void onSuccess(ServiceVersionBean response) {
                versionBean = response;
                serviceBean = versionBean.getService();
                currentContext.setAttribute(ContextKeys.CURRENT_SERVICE, serviceBean);
                currentContext.setAttribute(ContextKeys.CURRENT_SERVICE_VERSION, versionBean);
                dataPacketLoaded();
                onServiceVersionLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the service version is successfully loaded.  This provides a 
     * way for subclasses to start their own data fetching if they require the service
     * version to do it.
     */
    protected void onServiceVersionLoaded() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.emptyMap());
        String orgServicesHref = navHelper.createHrefToPage(OrgServicesPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String serviceOverviewHref = navHelper.createHrefToPage(ServiceOverviewPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String serviceImplHref = navHelper.createHrefToPage(ServiceImplPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String servicePlansHref = navHelper.createHrefToPage(ServicePlansPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String servicePoliciesHref = navHelper.createHrefToPage(ServicePoliciesPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String serviceContractsHref = navHelper.createHrefToPage(ServiceContractsPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String serviceEndpointHref = navHelper.createHrefToPage(ServiceEndpointPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String serviceActivityHref = navHelper.createHrefToPage(ServiceActivityPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String newServiceVersionHref = navHelper.createHrefToPage(NewServiceVersionPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        toServiceOverview.setHref(serviceOverviewHref);
        toServiceImpl.setHref(serviceImplHref);
        toServicePlans.setHref(servicePlansHref);
        toServicePolicies.setHref(servicePoliciesHref);
        toServiceContracts.setHref(serviceContractsHref);
        toServiceEndpoint.setHref(serviceEndpointHref);
        toServiceActivity.setHref(serviceActivityHref);
        toNewServiceVersion.setHref(newServiceVersionHref);

        serviceName.setHref(serviceOverviewHref);
        serviceName.setText(serviceBean.getName());
        
        versions.setVersions(getVersions());
        versions.setValue(this.versionBean.getVersion());
        
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addItem(orgServicesHref, "shield", versionBean.getService().getOrganization().getName()); //$NON-NLS-1$
        breadcrumb.addActiveItem("puzzle-piece", serviceBean.getName()); //$NON-NLS-1$

        String newContractHref = navHelper.createHrefToPage(NewContractPage.class, MultimapUtil.fromMultiple("svcorg", org, "svc", service, "svcv", versionBean.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ttd_toNewContract.setHref(newContractHref);
        ttd_toNewServiceVersion.setHref(newServiceVersionHref);
        
        description.setValue(serviceBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(versionBean.getCreatedOn()));
        createdBy.setText(versionBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", serviceBean.getCreatedBy())); //$NON-NLS-1$
        createdBy.setHref(toUserHref);

        renderServiceStatus();
        
        description.setEnabled(hasPermission(PermissionType.svcEdit));
        
        publishButton.setTitle(i18n.format(AppMessages.PUBLISH_TOOLTIP));
        retireButton.setTitle(i18n.format(AppMessages.RETIRE_TOOLTIP));
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        super.onPageLoaded();
        publishButton.reset();
        retireButton.reset();
        renderServiceStatus();
    }

    /**
     * @return a list of versions
     */
    private List<String> getVersions() {
        List<String> v = new ArrayList<String>();
        for (ServiceVersionSummaryBean versionBean : versionBeans) {
            v.add(versionBean.getVersion());
        }
        return v;
    }

    /**
     * Called when the user switches versions.
     * @param value
     */
    protected void onVersionSelected(String value) {
        navigation.goTo(getClass(), MultimapUtil.fromMultiple("org", org, "service", service, "version", value)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Updates various UI bits based on the status of the app.
     */
    protected void renderServiceStatus() {
        status.setText(versionBean.getStatus().name());
        status.setTitle(getStatusDescription(versionBean.getStatus()));

        setStatusLabelClass(status, versionBean.getStatus());

        boolean canRegister = versionBean.getStatus() == ServiceStatus.Ready;
        boolean publishedOrRetired = versionBean.getStatus() == ServiceStatus.Published || versionBean.getStatus() == ServiceStatus.Retired;
        publishButton.setEnabled(canRegister);
        publishButton.setVisible(!publishedOrRetired);

        boolean canRetire = versionBean.getStatus() == ServiceStatus.Published;
        retireButton.setEnabled(canRetire);
        retireButton.setVisible(canRetire);
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
        action.setOrganizationId(versionBean.getService().getOrganization().getId());
        action.setEntityId(versionBean.getService().getId());
        action.setEntityVersion(versionBean.getVersion());
        rest.performAction(action, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                versionBean.setStatus(ServiceStatus.Published);
                publishButton.onActionComplete();
                status.setText(ServiceStatus.Published.toString());
                renderServiceStatus();
                hideElementsBasedOnStatus();
            }
            
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user clicks the "Retire" button.
     * @param event
     */
    @EventHandler("retireButton")
    public void onRetire(ClickEvent event) {
        retireButton.onActionStarted();

        ConfirmationDialog dialog = confirmationDialogFactory.get();
        dialog.setDialogTitle(i18n.format(AppMessages.CONFIRM_RETIRE_SERVICE));
        dialog.setDialogMessage(i18n.format(AppMessages.CONFIRM_RETIRE_SERVICE_MESSAGE, serviceBean.getName()));
        dialog.addConfirmationHandler(new Handler() {
            @Override
            public void onConfirmation(ConfirmationEvent event) {
                if (event.isConfirmed()) {
                    retireButton.onActionComplete();
                    ActionBean action = new ActionBean();
                    action.setType(ActionType.retireService);
                    action.setOrganizationId(versionBean.getService().getOrganization().getId());
                    action.setEntityId(versionBean.getService().getId());
                    action.setEntityVersion(versionBean.getVersion());
                    rest.performAction(action, new IRestInvokerCallback<Void>() {
                        @Override
                        public void onSuccess(Void response) {
                            versionBean.setStatus(ServiceStatus.Retired);
                            retireButton.onActionComplete();
                            status.setText(ServiceStatus.Retired.toString());
                            renderServiceStatus();
                            hideElementsBasedOnStatus();
                        }

                        @Override
                        public void onError(Throwable error) {
                            dataPacketError(error);
                        }
                    });
                } else {
                    retireButton.onActionComplete();
                }
            }
        });
        dialog.show();

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
     * Can be called by a subclass if something about the service version was changed.
     * This is necessary because a change to the service *may* change the service
     * status, resulting in the UI becoming out of date.
     */
    protected void refreshServiceVersion() {
        rest.getServiceVersion(org, service, versionBean.getVersion(), new IRestInvokerCallback<ServiceVersionBean>() {
            @Override
            public void onSuccess(ServiceVersionBean response) {
                versionBean = response;
                serviceBean = versionBean.getService();
                currentContext.setAttribute(ContextKeys.CURRENT_SERVICE, serviceBean);
                currentContext.setAttribute(ContextKeys.CURRENT_SERVICE_VERSION, versionBean);
                renderServiceStatus();
                hideElementsBasedOnStatus();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
}
