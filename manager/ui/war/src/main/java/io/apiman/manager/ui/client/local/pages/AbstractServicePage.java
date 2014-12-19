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

import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
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
        String serviceActivityHref = navHelper.createHrefToPage(ServiceActivityPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String newServiceVersionHref = navHelper.createHrefToPage(NewServiceVersionPage.class, MultimapUtil.fromMultiple("org", org, "service", service, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        toServiceOverview.setHref(serviceOverviewHref);
        toServiceImpl.setHref(serviceImplHref);
        toServicePlans.setHref(servicePlansHref);
        toServicePolicies.setHref(servicePoliciesHref);
        toServiceContracts.setHref(serviceContractsHref);
        toServiceActivity.setHref(serviceActivityHref);
        toNewServiceVersion.setHref(newServiceVersionHref);

        serviceName.setHref(serviceOverviewHref);
        serviceName.setText(serviceBean.getName());
        
        versions.setVersions(getVersions());
        versions.setValue(this.versionBean.getVersion());
        
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addItem(orgServicesHref, "shield", versionBean.getService().getOrganization().getName()); //$NON-NLS-1$
        breadcrumb.addActiveItem("puzzle-piece", serviceBean.getName()); //$NON-NLS-1$
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

}
