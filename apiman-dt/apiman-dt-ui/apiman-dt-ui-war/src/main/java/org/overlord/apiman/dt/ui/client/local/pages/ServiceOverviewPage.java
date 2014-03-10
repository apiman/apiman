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
 * The "Service" page, with the Overview tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/service-overview.html#page")
@Page(path="service-overview")
@Dependent
public class ServiceOverviewPage extends AbstractServicePage {
    
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

    @Inject @DataField
    Anchor ttd_toNewContract;
    @Inject @DataField
    Anchor ttd_toNewServiceVersion;

    
    /**
     * Constructor.
     */
    public ServiceOverviewPage() {
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
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractServicePage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        
        String newContractHref = navHelper.createHrefToPage(NewContractPage.class, MultimapUtil.fromMultiple("org", org, "svc", service, "svcv", versionBean.getVersion())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ttd_toNewContract.setHref(newContractHref);
        String newServiceVersionHref = navHelper.createHrefToPage(NewServiceVersionPage.class, MultimapUtil.fromMultiple("org", org, "service", service)); //$NON-NLS-1$ //$NON-NLS-2$
        ttd_toNewServiceVersion.setHref(newServiceVersionHref);
        
        description.setText(serviceBean.getDescription());
        createdOn.setText(Formatting.formatShortDate(serviceBean.getCreatedOn()));
        createdBy.setText(serviceBean.getCreatedBy());
        String toUserHref = navHelper.createHrefToPage(UserServicesPage.class,
                MultimapUtil.fromMultiple("user", serviceBean.getCreatedBy())); //$NON-NLS-1$
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
        return i18n.format(AppMessages.TITLE_SERVICE_OVERVIEW, serviceBean.getName());
    }

}
