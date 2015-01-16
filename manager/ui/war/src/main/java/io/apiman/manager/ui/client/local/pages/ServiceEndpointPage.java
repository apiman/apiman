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

import io.apiman.manager.api.beans.summary.ServiceVersionEndpointSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.TextArea;


/**
 * The "Service" page, with the Endpoint tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/service-endpoint.html#page")
@Page(path="service-endpoint")
@Dependent
public class ServiceEndpointPage extends AbstractServicePage {

    @Inject @DataField
    TextArea mangedEndpoint;

    ServiceVersionEndpointSummaryBean endpointBean;
    
    /**
     * Constructor.
     */
    public ServiceEndpointPage() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        // Will load our data once the service version is loaded.
        return rval + 1;
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#onServiceVersionLoaded()
     */
    @Override
    protected void onServiceVersionLoaded() {
        rest.getServiceVersionEndpointInfo(org, service, versionBean.getVersion(), new IRestInvokerCallback<ServiceVersionEndpointSummaryBean>() {
            @Override
            public void onSuccess(ServiceVersionEndpointSummaryBean response) {
                endpointBean = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        this.mangedEndpoint.setValue(endpointBean.getManagedEndpoint());
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SERVICE_ENDPOINT, serviceBean.getName());
    }

}
