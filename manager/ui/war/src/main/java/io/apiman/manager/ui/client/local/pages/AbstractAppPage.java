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
import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationStatus;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.apps.UpdateApplicationBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.summary.ApplicationVersionSummaryBean;
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
 * Base class for all Application pages.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractAppPage extends AbstractPage {
    
    @PageState
    protected String app;
    @PageState
    protected String org;
    @PageState
    protected String version;
    
    ApplicationBean applicationBean;
    List<ApplicationVersionSummaryBean> versionBeans;
    ApplicationVersionBean versionBean;
    
    @Inject @DataField
    Breadcrumb breadcrumb;
    
    @Inject @DataField
    Anchor application;
    @Inject @DataField
    VersionSelector versions;
    @Inject @DataField
    Anchor toNewAppVersion;

    @Inject @DataField
    InlineEditableLabel description;
    @Inject @DataField
    InlineLabel createdOn;
    @Inject @DataField
    Anchor createdBy;
    @Inject @DataField
    InlineLabel status;

    @Inject @DataField
    Anchor ttd_toConsumeServices;
    @Inject @DataField
    Anchor ttd_toNewContract;
    @Inject @DataField
    Anchor ttd_toNewVersion;

    @Inject @DataField
    AsyncActionButton registerButton;
    @Inject @DataField
    AsyncActionButton unregisterButton;

    @Inject @DataField
    Anchor toAppOverview;
    @Inject @DataField
    Anchor toAppContracts;
    @Inject @DataField
    Anchor toAppApis;
    @Inject @DataField
    Anchor toAppPolicies;
    @Inject @DataField
    Anchor toAppActivity;

    /**
     * Constructor.
     */
    public AbstractAppPage() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#isAuthorized()
     */
    @Override
    protected boolean isAuthorized() {
        return hasPermission(PermissionType.appView);
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
        description.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String newDescription = event.getValue();
                updateAppDescription(newDescription);
            }
        });
        description.setEmptyValueMessage(i18n.format(AppMessages.NO_DESCRIPTION));
    }

    /**
     * @param newDescription
     */
    protected void updateAppDescription(final String newDescription) {
        UpdateApplicationBean update = new UpdateApplicationBean();
        update.setDescription(newDescription);
        rest.updateApplication(applicationBean.getOrganization().getId(), applicationBean.getId(), update, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                applicationBean.setDescription(newDescription);
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
        rest.getApplicationVersions(org, app, new IRestInvokerCallback<List<ApplicationVersionSummaryBean>>() {
            @Override
            public void onSuccess(List<ApplicationVersionSummaryBean> response) {
                versionBeans = response;
                // If no version is specified in the URL, use the most recent (first in the list)
                if (version == null) {
                    loadApplicationVersion(response.get(0).getVersion());
                }
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        if (version != null) {
            loadApplicationVersion(version);
        }
        return rval + 2;
    }

    /**
     * Loads a specific app version.
     * @param version 
     */
    protected void loadApplicationVersion(String version) {
        rest.getApplicationVersion(org, app, version, new IRestInvokerCallback<ApplicationVersionBean>() {
            @Override
            public void onSuccess(ApplicationVersionBean response) {
                versionBean = response;
                applicationBean = versionBean.getApplication();
                currentContext.setAttribute(ContextKeys.CURRENT_APPLICATION, applicationBean);
                currentContext.setAttribute(ContextKeys.CURRENT_APPLICATION_VERSION, versionBean);
                dataPacketLoaded();
                onAppVersionLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the application version is successfully loaded.  This provides a 
     * way for subclasses to start their own data fetching if they require the app
     * version to do it.
     */
    protected void onAppVersionLoaded() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.emptyMap());
        String orgAppsHref = navHelper.createHrefToPage(OrgAppsPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String appOverviewHref = navHelper.createHrefToPage(AppOverviewPage.class, MultimapUtil.fromMultiple("org", org, "app", app, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String appContractsHref = navHelper.createHrefToPage(AppContractsPage.class, MultimapUtil.fromMultiple("org", org, "app", app, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String appApisHref = navHelper.createHrefToPage(AppApisPage.class, MultimapUtil.fromMultiple("org", org, "app", app, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String appPoliciesHref = navHelper.createHrefToPage(AppPoliciesPage.class, MultimapUtil.fromMultiple("org", org, "app", app, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String appActivityHref = navHelper.createHrefToPage(AppActivityPage.class, MultimapUtil.fromMultiple("org", org, "app", app, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String newAppVersionHref = navHelper.createHrefToPage(NewAppVersionPage.class, MultimapUtil.fromMultiple("org", org, "app", app, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        toAppOverview.setHref(appOverviewHref);
        toAppContracts.setHref(appContractsHref);
        toAppApis.setHref(appApisHref);
        toAppPolicies.setHref(appPoliciesHref);
        toAppActivity.setHref(appActivityHref);
        toNewAppVersion.setHref(newAppVersionHref);

        application.setHref(appOverviewHref);
        application.setText(applicationBean.getName());
        
        versions.setVersions(getVersions());
        versions.setValue(this.versionBean.getVersion());
        
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addItem(orgAppsHref, "shield", versionBean.getApplication().getOrganization().getName()); //$NON-NLS-1$
        breadcrumb.addActiveItem("gears", applicationBean.getName()); //$NON-NLS-1$

        String consumeServicesHref = navHelper.createHrefToPage(ConsumerServicesPage.class, MultimapUtil.emptyMap());
        ttd_toConsumeServices.setHref(consumeServicesHref);
        String newContractHref = navHelper.createHrefToPage(NewContractPage.class, MultimapUtil.fromMultiple("apporg", org, "app", app, "appv", versionBean.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ttd_toNewContract.setHref(newContractHref);
        String newVersionHref = navHelper.createHrefToPage(NewAppVersionPage.class, MultimapUtil.fromMultiple("org", org, "app", app)); //$NON-NLS-1$ //$NON-NLS-2$
        ttd_toNewVersion.setHref(newVersionHref);

        description.setValue(applicationBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(versionBean.getCreatedOn()));
        createdBy.setText(versionBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserAppsPage.class,
                MultimapUtil.fromMultiple("user", applicationBean.getCreatedBy())); //$NON-NLS-1$
        createdBy.setHref(toUserHref);
        
        renderApplicationStatus();

        description.setEnabled(hasPermission(PermissionType.appEdit));
        
        registerButton.setTitle(i18n.format(AppMessages.REGISTER_TOOLTIP));
        unregisterButton.setTitle(i18n.format(AppMessages.UNREGISTER_TOOLTIP));
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        super.onPageLoaded();
        registerButton.reset();
        unregisterButton.reset();
        renderApplicationStatus();
    }

    /**
     * Updates various UI bits based on the status of the app.
     */
    protected void renderApplicationStatus() {
        status.setText(versionBean.getStatus().toString());
        status.setTitle(getStatusDescription(versionBean.getStatus()));
        setStatusLabelClass(status, versionBean.getStatus());

        boolean canRegister = versionBean.getStatus() == ApplicationStatus.Ready;
        boolean registeredOrRetired = versionBean.getStatus() == ApplicationStatus.Registered || versionBean.getStatus() == ApplicationStatus.Retired;
        registerButton.setEnabled(canRegister);
        registerButton.setVisible(!registeredOrRetired);
        
        boolean canRetire = versionBean.getStatus() == ApplicationStatus.Registered;
        unregisterButton.setEnabled(canRetire);
        unregisterButton.setVisible(canRetire);
    }
    
    /**
     * @return a list of versions
     */
    private List<String> getVersions() {
        List<String> v = new ArrayList<String>();
        for (ApplicationVersionSummaryBean versionBean : versionBeans) {
            v.add(versionBean.getVersion());
        }
        return v;
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
        action.setOrganizationId(versionBean.getApplication().getOrganization().getId());
        action.setEntityId(versionBean.getApplication().getId());
        action.setEntityVersion(versionBean.getVersion());
        rest.performAction(action, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                registerButton.onActionComplete();
                versionBean.setStatus(ApplicationStatus.Registered);
                status.setText(ApplicationStatus.Registered.toString());
                renderApplicationStatus();
                hideElementsBasedOnStatus();
            }
            
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * Called when the user clicks the "Unregister" button.
     * @param event
     */
    @EventHandler("unregisterButton")
    public void onUnregister(ClickEvent event) {
        unregisterButton.onActionStarted();
        
        ConfirmationDialog dialog = confirmationDialogFactory.get();
        dialog.setDialogTitle(i18n.format(AppMessages.CONFIRM_UNREGISTER_APP_TITLE));
        dialog.setDialogMessage(i18n.format(AppMessages.CONFIRM_UNREGISTER_APP_MESSAGE, applicationBean.getName()));
        dialog.addConfirmationHandler(new Handler() {
            @Override
            public void onConfirmation(ConfirmationEvent event) {
                if (event.isConfirmed()) {
                    unregisterButton.onActionComplete();
                    ActionBean action = new ActionBean();
                    action.setType(ActionType.unregisterApplication);
                    action.setOrganizationId(versionBean.getApplication().getOrganization().getId());
                    action.setEntityId(versionBean.getApplication().getId());
                    action.setEntityVersion(versionBean.getVersion());
                    rest.performAction(action, new IRestInvokerCallback<Void>() {
                        @Override
                        public void onSuccess(Void response) {
                            unregisterButton.onActionComplete();
                            versionBean.setStatus(ApplicationStatus.Retired);
                            status.setText(ApplicationStatus.Retired.toString());
                            renderApplicationStatus();
                            hideElementsBasedOnStatus();
                        }

                        @Override
                        public void onError(Throwable error) {
                            dataPacketError(error);
                        }
                    });
                } else {
                    unregisterButton.onActionComplete();
                }
            }
        });
        dialog.show();
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
     * Gets an explanation of the status.
     * @param status
     */
    private String getStatusDescription(ApplicationStatus status) {
        switch (status) {
        case Created:
            return i18n.format(AppMessages.APP_STATUS_CREATED);
        case Ready:
            return i18n.format(AppMessages.APP_STATUS_READY);
        case Registered:
            return i18n.format(AppMessages.APP_STATUS_REGISTERED);
        case Retired:
            return i18n.format(AppMessages.APP_STATUS_RETIRED);
        default:
            return null;
        }
    }

    /**
     * Can be called by a subclass if something about the application version was changed.
     * This is necessary because a change to the application *may* change the application
     * status, resulting in the UI becoming out of date.
     */
    protected void refreshApplicationVersion() {
        rest.getApplicationVersion(org, app, versionBean.getVersion(), new IRestInvokerCallback<ApplicationVersionBean>() {
            @Override
            public void onSuccess(ApplicationVersionBean response) {
                versionBean = response;
                applicationBean = versionBean.getApplication();
                currentContext.setAttribute(ContextKeys.CURRENT_APPLICATION, applicationBean);
                currentContext.setAttribute(ContextKeys.CURRENT_APPLICATION_VERSION, versionBean);
                renderApplicationStatus();
                hideElementsBasedOnStatus();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * Called when the user switches versions.
     * @param value
     */
    protected void onVersionSelected(String value) {
        navigation.goTo(getClass(), MultimapUtil.fromMultiple("org", org, "app", app, "version", value)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
