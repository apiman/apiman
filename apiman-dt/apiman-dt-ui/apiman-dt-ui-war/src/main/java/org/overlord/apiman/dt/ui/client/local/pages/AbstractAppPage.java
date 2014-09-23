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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.Breadcrumb;
import org.overlord.apiman.dt.ui.client.local.pages.common.VersionSelector;
import org.overlord.apiman.dt.ui.client.local.services.ContextKeys;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;


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
    
    OrganizationBean organizationBean;
    ApplicationBean applicationBean;
    List<ApplicationVersionBean> versionBeans;
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
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#doLoadPageData()
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
        rest.getApplicationVersions(org, app, new IRestInvokerCallback<List<ApplicationVersionBean>>() {
            @Override
            public void onSuccess(List<ApplicationVersionBean> response) {
                versionBeans = response;
                // If no version is specified in the URL, use the most recent (first in the list)
                if (version == null) {
                    versionBean = response.get(0);
                } else {
                    for (ApplicationVersionBean avb : response) {
                        if (avb.getVersion().equals(version)) {
                            versionBean = avb;
                        }
                    }
                }
                if (versionBean == null) {
                    try {
                        throw new ApplicationVersionNotFoundException();
                    } catch (Throwable t) {
                        dataPacketError(t);
                    }
                } else {
                    applicationBean = versionBean.getApplication();
                    currentContext.setAttribute(ContextKeys.CURRENT_APPLICATION, applicationBean);
                    currentContext.setAttribute(ContextKeys.CURRENT_APPLICATION_VERSION, versionBean);
                    dataPacketLoaded();
                    onAppVersionLoaded();
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
     * Called when the application version is successfully loaded.  This provides a 
     * way for subclasses to start their own data fetching if they require the app
     * version to do it.
     */
    protected void onAppVersionLoaded() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.fromMultiple());
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
        breadcrumb.addItem(orgAppsHref, "shield", organizationBean.getName()); //$NON-NLS-1$
        breadcrumb.addActiveItem("gears", applicationBean.getName()); //$NON-NLS-1$
    }

    /**
     * @return a list of versions
     */
    private List<String> getVersions() {
        List<String> v = new ArrayList<String>();
        for (ApplicationVersionBean versionBean : versionBeans) {
            v.add(versionBean.getVersion());
        }
        return v;
    }

    /**
     * Called when the user switches versions.
     * @param value
     */
    protected void onVersionSelected(String value) {
        navigation.goTo(getClass(), MultimapUtil.fromMultiple("org", org, "app", app, "version", value)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
