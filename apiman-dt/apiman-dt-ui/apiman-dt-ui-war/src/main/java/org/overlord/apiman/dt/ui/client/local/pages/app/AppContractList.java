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
package org.overlord.apiman.dt.ui.client.local.pages.app;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.OrgServicesPage;
import org.overlord.apiman.dt.ui.client.local.pages.PlanOverviewPage;
import org.overlord.apiman.dt.ui.client.local.pages.ServiceOverviewPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.apiman.dt.ui.client.local.util.Formatting;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a list of contracts on the Application / Contract page/tab.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class AppContractList extends FlowPanel implements HasValue<List<ContractSummaryBean>> {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    
    private List<ContractSummaryBean> contracts;
    private boolean filtered;
    
    /**
     * Constructor.
     */
    public AppContractList() {
        getElement().setClassName("apiman-contracts"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<ContractSummaryBean>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<ContractSummaryBean> getValue() {
        return contracts;
    }
    
    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<ContractSummaryBean> value) {
        filtered = true;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<ContractSummaryBean> value) {
        filtered = false;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<ContractSummaryBean> value, boolean fireEvents) {
        contracts = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (contracts != null && !contracts.isEmpty()) {
            for (ContractSummaryBean bean : contracts) {
                Widget row = createContractRow(bean);
                add(row);
            }
        } else {
            add(createNoEntitiesWidget());
        }
    }


    /**
     * @return a widget to show when there are no entities.
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered())
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_CONTRACTS_FOR_APP_MESSAGE), true);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_CONTRACTS_FOR_APP_MESSAGE), true);
    }

    /**
     * Creates a single contract row.
     * @param bean
     */
    private Widget createContractRow(ContractSummaryBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row1 = new FlowPanel();
        container.add(row1);
        row1.getElement().setClassName("row"); //$NON-NLS-1$
        createTitleRow(bean, row1);

        FlowPanel row2 = new FlowPanel();
        container.add(row2);
        row2.getElement().setClassName("row"); //$NON-NLS-1$
        createDescriptionRow(bean, row2);
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
    }

    /**
     * Creates the title row.
     * @param bean
     * @param row
     */
    protected void createTitleRow(ContractSummaryBean bean, FlowPanel row) {
        Anchor org = new Anchor(bean.getServiceOrganizationName());
        row.add(org);
        org.setHref(navHelper.createHrefToPage(OrgServicesPage.class, MultimapUtil.fromMultiple("org", bean.getServiceOrganizationId()))); //$NON-NLS-1$
        InlineLabel divider = new InlineLabel(" / "); //$NON-NLS-1$
        row.add(divider);
        SpanPanel sp = new SpanPanel();
        row.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        Anchor a = new Anchor(bean.getServiceName());
        sp.add(a);
        a.setHref(navHelper.createHrefToPage(ServiceOverviewPage.class,
                MultimapUtil.fromMultiple("org", bean.getServiceOrganizationId(), "service", bean.getServiceId()))); //$NON-NLS-1$ //$NON-NLS-2$
        sp = new SpanPanel();
        row.add(sp);
        sp.getElement().setClassName("actions"); //$NON-NLS-1$
        sp.getElement().addClassName("pull-right"); //$NON-NLS-1$
        // TODO need to handle what happens when the user actually clicks this link!
        a = new Anchor(i18n.format(AppMessages.BREAK_CONTRACT));
        sp.add(a);
    }
    
    /**
     * Creates the description row for a single contract in the list.
     * @param bean
     * @param row
     */
    protected void createDescriptionRow(ContractSummaryBean bean, FlowPanel row) {
        FlowPanel versionAndPlan = new FlowPanel();
        row.add(versionAndPlan);
        row.getElement().setClassName("versionAndPlan"); //$NON-NLS-1$
        row.add(new InlineLabel(i18n.format(AppMessages.SERVICE_VERSION) + " ")); //$NON-NLS-1$
        SpanPanel sp = new SpanPanel();
        row.add(sp);
        Anchor a = new Anchor(bean.getServiceVersion());
        sp.add(a);
        a.setHref(navHelper.createHrefToPage(ServiceOverviewPage.class,
                MultimapUtil.fromMultiple("org", bean.getServiceOrganizationId(), "service", bean.getServiceId(), "version", bean.getServiceVersion()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        row.add(new InlineLabel(" " + i18n.format(AppMessages.VIA_PLAN) + " ")); //$NON-NLS-1$ //$NON-NLS-2$
        sp = new SpanPanel();
        row.add(sp);
        a = new Anchor(bean.getPlanName());
        sp.add(a);
        a.setHref(navHelper.createHrefToPage(PlanOverviewPage.class,
                MultimapUtil.fromMultiple("org", bean.getServiceOrganizationId(), "plan", bean.getPlanId(), "version", bean.getPlanVersion()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        row.add(new InlineLabel(" " + i18n.format(AppMessages.ENTERED_INTO_ON) + " ")); //$NON-NLS-1$ //$NON-NLS-2$
        FontAwesomeIcon icon = new FontAwesomeIcon("clock-o", true); //$NON-NLS-1$
        row.add(icon);
        icon.getElement().addClassName("fa-inline"); //$NON-NLS-1$
        row.add(new InlineLabel(Formatting.formatShortDate(bean.getCreatedOn())));
        
        Label description = new Label(bean.getServiceDescription());
        row.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
        description.getElement().addClassName("apiman-label-faded"); //$NON-NLS-1$
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }

}
