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
package io.apiman.manager.ui.client.local.pages;

import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.services.ConfigurationService;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;

/**
 * The default page.  This is the page that users are shown when 
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/dash.html#page")
@Page(path="h", role=DefaultPage.class)
@Dependent
public class DashboardPage extends AbstractPage {
    
    @Inject
    ConfigurationService config;

    @Inject @DataField
    Anchor createOrg;
    @Inject @DataField
    Anchor browseOrgs;
    @Inject @DataField
    Anchor myOrgs;

    @Inject @DataField
    Anchor createService;
    @Inject @DataField
    Anchor browseServices;
    @Inject @DataField
    Anchor myServices;

    @Inject @DataField
    Anchor createApp;
    @Inject @DataField
    Anchor myApps;
    
    @Inject @DataField
    Anchor manageRoles;
    @Inject @DataField
    Anchor managePolicyDefs;

    /**
     * Constructor.
     */
    public DashboardPage() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractAppPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        
        if (!getCurrentUserBean().isAdmin()) {
            hideAdminGroup();
        }
        
        String currentUser = config.getCurrentConfig().getUser().getUsername();

        String createOrgHref = navHelper.createHrefToPage(NewOrgPage.class, MultimapUtil.emptyMap());
        String browseOrgsHref = navHelper.createHrefToPage(ConsumerOrgsPage.class, MultimapUtil.emptyMap());
        String myOrgsHref = navHelper.createHrefToPage(UserOrgsPage.class, MultimapUtil.singleItemMap("user", currentUser)); //$NON-NLS-1$
        String createServiceHref = navHelper.createHrefToPage(NewServicePage.class, MultimapUtil.emptyMap());
        String browseServicesHref = navHelper.createHrefToPage(ConsumerServicesPage.class, MultimapUtil.emptyMap());
        String myServicesHref = navHelper.createHrefToPage(UserServicesPage.class, MultimapUtil.singleItemMap("user", currentUser)); //$NON-NLS-1$
        String createAppHref = navHelper.createHrefToPage(NewAppPage.class, MultimapUtil.emptyMap());
        String myAppsHref = navHelper.createHrefToPage(UserAppsPage.class, MultimapUtil.singleItemMap("user", currentUser)); //$NON-NLS-1$
        String manageRolesHref = navHelper.createHrefToPage(AdminRolesPage.class, MultimapUtil.emptyMap());
        String managePolicyDefsHref = navHelper.createHrefToPage(AdminPolicyDefsPage.class, MultimapUtil.emptyMap());
        
        createOrg.setHref(createOrgHref);
        browseOrgs.setHref(browseOrgsHref);
        myOrgs.setHref(myOrgsHref);

        createService.setHref(createServiceHref);
        browseServices.setHref(browseServicesHref);
        myServices.setHref(myServicesHref);

        createApp.setHref(createAppHref);
        myApps.setHref(myAppsHref);
        
        manageRoles.setHref(manageRolesHref);
        managePolicyDefs.setHref(managePolicyDefsHref);
    }

    /**
     * Hides the admin group div.
     */
    private native void hideAdminGroup() /*-{
        $wnd.jQuery('#dash-admin-group').hide();
    }-*/;

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_DASHBOARD);
    }

}
