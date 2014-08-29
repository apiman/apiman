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
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.api.beans.summary.PlanSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.consumer.ConsumerOrgServiceList;
import org.overlord.apiman.dt.ui.client.local.pages.consumer.ServiceCard;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;

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
    private ServiceCard serviceCard;
    @Inject @DataField
    private ConsumerOrgServiceList plans;

    protected ServiceBean serviceBean;
    protected List<PlanSummaryBean> planBeans;

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
    }
    
    /**
     * Called when the user picks a different version of the Service.
     * @param value
     */
    protected void onVersionSelected(String value) {
        // TODO implement this
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        rest.getService(org, service, new IRestInvokerCallback<ServiceBean>() {
            @Override
            public void onSuccess(ServiceBean response) {
                serviceBean = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 3;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        serviceCard.setValue(serviceBean);
//        plans.setValue(planBeans);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_CONSUME_ORG);
    }

}
