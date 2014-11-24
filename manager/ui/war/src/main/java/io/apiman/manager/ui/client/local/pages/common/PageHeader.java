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
package io.apiman.manager.ui.client.local.pages.common;

import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.ui.client.local.pages.ConsumerServicesPage;
import io.apiman.manager.ui.client.local.pages.DashboardPage;
import io.apiman.manager.ui.client.local.pages.SettingsProfilePage;
import io.apiman.manager.ui.client.local.pages.UserOrgsPage;
import io.apiman.manager.ui.client.local.services.ConfigurationService;
import io.apiman.manager.ui.client.local.services.NavigationHelperService;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Templated widget for the top navigation header.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/dash.html#header")
@ApplicationScoped
public class PageHeader extends Composite {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TransitionTo<ConsumerServicesPage> toServices;
    @Inject
    protected TransitionTo<DashboardPage> toDashboard;
    @Inject
    protected ConfigurationService config;

    @Inject @DataField
    Label logo;
    @Inject @DataField
    TextBox search;
    @Inject @DataField
    InlineLabel username;
    @Inject @DataField
    Anchor toUserHome;
    @Inject @DataField
    Anchor toProfile;
    @Inject @DataField
    Anchor toLogout;
    
    /**
     * Constructor.
     */
    public PageHeader() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        search.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String query = search.getText();
                if (query != null && query.trim().length() > 0) {
                    toServices.go(MultimapUtil.fromMultiple("query", query)); //$NON-NLS-1$
                    search.setValue(""); //$NON-NLS-1$
                }
            }
        });
        logo.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toDashboard.go();
            }
        });
        logo.getElement().getStyle().setCursor(Cursor.POINTER);
        username.setText(""); //$NON-NLS-1$
    }

    /**
     * @param currentUserBean
     */
    public void setValue(UserBean currentUserBean) {
        String uname = currentUserBean.getUsername();
        if (currentUserBean.getFullName() != null) {
            username.setText(currentUserBean.getFullName());
        } else {
            username.setText(uname);
        }
        String userOrgsHref = navHelper.createHrefToPage(UserOrgsPage.class, MultimapUtil.singleItemMap("user", uname)); //$NON-NLS-1$
        String userProfileHref = navHelper.createHrefToPage(SettingsProfilePage.class, MultimapUtil.emptyMap());
        
        toUserHome.setHref(userOrgsHref);
        toProfile.setHref(userProfileHref);
        
        toLogout.setHref(config.getCurrentConfig().getApiman().getLogoutUrl());
    }

}
