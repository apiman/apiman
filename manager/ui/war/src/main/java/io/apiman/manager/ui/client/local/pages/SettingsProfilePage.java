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

import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The User Profile settings page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/settings-profile.html#page")
@Page(path="settings-profile")
@Dependent
public class SettingsProfilePage extends AbstractPage {
    
    @Inject @DataField
    TextBox username;
    @Inject @DataField
    TextBox name;
    @Inject @DataField
    TextBox email;
    @Inject @DataField
    AsyncActionButton updateButton;
    
    UserBean userBean;

    /**
     * Constructor.
     */
    public SettingsProfilePage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        name.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateButton.setEnabled(true);
            }
        });
        email.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateButton.setEnabled(true);
            }
        });
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractUserPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getCurrentUserInfo(new IRestInvokerCallback<UserBean>() {
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        username.setValue(userBean.getUsername());
        name.setValue(userBean.getFullName());
        email.setValue(userBean.getEmail());
        updateButton.reset();
        updateButton.setEnabled(false);
    }
    
    /**
     * Called when the user clicks the Update button.
     * @param event
     */
    @EventHandler("updateButton")
    public void onUpdate(ClickEvent event) {
        updateButton.onActionStarted();
        UserBean updatedUser = new UserBean();
        updatedUser.setUsername(userBean.getUsername());
        updatedUser.setFullName(name.getValue());
        updatedUser.setEmail(email.getValue());
        rest.updateCurrentUserInfo(updatedUser, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                updateButton.onActionComplete();
                updateButton.setEnabled(false);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SETTINGS_PROFILE);
    }

}
