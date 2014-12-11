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
import io.apiman.manager.ui.client.local.pages.ConsumerServicePage;
import io.apiman.manager.ui.client.local.pages.common.AbstractServiceList;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * List of services in an organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ConsumerOrgServiceList extends AbstractServiceList {
    
    @Inject
    private TransitionAnchorFactory<ConsumerServicePage> toServiceDetails;

    /**
     * Constructor.
     */
    public ConsumerOrgServiceList() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.AbstractServiceList#createTitleRow(io.apiman.manager.api.beans.summary.ServiceSummaryBean, com.google.gwt.user.client.ui.FlowPanel)
     */
    @Override
    protected void createTitleRow(ServiceSummaryBean bean, FlowPanel row) {
        FontAwesomeIcon icon = new FontAwesomeIcon("puzzle-piece", true); //$NON-NLS-1$
        row.add(icon);
        icon.getElement().addClassName("icon"); //$NON-NLS-1$

        SpanPanel sp = new SpanPanel();
        row.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        
        Anchor a = toServiceDetails.get(MultimapUtil.fromMultiple("org", bean.getOrganizationId(), "service", bean.getId())); //$NON-NLS-1$ //$NON-NLS-2$
        a.setText(bean.getName());
        sp.add(a);
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.common.AbstractServiceList#createNoEntitiesWidget()
     */
    @Override
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered())
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_SERVICES_IN_CONSUMER_ORG_MESSAGE), false);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_SERVICES_IN_CONSUMER_ORG_MESSAGE), false);
    }
}
