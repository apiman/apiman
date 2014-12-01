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
package io.apiman.manager.ui.client.local.pages.app;

import io.apiman.manager.api.beans.summary.ApiEntryBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.ConsumerOrgPage;
import io.apiman.manager.ui.client.local.pages.ConsumerServicePage;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;
import io.apiman.manager.ui.client.local.util.MultimapUtil;
import io.apiman.manager.ui.client.local.widgets.ChevronToggleAnchor;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;
import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table widget that displays all of the APIs in the application's API registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class AppApiRegistryTable extends TemplatedWidgetTable implements TakesValue<ApiRegistryBean> {

    @Inject
    protected TranslationService i18n;
    @Inject
    protected TransitionAnchorFactory<ConsumerOrgPage> orgLinkFactory;
    @Inject
    protected TransitionAnchorFactory<ConsumerServicePage> svcLinkFactory;
    @Inject
    protected Instance<AppApiEntryDetails> entryDetailsFactory;

    private ApiRegistryBean apiRegistry;
    private boolean filtered;

    /**
     * Constructor.
     */
    public AppApiRegistryTable() {
    }
    
    /**
     * Filtered version of setValue().
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(ApiRegistryBean value) {
        filtered = true;
        apiRegistry = value;
        clear();
        refresh();
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(ApiRegistryBean value) {
        filtered = false;
        apiRegistry = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (apiRegistry != null && !apiRegistry.getApis().isEmpty()) {
            int rowIdx = 0;
            for (ApiEntryBean bean : apiRegistry.getApis()) {
                addRow(rowIdx, bean);
                rowIdx += 2;
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
    private void addRow(int rowIdx, ApiEntryBean bean) {
        final AppApiEntryDetails entryDetails = entryDetailsFactory.get();
        entryDetails.setValue(bean);
        
        ChevronToggleAnchor toggle = new ChevronToggleAnchor();
        toggle.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                boolean isToggleOpen = event.getValue();
                if (isToggleOpen) {
                    entryDetails.getElement().getParentElement().getParentElement().getStyle().clearDisplay();
                } else {
                    entryDetails.getElement().getParentElement().getParentElement().getStyle().setDisplay(Display.NONE);
                }
            }
        });
        add(rowIdx, 0, toggle);

        SpanPanel svcContainer = new SpanPanel();
        Anchor org = orgLinkFactory.get("org", bean.getServiceOrgId()); //$NON-NLS-1$
        org.setText(bean.getServiceOrgName());
        svcContainer.add(org);
        InlineLabel divider = new InlineLabel(" / "); //$NON-NLS-1$
        svcContainer.add(divider);
        Anchor a = svcLinkFactory.get(MultimapUtil.fromMultiple("org", bean.getServiceOrgId(), "service",  //$NON-NLS-1$ //$NON-NLS-2$
                bean.getServiceId(), "version", bean.getServiceVersion())); //$NON-NLS-1$
        a.setText(bean.getServiceName());
        svcContainer.add(a);
        add(rowIdx, 1, svcContainer);

        InlineLabel version = new InlineLabel();
        version.setText(bean.getServiceVersion());
        add(rowIdx, 2, version);
        
        InlineLabel plan = new InlineLabel(bean.getPlanName());
        add(rowIdx, 3, plan);
        
        add(rowIdx + 1, 0, entryDetails).setAttribute("colspan", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        // Hide the detail row initially
        entryDetails.getElement().getParentElement().getParentElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered()) {
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_APIS_FOR_APP_MESSAGE), false);
        } else {
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_APIS_FOR_APP_MESSAGE), false);
        }
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public ApiRegistryBean getValue() {
        return apiRegistry;
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }
}
