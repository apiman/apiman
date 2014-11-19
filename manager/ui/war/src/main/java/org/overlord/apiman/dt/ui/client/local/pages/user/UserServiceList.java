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
package org.overlord.apiman.dt.ui.client.local.pages.user;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.OrgRedirectPage;
import org.overlord.apiman.dt.ui.client.local.pages.OrgServicesPage;
import org.overlord.apiman.dt.ui.client.local.pages.ServiceOverviewPage;
import org.overlord.apiman.dt.ui.client.local.pages.ServiceRedirectPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.AbstractServiceList;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.apiman.dt.ui.client.local.util.Formatting;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.AnchorPanel;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * List of services visible to the user.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class UserServiceList extends AbstractServiceList {
    
    @Inject
    TransitionAnchorFactory<OrgRedirectPage> toOrgFactory;
    @Inject
    TransitionAnchorFactory<ServiceRedirectPage> toServiceFactory;

    /**
     * Constructor.
     */
    public UserServiceList() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.AbstractServiceList#createTitleRow(org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean, com.google.gwt.user.client.ui.FlowPanel)
     */
    @Override
    protected void createTitleRow(ServiceSummaryBean bean, FlowPanel row) {
        Anchor org = new Anchor(bean.getOrganizationName());
        row.add(org);
        org.setHref(navHelper.createHrefToPage(OrgServicesPage.class, MultimapUtil.fromMultiple("org", bean.getOrganizationId()))); //$NON-NLS-1$
        InlineLabel divider = new InlineLabel(" / "); //$NON-NLS-1$
        row.add(divider);
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
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.AbstractServiceList#createNoEntitiesWidget()
     */
    @Override
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered())
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_SERVICES_FOR_USER_MESSAGE), true);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_SERVICES_FOR_USER_MESSAGE), true);
    }

}
