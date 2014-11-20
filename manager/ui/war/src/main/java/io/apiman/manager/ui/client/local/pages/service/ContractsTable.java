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
package io.apiman.manager.ui.client.local.pages.service;

import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.AppRedirectPage;
import io.apiman.manager.ui.client.local.pages.OrgAppsPage;
import io.apiman.manager.ui.client.local.pages.PlanRedirectPage;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;
import io.apiman.manager.ui.client.local.util.Formatting;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of service contracts
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ContractsTable extends TemplatedWidgetTable implements TakesValue<List<ContractSummaryBean>> {

    @Inject
    protected TranslationService i18n;
    
    @Inject
    protected TransitionAnchorFactory<OrgAppsPage> toOrg;
    @Inject
    protected TransitionAnchorFactory<AppRedirectPage> toApp;
    @Inject
    protected TransitionAnchorFactory<PlanRedirectPage> toPlan;
    

    private List<ContractSummaryBean> contracts;

    /**
     * Constructor.
     */
    public ContractsTable() {
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<ContractSummaryBean> value) {
        contracts = value;
        clear();
        refresh();
    }

    /**
     * @param contracts
     */
    public void append(List<ContractSummaryBean> contracts) {
        int rowIdx = getRowCount();
        for (ContractSummaryBean bean : contracts) {
            addRow(rowIdx++, bean);
        }
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (contracts != null && !contracts.isEmpty()) {
            int rowIdx = 0;
            for (ContractSummaryBean bean : contracts) {
                addRow(rowIdx++, bean);
            }
        } else {
            Element tdElement = add(0, 0, createNoEntitiesWidget());
            tdElement.setAttribute("colspan", "5"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Adds a row to the table.
     * @param rowIdx
     * @param bean
     */
    private void addRow(int rowIdx, ContractSummaryBean bean) {
        TransitionAnchor<OrgAppsPage> orgLink = toOrg.get(MultimapUtil.singleItemMap("org", bean.getAppOrganizationId())); //$NON-NLS-1$
        orgLink.setText(bean.getAppOrganizationName());
        add(rowIdx, 0, orgLink);
        
        TransitionAnchor<AppRedirectPage> appLink = toApp.get(MultimapUtil.fromMultiple("org", bean.getAppOrganizationId(), "app", bean.getAppId())); //$NON-NLS-1$ //$NON-NLS-2$
        appLink.setText(bean.getAppName());
        add(rowIdx, 1, appLink);
        
        TransitionAnchor<AppRedirectPage> appVersionLink = toApp.get(MultimapUtil.fromMultiple("org", bean.getAppOrganizationId(), "app", bean.getAppId(), "version", bean.getAppVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        appVersionLink.setText(bean.getAppVersion());
        add(rowIdx, 2, appVersionLink);
        
        TransitionAnchor<PlanRedirectPage> planLink = toPlan.get(MultimapUtil.fromMultiple("org", bean.getServiceOrganizationId(), "plan", bean.getPlanId(), "version", bean.getPlanVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        planLink.setText(bean.getPlanName());
        add(rowIdx, 3, planLink);

        InlineLabel when = new InlineLabel(Formatting.formatShortDate(bean.getCreatedOn()));
        add(rowIdx, 4, when);
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_CONTRACTS_FOR_SERVICE), false);
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public List<ContractSummaryBean> getValue() {
        return contracts;
    }

}
