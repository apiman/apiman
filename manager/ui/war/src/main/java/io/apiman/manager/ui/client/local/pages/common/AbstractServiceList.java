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
package io.apiman.manager.ui.client.local.pages.common;

import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.ui.client.local.services.NavigationHelperService;

import java.util.List;

import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * UI component to display the user's list of services.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractServiceList extends FlowPanel implements HasValue<List<ServiceSummaryBean>> {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    
    private List<ServiceSummaryBean> services;
    private boolean filtered;

    /**
     * Constructor.
     */
    public AbstractServiceList() {
        getElement().setClassName("apiman-services"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<ServiceSummaryBean>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<ServiceSummaryBean> getValue() {
        return services;
    }
    
    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<ServiceSummaryBean> value) {
        filtered = true;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<ServiceSummaryBean> value) {
        filtered = false;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<ServiceSummaryBean> value, boolean fireEvents) {
        services = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (services != null && !services.isEmpty()) {
            for (ServiceSummaryBean bean : services) {
                Widget row = createServiceRow(bean);
                add(row);
            }
        } else {
            add(createNoEntitiesWidget());
        }
    }

    /**
     * @return a no-entities widget to be shown when no aps are found
     */
    protected abstract NoEntitiesWidget createNoEntitiesWidget();

    /**
     * Creates a single service row.
     * @param bean
     */
    private Widget createServiceRow(ServiceSummaryBean bean) {
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
     * Creates the title row for a single service in the list.
     * @param bean
     * @param row
     */
    protected abstract void createTitleRow(ServiceSummaryBean bean, FlowPanel row);

    /**
     * Creates the description row for a single service in the list.
     * @param bean
     * @param row
     */
    protected void createDescriptionRow(ServiceSummaryBean bean, FlowPanel row) {
        InlineLabel description = new InlineLabel(bean.getDescription());
        row.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }
}
