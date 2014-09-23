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
import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.Breadcrumb;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.Formatting;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;


/**
 * The "User" page, with the Organizations tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractUserPage extends AbstractPage {
    
    @PageState
    protected String user;
    
    UserBean userBean;

    @Inject @DataField
    Breadcrumb breadcrumb;
    
    @Inject @DataField
    Label fullName;
    @Inject @DataField
    Label userId;
    @Inject @DataField
    Anchor email;
    @Inject @DataField
    Label joinedOn;
    
    @Inject @DataField
    Anchor toUserOrgs;
    @Inject @DataField
    Anchor toUserApps;
    @Inject @DataField
    Anchor toUserServices;
    @Inject @DataField
    Anchor toUserActivity;

    /**
     * Constructor.
     */
    public AbstractUserPage() {
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getUser(user, new IRestInvokerCallback<UserBean>() {
            @Override
            public void onSuccess(UserBean response) {
                userBean = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 1;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        String userOrgsHref = navHelper.createHrefToPage(UserOrgsPage.class, MultimapUtil.singleItemMap("user", user)); //$NON-NLS-1$
        String userAppsHref = navHelper.createHrefToPage(UserAppsPage.class, MultimapUtil.singleItemMap("user", user)); //$NON-NLS-1$
        String userServicesHref = navHelper.createHrefToPage(UserServicesPage.class, MultimapUtil.singleItemMap("user", user)); //$NON-NLS-1$
        String userActivityHref = navHelper.createHrefToPage(UserActivityPage.class, MultimapUtil.singleItemMap("user", user)); //$NON-NLS-1$
        toUserOrgs.setHref(userOrgsHref);
        toUserApps.setHref(userAppsHref);
        toUserServices.setHref(userServicesHref);
        toUserActivity.setHref(userActivityHref);

        userId.setText(userBean.getUsername());
        if (userBean.getFullName() != null && userBean.getFullName().trim().length() > 0) {
            fullName.setText(userBean.getFullName());
        } else {
            fullName.setText(userBean.getUsername());
        }
        if (userBean.getEmail() != null && userBean.getEmail().trim().length() > 0) {
            email.setText(userBean.getEmail());
            email.setHref("mailto://" + userBean.getEmail()); //$NON-NLS-1$
        } else {
            email.setText("N/A"); //$NON-NLS-1$
        }
        if (userBean.getJoinedOn() != null) {
            joinedOn.setText(Formatting.formatShortDate(userBean.getJoinedOn()));
        } else {
            joinedOn.setText("N/A"); //$NON-NLS-1$
        }
        
        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.fromMultiple());
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addActiveItem("user", userBean.getFullName()); //$NON-NLS-1$
    }

}
