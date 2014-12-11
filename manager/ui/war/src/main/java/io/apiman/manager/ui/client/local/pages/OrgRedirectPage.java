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

import io.apiman.manager.api.beans.idm.CurrentUserBean;
import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.ui.client.local.services.RestInvokerService;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;

/**
 * The default org page.  This page is responsible for simply
 * redirecting to the proper specific org page.
 *
 * @author eric.wittmann@redhat.com
 */
@Page(path="org")
@Dependent
public class OrgRedirectPage extends AbstractRedirectPage {

    @Inject
    protected RestInvokerService rest;

    @PageState
    protected String org;

    /**
     * Constructor.
     */
    public OrgRedirectPage() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractRedirectPage#doRedirect()
     */
    @Override
    protected void doRedirect() {
        CurrentUserBean currentUserBean = AbstractPage.currentUserBean;
        if (currentUserBean != null) {
            redirectFor(currentUserBean);
        } else {
            rest.getCurrentUserInfo(new IRestInvokerCallback<CurrentUserBean>() {
                @Override
                public void onSuccess(CurrentUserBean response) {
                    redirectFor(response);
                }
                
                @Override
                public void onError(Throwable error) {
                    nav.goTo(DashboardPage.class, MultimapUtil.emptyMap());
                }
            });
        }
    }

    /**
     * @param user
     */
    protected void redirectFor(CurrentUserBean user) {
        if (hasPermission(PermissionType.appView, user)) {
            nav.goTo(OrgAppsPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        } else if (hasPermission(PermissionType.svcView, user)) {
            nav.goTo(OrgServicesPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        } else if (hasPermission(PermissionType.planView, user)) {
            nav.goTo(OrgPlansPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        } else {
            nav.goTo(OrgMembersPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        }
    }

    /**
     * @param permission
     * @param user
     */
    private boolean hasPermission(PermissionType permission, CurrentUserBean user) {
        Set<PermissionBean> permissions = user.getPermissions();
        for (PermissionBean permissionBean : permissions) {
            if (permissionBean.getName() == permission && permissionBean.getOrganizationId().equals(org)) {
                return true;
            }
        }
        return false;
    }

}
