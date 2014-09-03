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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.events.CreateContractEvent;
import org.overlord.apiman.dt.ui.client.local.events.CreateContractEvent.Handler;
import org.overlord.apiman.dt.ui.client.local.pages.common.Breadcrumb;
import org.overlord.apiman.dt.ui.client.local.pages.consumer.ConsumerServicePlanList;
import org.overlord.apiman.dt.ui.client.local.pages.consumer.ServiceCard;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;


/**
 * The "Service Details" page - part of the consumer UI.  This page
 * allows users to see details about an Service.  It displays useful
 * information such as the list of members and the services offered by
 * the service.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/consumer-service.html#page")
@Page(path="cservice")
@Dependent
public class ConsumerServicePage extends AbstractPage {
    
    @PageState
    protected String org;
    @PageState
    protected String service;
    @PageState
    protected String version;

    @Inject @DataField
    Breadcrumb breadcrumb;

    @Inject @DataField
    private ServiceCard serviceCard;
    @Inject @DataField
    private ConsumerServicePlanList plans;
    
    @Inject
    private TransitionTo<ConsumerServicePage> toThis;
    @Inject
    private TransitionTo<NewContractPage> toNewContract;

    protected OrganizationBean organizationBean;
    protected ServiceBean serviceBean;
    protected List<ServiceVersionBean> versionBeans;
    protected ServiceVersionBean versionBean;
    protected List<ServicePlanSummaryBean> planBeans;

    /**
     * Constructor.
     */
    public ConsumerServicePage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        serviceCard.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onVersionSelected(event.getValue());
            }
        });
        plans.addCreateContractHandler(new Handler() {
            @Override
            public void onCreateContract(CreateContractEvent event) {
                ConsumerServicePage.this.onCreateContract((ServicePlanSummaryBean) event.getBean());
            }
        });
    }
    
    /**
     * Called when the user clicks the Create Contract button on one of the plans.
     * @param bean
     */
    protected void onCreateContract(final ServicePlanSummaryBean bean) {
        toNewContract.go(MultimapUtil.fromMultiple(
                "svcorg", this.serviceBean.getOrganizationId(),  //$NON-NLS-1$
                "svc", this.serviceBean.getId(), //$NON-NLS-1$
                "svcv", this.versionBean.getVersion(), //$NON-NLS-1$
                "planid", bean.getPlanId())); //$NON-NLS-1$
    }

    /**
     * Called when the user picks a different version of the Service.
     * @param value
     */
    protected void onVersionSelected(String value) {
        toThis.go(MultimapUtil.fromMultiple("org", org, "service", service, "version", value)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
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
                    getPlansForServiceVersion();
                }
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 3;
    }

    /**
     * Makes a REST call to get the plan information for the selected service
     * version.
     */
    protected void getPlansForServiceVersion() {
        rest.getServiceVersionPlans(org, service, versionBean.getVersion(), new IRestInvokerCallback<List<ServicePlanSummaryBean>>() {
            @Override
            public void onSuccess(List<ServicePlanSummaryBean> response) {
                planBeans = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        serviceCard.setValue(versionBean);
        serviceCard.setOrganization(organizationBean);
        serviceCard.setVersions(versionBeans);
        serviceCard.selectVersion(versionBean.getVersion());
        
        plans.setValue(planBeans);

        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.fromMultiple());
        String consumerServicesHref = navHelper.createHrefToPage(ConsumerServicesPage.class, MultimapUtil.fromMultiple());
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addItem(consumerServicesHref, "search", i18n.format(AppMessages.SERVICES)); //$NON-NLS-1$
        breadcrumb.addActiveItem("puzzle-piece", serviceBean.getName()); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_CONSUME_SERVICE);
    }

}
