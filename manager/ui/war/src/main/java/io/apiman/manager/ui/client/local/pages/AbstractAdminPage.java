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
import io.apiman.manager.ui.client.local.pages.common.Breadcrumb;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;

import com.google.gwt.user.client.ui.Anchor;


/**
 * Base class for all Organization pages.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractAdminPage extends AbstractPage {
    
    @Inject @DataField
    Breadcrumb breadcrumb;
    
    @Inject @DataField
    Anchor toRoles;
    @Inject @DataField
    Anchor toPolicyDefs;
    @Inject @DataField
    Anchor toGateways;

    /**
     * Constructor.
     */
    public AbstractAdminPage() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#isAuthorized()
     */
    @Override
    protected boolean isAuthorized() {
        return isAdmin();
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String rolesHref = navHelper.createHrefToPage(AdminRolesPage.class, MultimapUtil.emptyMap());
        String policyDefsHref = navHelper.createHrefToPage(AdminPolicyDefsPage.class, MultimapUtil.emptyMap());
        String gatewaysHref = navHelper.createHrefToPage(AdminGatewaysPage.class, MultimapUtil.emptyMap());
        toRoles.setHref(rolesHref);
        toPolicyDefs.setHref(policyDefsHref);
        toGateways.setHref(gatewaysHref);

        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.emptyMap());
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addActiveItem("gavel", i18n.format(AppMessages.SYSTEM_ADMINISTRATION)); //$NON-NLS-1$
    }

}
