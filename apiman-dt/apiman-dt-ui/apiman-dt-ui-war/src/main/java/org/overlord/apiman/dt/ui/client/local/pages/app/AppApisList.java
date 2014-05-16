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
import org.overlord.apiman.dt.ui.client.local.pages.ServiceOverviewPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.apiman.dt.ui.client.local.services.ConfigurationService;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a list of APIs on the Application / APIs page/tab.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class AppApisList extends FlowPanel implements HasValue<List<ContractSummaryBean>> {
    
    @Inject
    protected ConfigurationService config;
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    
    private List<ContractSummaryBean> contracts;
    
    /**
     * Constructor.
     */
    public AppApisList() {
        getElement().setClassName("apiman-apis"); //$NON-NLS-1$
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
    @Override
    public void setValue(List<ContractSummaryBean> value) {
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
                Widget row = createApiRow(bean);
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
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_APIS_FOR_APP_MESSAGE), true);
    }

    /**
     * Creates a single API row.
     * @param bean
     */
    private Widget createApiRow(ContractSummaryBean bean) {
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
        createDetailRow(bean, row2);
        
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
        sp.getElement().setClassName("apikey"); //$NON-NLS-1$
        sp.getElement().addClassName("pull-right"); //$NON-NLS-1$
        
        InlineLabel keyLabel = new InlineLabel(i18n.format(AppMessages.KEY) + ": "); //$NON-NLS-1$
        keyLabel.setStyleName("emphasis"); //$NON-NLS-1$
        sp.add(keyLabel);
        InlineLabel key = new InlineLabel(bean.getKey());
        key.setStyleName(""); //$NON-NLS-1$
        sp.add(key);
    }
    
    /**
     * Creates the detail row for a single API in the list.
     * @param bean
     * @param row
     */
    protected void createDetailRow(final ContractSummaryBean bean, FlowPanel row) {
        FlowPanel url = new FlowPanel();
        row.add(url);
        url.setStyleName("url"); //$NON-NLS-1$
        
        final String apiUrl = generateBaseApiUrl(bean);
        
        // The API Url
        Anchor a = new Anchor(apiUrl);
        url.add(a);
        a.setStyleName(""); //$NON-NLS-1$
        
        // The interactive portion - adding extra path info and copying to clipboard
        SpanPanel sp = new SpanPanel();
        sp.setStyleName("extra-path"); //$NON-NLS-1$
        url.add(sp);
        
        InlineLabel slash = new InlineLabel(" / "); //$NON-NLS-1$
        sp.add(slash);
        final TextBox pathInput = new TextBox();
        sp.add(pathInput);
        pathInput.setStyleName("form-input"); //$NON-NLS-1$
        pathInput.addStyleName("api-path"); //$NON-NLS-1$
        InlineLabel spacer = new InlineLabel(" "); //$NON-NLS-1$
        sp.add(spacer);
        spacer.setStyleName(""); //$NON-NLS-1$
        Button button = new Button();
        sp.add(button);
        button.setStyleName("btn"); //$NON-NLS-1$
        button.addStyleName("btn-default"); //$NON-NLS-1$
        button.addStyleName("btn-xs"); //$NON-NLS-1$
        button.addStyleName("btn-copy"); //$NON-NLS-1$
        button.setText(i18n.format(AppMessages.COPY));
        
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onApiUrlCopy(bean, pathInput);
            }
        });
    }

    /**
     * Called when the user clicks the "Copy" button on one of the roes in the
     * list of APIs.
     * @param bean
     * @param pathInput
     */
    protected void onApiUrlCopy(ContractSummaryBean bean, TextBox pathInput) {
        String extraPath = pathInput.getValue();
        if (extraPath == null) {
            extraPath = ""; //$NON-NLS-1$
        }
        if (extraPath.startsWith("/")) { //$NON-NLS-1$
            extraPath.substring(1);
        }
        String baseUrl = generateBaseApiUrl(bean);
        String fullUrl = baseUrl + "/" + extraPath; //$NON-NLS-1$
        if (fullUrl.contains("?")) { //$NON-NLS-1$
            fullUrl += "&apikey=" + bean.getKey(); //$NON-NLS-1$
        } else {
            fullUrl += "?apikey=" + bean.getKey(); //$NON-NLS-1$
        }
        Window.prompt(i18n.format(AppMessages.APIS_LIST_PLEASE_COPY), fullUrl);
    }

    /**
     * Generates the base URL to the gateway - this URL is what an application would
     * use to invoke a service (by contract).
     * @param bean
     */
    protected String generateBaseApiUrl(ContractSummaryBean bean) {
        StringBuilder builder = new StringBuilder();
        String gatewayUrl = config.getCurrentConfig().getApiman().getGatewayBaseUrl();
        if (gatewayUrl == null) {
            gatewayUrl = "http://gateway-host:port/gateway/"; //$NON-NLS-1$
        }
        if (!gatewayUrl.endsWith("/")) { //$NON-NLS-1$
            gatewayUrl += "/"; //$NON-NLS-1$
        }
        builder.append(gatewayUrl);
        builder.append(bean.getServiceOrganizationId());
        builder.append("/"); //$NON-NLS-1$
        builder.append(bean.getServiceId());
        builder.append("/"); //$NON-NLS-1$
        builder.append(bean.getServiceVersion());
        return builder.toString();
    }

}
