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

import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.ui.client.local.pages.AppOverviewPage;
import org.overlord.apiman.dt.ui.client.local.pages.OrgAppsPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.AbstractApplicationList;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * List of applications visible to the user.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class UserApplicationList extends AbstractApplicationList {

    /**
     * Constructor.
     */
    public UserApplicationList() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.AbstractApplicationList#createTitleRow(org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean, com.google.gwt.user.client.ui.FlowPanel)
     */
    @Override
    protected void createTitleRow(ApplicationSummaryBean bean, FlowPanel row1) {
        Anchor a = new Anchor(bean.getOrganizationName());
        row1.add(a);
        a.setHref(navHelper.createHrefToPage(OrgAppsPage.class, MultimapUtil.singleItemMap("org", bean.getOrganizationId()))); //$NON-NLS-1$
        row1.add(new InlineLabel(" / ")); //$NON-NLS-1$
        SpanPanel sp = new SpanPanel();
        row1.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        a = new Anchor(bean.getName());
        sp.add(a);
        a.setHref(navHelper.createHrefToPage(AppOverviewPage.class,
                MultimapUtil.fromMultiple("org", bean.getOrganizationId(), "app", bean.getId()))); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
