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
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.events.BreakContractEvent;
import org.overlord.apiman.dt.ui.client.local.pages.app.AppContractList;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The "Application" page, with the Contracts tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/app-contracts.html#page")
@Page(path="app-contracts")
@Dependent
public class AppContractsPage extends AbstractAppPage {

    private List<ContractSummaryBean> contractBeans;

    @Inject @DataField
    Anchor toNewContract;

    @Inject @DataField
    TextBox contractFilter;
    @Inject @DataField
    AppContractList contracts;

    /**
     * Constructor.
     */
    public AppContractsPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        contractFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                filterContracts();
            }
        });
        contracts.addBreakContractHandler(new BreakContractEvent.Handler() {
            @Override
            public void onBreakContract(BreakContractEvent event) {
                doBreakContract(event.getContract());
            }
        });
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        // we'll trigger an additional load after the app version has been loaded (hence the +1)
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
        String orgId = org;
        String appId = app;
        String appVersion = versionBean.getVersion();
        String newContractHref = navHelper.createHrefToPage(NewContractPage.class,
                MultimapUtil.fromMultiple("apporg", orgId, "app", appId, "appv", appVersion)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        toNewContract.setHref(newContractHref);
        contracts.setValue(contractBeans);
    }

    /**
     * Apply a filter to the list of contracts.
     */
    protected void filterContracts() {
        if (contractFilter.getValue().trim().length() == 0) {
            contracts.setValue(contractBeans);
        } else {
            List<ContractSummaryBean> filtered = new ArrayList<ContractSummaryBean>();
            for (ContractSummaryBean contract : contractBeans) {
                if (matchesFilter(contract)) {
                    filtered.add(contract);
                }
            }
            contracts.setFilteredValue(filtered);
        }
    }

    /**
     * Returns true if the given contract matches the current filter.
     * @param contract
     */
    private boolean matchesFilter(ContractSummaryBean contract) {
        if (contractFilter.getValue() == null || contractFilter.getValue().trim().length() == 0)
            return true;
        if (contract.getServiceOrganizationName().toUpperCase().contains(contractFilter.getValue().toUpperCase()))
            return true;
        if (contract.getServiceName().toUpperCase().contains(contractFilter.getValue().toUpperCase()))
            return true;
        return false;
    }

    /**
     * Called when the user chooses to break a contract.
     * @param contract
     */
    protected void doBreakContract(final ContractSummaryBean contract) {
        rest.deleteContract(org, versionBean.getApplication().getId(), versionBean.getVersion(),
                contract.getContractId(), new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                contractBeans.remove(contract);
                filterContracts();
            }
            @Override
            public void onError(Throwable error) {
                Window.alert(i18n.format(AppMessages.BREAK_CONTRACT_FAILURE));
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_APP_CONTRACTS, applicationBean.getName());
    }

}
