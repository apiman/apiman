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
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.ui.client.local.pages.common.VersionSelector;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

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
    
    OrganizationBean organizationBean;
    ServiceBean serviceBean;
    List<ServiceVersionBean> versionBeans;
    ServiceVersionBean versionBean;
    
    @Inject @DataField
    Anchor organization;
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
    Anchor toServiceActivity;

    /**
     * Constructor.
     */
    public AbstractServicePage() {
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
        rest.getServiceVersions(org, service, new IRestInvokerCallback<List<ServiceVersionBean>>() {
            @Override
            public void onSuccess(List<ServiceVersionBean> response) {
                versionBeans = response;
                // If no version is specified in the URL, use the most recent (first in the list)
                if (version == null) {
                    versionBean = response.get(0);
                } else {
                    for (ServiceVersionBean avb : response) {
                        if (avb.getVersion().equals(version)) {
                            versionBean = avb;
                        }
                    }
                }
                if (versionBean == null) {
                    try {
                        throw new ServiceVersionNotFoundException();
                    } catch (Throwable t) {
                        dataPacketError(t);
                    }
                } else {
                    serviceBean = versionBean.getService();
                    dataPacketLoaded();
                    onServiceVersionLoaded();
                }
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return 2;
    }

    /**
     * Called when the service version is successfully loaded.  This provides a 
     * way for subclasses to start their own data fetching if they require the service
     * version to do it.
     */
    protected void onServiceVersionLoaded() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String orgServicesHref = navHelper.createHrefToPage(OrgServicesPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String serviceOverviewHref = navHelper.createHrefToPage(ServiceOverviewPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        String serviceImplHref = navHelper.createHrefToPage(ServiceImplPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        String servicePlansHref = navHelper.createHrefToPage(ServicePlansPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        String servicePoliciesHref = navHelper.createHrefToPage(ServicePoliciesPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        String serviceActivityHref = navHelper.createHrefToPage(ServiceActivityPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        String newServiceVersionHref = navHelper.createHrefToPage(NewServiceVersionPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        toServiceOverview.setHref(serviceOverviewHref);
        toServiceImpl.setHref(serviceImplHref);
        toServicePlans.setHref(servicePlansHref);
        toServicePolicies.setHref(servicePoliciesHref);
        toServiceActivity.setHref(serviceActivityHref);
        toNewServiceVersion.setHref(newServiceVersionHref);

        organization.setHref(orgServicesHref);
        organization.setText(organizationBean.getName());
        serviceName.setHref(serviceOverviewHref);
        serviceName.setText(serviceBean.getName());
        
        versions.setVersions(getVersions());
        versions.setValue(this.versionBean.getVersion());
    }

    /**
     * @return a list of versions
     */
    private List<String> getVersions() {
        List<String> v = new ArrayList<String>();
        for (ServiceVersionBean versionBean : versionBeans) {
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
