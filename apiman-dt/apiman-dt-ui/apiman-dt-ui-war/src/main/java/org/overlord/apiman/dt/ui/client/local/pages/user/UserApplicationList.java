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
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.AppRedirectPage;
import org.overlord.apiman.dt.ui.client.local.pages.OrgRedirectPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.AbstractApplicationList;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
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
    
    @Inject
    TransitionAnchorFactory<OrgRedirectPage> toOrgFactory;
    @Inject
    TransitionAnchorFactory<AppRedirectPage> toAppFactory;

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
        Anchor a = toOrgFactory.get(MultimapUtil.singleItemMap("org", bean.getOrganizationId())); //$NON-NLS-1$
        row1.add(a);
        a.setText(bean.getOrganizationName());
        row1.add(new InlineLabel(" / ")); //$NON-NLS-1$
        SpanPanel sp = new SpanPanel();
        row1.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        a = toAppFactory.get(MultimapUtil.fromMultiple("org", bean.getOrganizationId(), "app", bean.getId())); //$NON-NLS-1$ //$NON-NLS-2$
        sp.add(a);
        a.setText(bean.getName());
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.AbstractApplicationList#createNoEntitiesWidget()
     */
    @Override
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered())
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_APPS_FOR_USER_MESSAGE), true);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_APPS_FOR_USER_MESSAGE), true);
    }

}
