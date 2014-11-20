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

import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.service.ContractsTable;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;


/**
 * The "Service" page, with the Contracts tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/service-contracts.html#page")
@Page(path="service-contracts")
@Dependent
public class ServiceContractsPage extends AbstractServicePage {
    
    private static final int PAGE_SIZE = 5;
    
    @Inject @DataField
    ContractsTable contracts;
    @Inject @DataField
    AsyncActionButton moreButton;
    
    private int currentPage = 1;
    
    /**
     * Constructor.
     */
    public ServiceContractsPage() {
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
        rest.getServiceContracts(org, service, versionBean.getVersion(), 1, PAGE_SIZE, new IRestInvokerCallback<List<ContractSummaryBean>>() {
            @Override
            public void onSuccess(List<ContractSummaryBean> response) {
                if (response.size() >= PAGE_SIZE) {
                    moreButton.reset();
                    moreButton.setVisible(true);
                } else {
                    moreButton.setVisible(false);
                }
                contracts.setValue(response);
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
    }

    /**
     * Called when the user clicks the "Show More Contracts" button.
     * @param event
     */
    @EventHandler("moreButton")
    public void onMore(ClickEvent event) {
        moreButton.onActionStarted();
        currentPage++;
        rest.getServiceContracts(org, service, versionBean.getVersion(), currentPage, PAGE_SIZE, new IRestInvokerCallback<List<ContractSummaryBean>>() {
            @Override
            public void onSuccess(List<ContractSummaryBean> response) {
                moreButton.onActionComplete();
                if (response.size() >= PAGE_SIZE) {
                    moreButton.setVisible(true);
                } else {
                    moreButton.setVisible(false);
                }
                contracts.append(response);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SERVICE_CONTRACTS, serviceBean.getName());
    }

}
