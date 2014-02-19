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
package org.overlord.apiman.dt.ui.client.local.pages.org;

import javax.enterprise.context.Dependent;

import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.ui.client.local.pages.AppOverviewPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.AbstractApplicationList;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * List of applications in an organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OrgApplicationList extends AbstractApplicationList {

    /**
     * Constructor.
     */
    public OrgApplicationList() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.AbstractApplicationList#createTitleRow(org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean, com.google.gwt.user.client.ui.FlowPanel)
     */
    @Override
    protected void createTitleRow(ApplicationSummaryBean bean, FlowPanel row1) {
        SpanPanel sp = new SpanPanel();
        row1.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        Anchor a = new Anchor(bean.getName());
        sp.add(a);
        a.setHref(navHelper.createHrefToPage(AppOverviewPage.class,
                MultimapUtil.fromMultiple("org", bean.getOrganizationId(), "app", bean.getId()))); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
