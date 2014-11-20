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
package io.apiman.manager.ui.client.local.pages.org;

import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.ServiceOverviewPage;
import io.apiman.manager.ui.client.local.pages.common.AbstractServiceList;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;
import io.apiman.manager.ui.client.local.util.Formatting;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.enterprise.context.Dependent;

import org.overlord.commons.gwt.client.local.widgets.AnchorPanel;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * List of services in an organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OrgServiceList extends AbstractServiceList {

    /**
     * Constructor.
     */
    public OrgServiceList() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.AbstractServiceList#createTitleRow(io.apiman.manager.api.beans.summary.ServiceSummaryBean, com.google.gwt.user.client.ui.FlowPanel)
     */
    @Override
    protected void createTitleRow(ServiceSummaryBean bean, FlowPanel row) {
        SpanPanel sp = new SpanPanel();
        row.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        Anchor a = new Anchor(bean.getName());
        sp.add(a);
        a.setHref(navHelper.createHrefToPage(ServiceOverviewPage.class,
                MultimapUtil.fromMultiple("org", bean.getOrganizationId(), "service", bean.getId()))); //$NON-NLS-1$ //$NON-NLS-2$
        
        AnchorPanel ap = new AnchorPanel();
        row.add(ap);
        ap.getElement().setClassName("apiman-summaryrow-icon"); //$NON-NLS-1$
        FontAwesomeIcon icon = new FontAwesomeIcon("clock-o", true); //$NON-NLS-1$
        ap.add(icon);
        InlineLabel label = new InlineLabel(i18n.format(AppMessages.CREATED_ON));
        ap.add(label);
        label.getElement().setClassName("title-summary-item"); //$NON-NLS-1$
        label = new InlineLabel(Formatting.formatShortDate(bean.getCreatedOn()));
        ap.add(label);
        label.getElement().setClassName("title-summary-item"); //$NON-NLS-1$
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.common.AbstractServiceList#createNoEntitiesWidget()
     */
    @Override
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered())
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_SERVICES_IN_ORG_MESSAGE), true);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_SERVICES_IN_ORG_MESSAGE), true);
    }
}
