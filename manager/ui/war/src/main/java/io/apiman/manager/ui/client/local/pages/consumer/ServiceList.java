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
package io.apiman.manager.ui.client.local.pages.consumer;

import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.ConsumerOrgPage;
import io.apiman.manager.ui.client.local.pages.ConsumerServicePage;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;
import io.apiman.manager.ui.client.local.services.NavigationHelperService;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a list of services in the consumer UI.  This is shown to the user on
 * the "Browse Services" page - the page that lets the user search for 
 * services to join.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ServiceList extends FlowPanel implements TakesValue<List<ServiceSummaryBean>> {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    
    @Inject
    protected TransitionAnchorFactory<ConsumerOrgPage> toOrg;
    @Inject
    protected TransitionAnchorFactory<ConsumerServicePage> toServiceDetails;
    
    private List<ServiceSummaryBean> services;

    /**
     * Constructor.
     */
    public ServiceList() {
        getElement().setClassName("apiman-services"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<ServiceSummaryBean> getValue() {
        return services;
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<ServiceSummaryBean> value) {
        services = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (services != null && !services.isEmpty()) {
            Widget count = createCount();
            add(count);
            for (ServiceSummaryBean bean : services) {
                Widget row = createServiceRow(bean);
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
        return new NoEntitiesWidget(i18n.format(AppMessages.NO_SERVICES_MESSAGE), false);
    }

    /**
     * @return a widget to display the number of matching items
     */
    private Widget createCount() {
        String cmsg = i18n.format(AppMessages.SERVICE_COUNT, String.valueOf(services.size()));
        Label label = new Label(cmsg);
        label.getElement().setClassName("count"); //$NON-NLS-1$
        return label;
    }

    /**
     * Creates a single service row.
     * @param bean
     */
    private Widget createServiceRow(ServiceSummaryBean bean) {
        FlowPanel item = new FlowPanel();
        item.getElement().setClassName("item"); //$NON-NLS-1$
        
        createTitleRow(bean, item);
        createDescriptionRow(bean, item);
        
        return item;
    }

    /**
     * @param bean
     * @param item
     */
    private void createTitleRow(ServiceSummaryBean bean, FlowPanel item) {
        FlowPanel title = new FlowPanel();
        item.add(title);
        title.getElement().setClassName("title"); //$NON-NLS-1$
        
        FontAwesomeIcon icon = new FontAwesomeIcon("puzzle-piece"); //$NON-NLS-1$
        title.add(icon);
        icon.getElement().addClassName("icon"); //$NON-NLS-1$

        Anchor orgLink = toOrg.get(MultimapUtil.singleItemMap("org", bean.getOrganizationId())); //$NON-NLS-1$
        title.add(orgLink);
        orgLink.setText(bean.getOrganizationName());
        
        InlineLabel divider = new InlineLabel(" / "); //$NON-NLS-1$
        title.add(divider);

        Anchor serviceLink = toServiceDetails.get(MultimapUtil.fromMultiple("org", bean.getOrganizationId(), "service", bean.getId())); //$NON-NLS-1$ //$NON-NLS-2$
        title.add(serviceLink);
        serviceLink.setText(bean.getName());
        serviceLink.setStyleName("emphasis"); //$NON-NLS-1$
    }

    /**
     * @param bean
     * @param item
     */
    private void createDescriptionRow(ServiceSummaryBean bean, FlowPanel item) {
        Label description = new Label();
        item.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
        description.setTitle(bean.getDescription());
        String d = bean.getDescription();
        if (d != null && d.length() >= 70) {
            d = d.substring(0, 70) + "..."; //$NON-NLS-1$
        }
        description.setText(d);
    }

}
