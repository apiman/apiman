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

import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.user.client.ui.Anchor;


/**
 * Base class for all Application pages.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractAppPage extends AbstractPage {
    
    @PageState
    private String app;
    @PageState
    private String org;
    
    OrganizationBean organizationBean;
    ApplicationBean applicationBean;
    
    @Inject @DataField
    Anchor organization;
    @Inject @DataField
    Anchor application;

    @Inject @DataField
    Anchor toAppOverview;
    @Inject @DataField
    Anchor toAppContracts;
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
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
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
        rest.getApplication(org, app, new IRestInvokerCallback<ApplicationBean>() {
            @Override
            public void onSuccess(ApplicationBean response) {
                applicationBean = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return 2;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String orgAppsHref = navHelper.createHrefToPage(OrgAppsPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String appOverviewHref = navHelper.createHrefToPage(AppOverviewPage.class, MultimapUtil.fromMultiple("org", org, "app", app)); //$NON-NLS-1$ //$NON-NLS-2$
        String appContractsHref = navHelper.createHrefToPage(AppContractsPage.class, MultimapUtil.fromMultiple("org", org, "app", app)); //$NON-NLS-1$ //$NON-NLS-2$
        String appPoliciesHref = navHelper.createHrefToPage(AppPoliciesPage.class, MultimapUtil.fromMultiple("org", org, "app", app)); //$NON-NLS-1$ //$NON-NLS-2$
        String appActivityHref = navHelper.createHrefToPage(AppActivityPage.class, MultimapUtil.fromMultiple("org", org, "app", app)); //$NON-NLS-1$ //$NON-NLS-2$
        toAppOverview.setHref(appOverviewHref);
        toAppContracts.setHref(appContractsHref);
        toAppPolicies.setHref(appPoliciesHref);
        toAppActivity.setHref(appActivityHref);

        organization.setHref(orgAppsHref);
        organization.setText(organizationBean.getName());
        application.setHref(appOverviewHref);
        application.setText(applicationBean.getName());
    }

}
