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
package org.overlord.apiman.dt.ui.client.local.pages.consumer;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.events.CreateContractEvent;
import org.overlord.apiman.dt.ui.client.local.events.CreateContractEvent.Handler;
import org.overlord.apiman.dt.ui.client.local.events.CreateContractEvent.HasCreateContractHandlers;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * List of plans for a service.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ConsumerServicePlanList extends FlowPanel implements TakesValue<List<ServicePlanSummaryBean>>, HasCreateContractHandlers {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    
    private List<ServicePlanSummaryBean> plans;

    /**
     * Constructor.
     */
    public ConsumerServicePlanList() {
        getElement().setClassName("apiman-plans"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<ServicePlanSummaryBean> getValue() {
        return plans;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<ServicePlanSummaryBean> value) {
        plans = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (plans != null && !plans.isEmpty()) {
            for (ServicePlanSummaryBean bean : plans) {
                Widget row = createPlanRow(bean);
                add(row);
            }
        } else {
            add(createNoEntitiesWidget());
        }
    }

    /**
     * Creates a single service row.
     * @param bean
     */
    private Widget createPlanRow(ServicePlanSummaryBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row = new FlowPanel();
        container.add(row);
        row.getElement().setClassName("row"); //$NON-NLS-1$
        createTitleAndDescription(bean, row);
        createActions(bean, row);
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
    }

    /**
     * Creates the title row.
     * @param bean
     * @param row
     */
    protected void createTitleAndDescription(ServicePlanSummaryBean bean, FlowPanel row) {
        FlowPanel div = new FlowPanel();
        row.add(div);
        div.getElement().setClassName("col-md-10"); //$NON-NLS-1$
        div.getElement().addClassName("col-no-padding"); //$NON-NLS-1$
        
        FontAwesomeIcon icon = new FontAwesomeIcon("bar-chart-o", true); //$NON-NLS-1$
        div.add(icon);
        icon.getElement().addClassName("icon"); //$NON-NLS-1$

        SpanPanel sp = new SpanPanel();
        div.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        
        Anchor a = new Anchor();
        a.setHref("#"); //$NON-NLS-1$
        a.setText(bean.getPlanName());
        sp.add(a);
        
        Label description = new Label(bean.getPlanDescription());
        div.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
    }
    
    /**
     * Creates the description row for a single service in the list.
     * @param bean
     * @param row
     */
    protected void createActions(final ServicePlanSummaryBean bean, FlowPanel row) {
        FlowPanel div = new FlowPanel();
        row.add(div);
        div.getElement().setClassName("col-md-2"); //$NON-NLS-1$
        div.getElement().addClassName("col-no-padding"); //$NON-NLS-1$

        SpanPanel sp = new SpanPanel();
        div.add(sp);
        sp.getElement().setClassName("actions"); //$NON-NLS-1$
        
        Button createContract = new Button(i18n.format(AppMessages.CREATE_CONTRACT));
        sp.add(createContract);
        createContract.getElement().setClassName("btn"); //$NON-NLS-1$
        createContract.getElement().addClassName("btn-default"); //$NON-NLS-1$
        createContract.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CreateContractEvent.fire(ConsumerServicePlanList.this, bean);
            }
        });
    }

    /**
     * @return a widget to display when no plans exist
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_PLANS_IN_CONSUMER_SERVICE_MESSAGE), false);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.CreateContractEvent.HasCreateContractHandlers#addCreateContractHandler(org.overlord.apiman.dt.ui.client.local.events.CreateContractEvent.Handler)
     */
    @Override
    public HandlerRegistration addCreateContractHandler(Handler handler) {
        return addHandler(handler, CreateContractEvent.getType());
    }
}
