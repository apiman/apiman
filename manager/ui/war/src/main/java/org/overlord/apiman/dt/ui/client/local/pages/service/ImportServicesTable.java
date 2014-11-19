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
package org.overlord.apiman.dt.ui.client.local.pages.service;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A table of services to import.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ImportServicesTable extends TemplatedWidgetTable implements HasValue<List<ServiceVersionBean>> {

    @Inject
    protected TranslationService i18n;

    private List<ServiceVersionBean> services;
    private List<ServiceVersionBean> selectedServices = new ArrayList<ServiceVersionBean>();
    private List<CheckBox> checkboxes = new ArrayList<CheckBox>();
    private List<TextBox> names = new ArrayList<TextBox>();
    private List<TextBox> versions = new ArrayList<TextBox>();

    /**
     * Constructor.
     */
    public ImportServicesTable() {
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<ServiceVersionBean> value) {
        setValue(value, false);
    }
    
    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<ServiceVersionBean> value, boolean fireEvents) {
        services = value;
        selectedServices.clear();
        checkboxes.clear();
        names.clear();
        versions.clear();
        selectedServices.addAll(value);
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (services != null && !services.isEmpty()) {
            int rowIdx = 0;
            for (ServiceVersionBean bean : services) {
                addRow(rowIdx++, bean);
            }
        } else {
            Element tdElement = add(0, 0, createNoEntitiesWidget());
            tdElement.setAttribute("colspan", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Adds a row to the table.
     * @param rowIdx
     * @param bean
     */
    private void addRow(int rowIdx, final ServiceVersionBean bean) {
        CheckBox checkbox = new CheckBox();
        checkbox.setValue(Boolean.TRUE);
        checkboxes.add(checkbox);
        add(rowIdx, 0, checkbox);
        checkbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean selected = event.getValue();
                if (selected) {
                    selectedServices.add(bean);
                } else {
                    selectedServices.remove(bean);
                }
                fireChangeEvent();
            }
        });
        
        final TextBox serviceName = new TextBox();
        serviceName.getElement().getStyle().setWidth(200, Unit.PX);
        serviceName.setValue(bean.getService().getName());
        names.add(serviceName);
        add(rowIdx, 1, serviceName);
        serviceName.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                bean.getService().setName(serviceName.getValue());
                fireChangeEvent();
            }
        });

        final TextBox initialVersion = new TextBox();
        initialVersion.getElement().getStyle().setWidth(75, Unit.PX);
        initialVersion.setValue(bean.getVersion());
        versions.add(initialVersion);
        add(rowIdx, 2, initialVersion);
        initialVersion.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                bean.setVersion(initialVersion.getValue());
                fireChangeEvent();
            }
        });
        
        InlineLabel endpoint = new InlineLabel(bean.getEndpoint());
        add(rowIdx, 3, endpoint);
    }

    /**
     * Fires a value change event when something changes.
     */
    protected void fireChangeEvent() {
        ValueChangeEvent.fire(this, selectedServices);
    }
    
    /**
     * @return true if the data in the table is all valid
     */
    public boolean isValid() {
        if (selectedServices.isEmpty()) {
            return false;
        }
        for (TextBox name : names) {
            if (name.getValue() == null || name.getValue().trim().isEmpty()) {
                return false;
            }
        }
        for (TextBox version : versions) {
            if (version.getValue() == null || version.getValue().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @return true only if all the services are selected
     */
    public boolean isAllSelected() {
        return services.size() == selectedServices.size();
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_SERVICES_IMPORT_MESSAGE), false);
    }
    
    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<ServiceVersionBean>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public List<ServiceVersionBean> getValue() {
        return services;
    }

    /**
     * Selects all rows.
     */
    public void selectAll() {
        selectedServices.clear();
        selectedServices.addAll(services);
        for (CheckBox checkBox : checkboxes) {
            checkBox.setValue(true);
        }
        fireChangeEvent();
    }

    /**
     * Deselects all rows.
     */
    public void deselectAll() {
        selectedServices.clear();
        for (CheckBox checkBox : checkboxes) {
            checkBox.setValue(false);
        }
        fireChangeEvent();
    }
}
