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
package org.overlord.apiman.dt.ui.client.local.pages;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.util.Formatting;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;


/**
 * The "Plan" page, with the Overview tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/plan-overview.html#page")
@Page(path="plan-overview")
@Dependent
public class PlanOverviewPage extends AbstractPlanPage {
    
    @Inject @DataField
    Label description;
    @Inject @DataField
    InlineLabel createdOn;
    @Inject @DataField
    Anchor createdBy;
    
    @Inject @DataField
    InlineLabel version;
    @Inject @DataField
    InlineLabel versionCreatedOn;
    @Inject @DataField
    Anchor versionCreatedBy;

    /**
     * Constructor.
     */
    public PlanOverviewPage() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        return rval;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPlanPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        description.setText(planBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(planBean.getCreatedOn()));
        createdBy.setText(planBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", planBean.getCreatedBy())); //$NON-NLS-1$
        createdBy.setHref(toUserHref);

        version.setText(versionBean.getVersion());
        versionCreatedOn.setText(Formatting.formatShortDate(versionBean.getCreatedOn()));
        versionCreatedBy.setText(versionBean.getCreatedBy());
        toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", versionBean.getCreatedBy())); //$NON-NLS-1$
        versionCreatedBy.setHref(toUserHref);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_PLAN_OVERVIEW, planBean.getName());
    }

}
