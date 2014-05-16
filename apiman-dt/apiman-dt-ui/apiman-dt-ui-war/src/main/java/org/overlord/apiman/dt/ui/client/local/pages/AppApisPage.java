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
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.app.AppApisList;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;


/**
 * The "Application" page, with the APIs tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/app-apis.html#page")
@Page(path="app-apis")
@Dependent
public class AppApisPage extends AbstractAppPage {

    private List<ContractSummaryBean> contractBeans;

    @Inject @DataField
    AppApisList apis;

    /**
     * Constructor.
     */
    public AppApisPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        return rval + 1;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractAppPage#onAppVersionLoaded()
     */
    @Override
    protected void onAppVersionLoaded() {
        String orgId = org;
        String appId = app;
        String appVersion = versionBean.getVersion();
        rest.getApplicationContracts(orgId, appId, appVersion, new IRestInvokerCallback<List<ContractSummaryBean>>() {
            @Override
            public void onSuccess(List<ContractSummaryBean> response) {
                contractBeans = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        apis.setValue(contractBeans);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_APP_APIS, applicationBean.getName());
    }

}
