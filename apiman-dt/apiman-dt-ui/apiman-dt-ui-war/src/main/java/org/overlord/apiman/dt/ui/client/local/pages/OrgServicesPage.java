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
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.org.OrgServiceList;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The "Organization" page, with the Services tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/org-services.html#page")
@Page(path="org-services")
@Dependent
public class OrgServicesPage extends AbstractOrgPage {

    private List<ServiceSummaryBean> serviceBeans;

    @Inject @DataField
    TransitionAnchor<NewServicePage> toNewService;

    @Inject @DataField
    TextBox serviceFilter;
    @Inject @DataField
    OrgServiceList services;

    /**
     * Constructor.
     */
    public OrgServicesPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        serviceFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                filterServices();
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        rest.getServices(org, new IRestInvokerCallback<List<ServiceSummaryBean>>() {
            @Override
            public void onSuccess(List<ServiceSummaryBean> response) {
                serviceBeans = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 1;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        services.setValue(serviceBeans);
    }

    /**
     * Apply a filter to the list of services.
     */
    protected void filterServices() {
        List<ServiceSummaryBean> filtered = new ArrayList<ServiceSummaryBean>();
        for (ServiceSummaryBean service : serviceBeans) {
            if (matchesFilter(service)) {
                filtered.add(service);
            }
        }
        services.setValue(filtered);
    }

    /**
     * Returns true if the given service matches the current filter.
     * @param service
     */
    private boolean matchesFilter(ServiceSummaryBean service) {
        if (serviceFilter.getValue() == null || serviceFilter.getValue().trim().length() == 0)
            return true;
        if (service.getName().toUpperCase().contains(serviceFilter.getValue().toUpperCase()))
            return true;
        return false;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_ORG_SERVICES, organizationBean.getName());
    }

}
