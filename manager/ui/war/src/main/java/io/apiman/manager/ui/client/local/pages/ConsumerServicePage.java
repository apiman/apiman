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

import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.policies.PolicyChainBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ServicePlanSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.events.CreateContractEvent;
import io.apiman.manager.ui.client.local.events.CreateContractEvent.Handler;
import io.apiman.manager.ui.client.local.events.ShowPolicyChainEvent;
import io.apiman.manager.ui.client.local.pages.common.Breadcrumb;
import io.apiman.manager.ui.client.local.pages.consumer.ConsumerServicePlanList;
import io.apiman.manager.ui.client.local.pages.consumer.ServiceCard;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

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
@Templated("/io/apiman/manager/ui/client/local/site/consumer-service.html#page")
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
    protected List<ServiceVersionSummaryBean> versionBeans;
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
        plans.addShowPolicyChainHandler(new ShowPolicyChainEvent.Handler() {
            @Override
            public void onShowPolicyChain(ShowPolicyChainEvent event) {
                String planId = event.getPlanId();
                ConsumerServicePage.this.onShowPolicyChain(planId);
            }
        });
    }
    
    /**
     * Called when the user clicks on one of the 
     * @param planId
     */
    protected void onShowPolicyChain(final String planId) {
        rest.getServicePlanPolicyChain(organizationBean.getId(), serviceBean.getId(), versionBean.getVersion(), planId, new IRestInvokerCallback<PolicyChainBean>() {
            @Override
            public void onSuccess(PolicyChainBean response) {
                plans.renderPolicyChain(planId, response);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user clicks the Create Contract button on one of the plans.
     * @param bean
     */
    protected void onCreateContract(final ServicePlanSummaryBean bean) {
        toNewContract.go(MultimapUtil.fromMultiple(
                "svcorg", this.serviceBean.getOrganization().getId(),  //$NON-NLS-1$
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
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        if (version != null) {
            loadServiceVersion(version);
        }
        return rval + 3;
    }

    /**
     * Loads the service version.
     */
    protected void loadServiceVersion(String version) {
        rest.getServiceVersion(org, service, version, new IRestInvokerCallback<ServiceVersionBean>() {
            @Override
            public void onSuccess(ServiceVersionBean response) {
                versionBean = response;
                serviceBean = versionBean.getService();
                dataPacketLoaded();
                getPlansForServiceVersion();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        serviceCard.setValue(versionBean);
        serviceCard.setOrganization(organizationBean);
        serviceCard.setVersions(versionBeans);
        serviceCard.selectVersion(versionBean.getVersion());
        
        plans.setValue(planBeans);

        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.emptyMap());
        String consumerServicesHref = navHelper.createHrefToPage(ConsumerServicesPage.class, MultimapUtil.emptyMap());
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addItem(consumerServicesHref, "search", i18n.format(AppMessages.SERVICES)); //$NON-NLS-1$
        breadcrumb.addActiveItem("puzzle-piece", serviceBean.getName()); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_CONSUME_SERVICE);
    }

}
