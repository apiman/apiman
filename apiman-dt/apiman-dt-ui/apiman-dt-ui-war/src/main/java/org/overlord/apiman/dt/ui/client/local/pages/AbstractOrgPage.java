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

import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.ui.client.local.services.ContextKeys;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.ParagraphLabel;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;


/**
 * Base class for all Organization pages.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractOrgPage extends AbstractPage {
    
    @PageState
    protected String org;
    
    OrganizationBean organizationBean;

    @Inject @DataField
    Label name;
    @Inject @DataField
    InlineLabel createdOn;
    @Inject @DataField
    InlineLabel numMembers;
    @Inject @DataField
    ParagraphLabel description;
    
    @Inject @DataField
    Anchor toOrgApps;
    @Inject @DataField
    Anchor toOrgServices;
    @Inject @DataField
    Anchor toOrgPlans;
    @Inject @DataField
    Anchor toOrgMembers;
    @Inject @DataField
    Anchor toOrgActivity;

    /**
     * Constructor.
     */
    public AbstractOrgPage() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        rest.getOrganization(org, new IRestInvokerCallback<OrganizationBean>() {
            @Override
            public void onSuccess(OrganizationBean response) {
                organizationBean = response;
                currentContext.setAttribute(ContextKeys.CURRENT_ORGANIZATION, organizationBean);
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return 1;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String orgAppsHref = navHelper.createHrefToPage(OrgAppsPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String orgServicesHref = navHelper.createHrefToPage(OrgServicesPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String orgPlansHref = navHelper.createHrefToPage(OrgPlansPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String orgMembersHref = navHelper.createHrefToPage(OrgMembersPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        String orgActivityHref = navHelper.createHrefToPage(OrgActivityPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        toOrgApps.setHref(orgAppsHref);
        toOrgServices.setHref(orgServicesHref);
        toOrgPlans.setHref(orgPlansHref);
        toOrgMembers.setHref(orgMembersHref);
        toOrgActivity.setHref(orgActivityHref);

        name.setText(organizationBean.getName());
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
        createdOn.setText(format.format(organizationBean.getCreatedOn()));
        // TODO implement # members
        numMembers.setText("1"); //$NON-NLS-1$
        if (organizationBean.getDescription() != null) {
            description.setText(organizationBean.getDescription());
        } else {
            description.setText(""); //$NON-NLS-1$
        }
    }

}
