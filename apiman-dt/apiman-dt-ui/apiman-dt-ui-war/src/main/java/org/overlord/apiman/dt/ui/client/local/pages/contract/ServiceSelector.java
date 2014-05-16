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
package org.overlord.apiman.dt.ui.client.local.pages.contract;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.overlord.apiman.dt.api.beans.services.ServiceBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Shows the user a list of services and allows her to select one.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ServiceSelector extends FlowPanel implements HasValue<ServiceBean> {
    
    private ServiceBean value;
    private Anchor selectedRow;
    private boolean enabled = true;
    private List<Anchor> rows = new ArrayList<Anchor>();
    private List<ServiceBean> services;
    
    @Inject TranslationService i18n;
    
    /**
     * Constructor.
     */
    public ServiceSelector() {
    }
    
    /**
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            getElement().removeClassName("disabled"); //$NON-NLS-1$
        else
            getElement().addClassName("disabled"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ServiceBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Called to display a list of users to choose from.
     * @param services
     */
    public void setServices(List<ServiceBean> services) {
        clear();
        this.services = services;
        if (services.isEmpty()) {
            add(new Label(i18n.format(AppMessages.SERVICE_SELECTOR_NONE_FOUND)));
        } else {
            for (ServiceBean userBean : services) {
                Widget row = createServiceRow(userBean);
                add(row);
            }
        }
    }

    /**
     * Creates a row in the output table for each user.
     * 
     * <pre>
     *      &lt;a href="#" class="item">
     *        &lt;i class="fa fa-puzzle-piece fa-fw">&lt;/i>
     *        &lt;span class="">Overlord&lt;/span>
     *        &lt;span> / &lt;/span>
     *        &lt;span class="emphasis">dtgov-deploy&lt;/span>
     *      &lt;/a>
     * </pre>
     * 
     * @param serviceBean
     */
    private Widget createServiceRow(final ServiceBean serviceBean) {
        final Anchor a = new Anchor();
        a.getElement().setClassName("item"); //$NON-NLS-1$
        StringBuilder builder = new StringBuilder();
        builder.append("<i class=\"fa fa-puzzle-piece fa-fw\"></i> "); //$NON-NLS-1$
        builder.append("<span>" + serviceBean.getOrganizationId() + "</span> "); //$NON-NLS-1$ //$NON-NLS-2$
        builder.append("<span> / </span> "); //$NON-NLS-1$
        builder.append("<span class=\"emphasis\">").append(serviceBean.getName()).append("</span> "); //$NON-NLS-1$ //$NON-NLS-2$
        a.setHTML(builder.toString());
        a.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (enabled) {
                    setValue(serviceBean, true);
                }
            }
        });
        rows.add(a);
        return a;
    }


    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public ServiceBean getValue() {
        return value;
    }
    
    /**
     * @see com.google.gwt.user.client.ui.FlowPanel#clear()
     */
    @Override
    public void clear() {
        super.clear();
        this.value = null;
        selectedRow = null;
        this.rows.clear();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(ServiceBean value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(ServiceBean value, boolean fireEvents) {
        this.value = value;
        selectRow(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * Selects the row associated with the given value.
     * @param value
     */
    private void selectRow(ServiceBean value) {
        int idx = 0;
        for (ServiceBean serviceBean : this.services) {
            if (serviceBean == value) {
                break;
            }
            idx++;
        }
        if (idx < rows.size()) {
            Anchor row = rows.get(idx);
            selectRow(row);
        } else {
            if (selectedRow != null) {
                selectedRow.getElement().removeClassName("selected"); //$NON-NLS-1$
                selectedRow = null;
            }
        }
    }

    /**
     * Called when the user clicks on a service/row in the list.
     * @param row
     */
    protected void selectRow(Anchor row) {
        if (row == selectedRow)
            return;
        if (selectedRow != null) {
            selectedRow.getElement().removeClassName("selected"); //$NON-NLS-1$
            selectedRow = null;
        }
        row.getElement().addClassName("selected"); //$NON-NLS-1$
        selectedRow = row;
    }
}
