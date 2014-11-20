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

import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.admin.PermissionSelector;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user create a new Role.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/new-role.html#page")
@Page(path="new-role")
@Dependent
public class NewRolePage extends AbstractPage {
    
    @Inject
    TransitionTo<AdminRolesPage> toRoles;
    
    @Inject @DataField
    TextBox name;
    @Inject @DataField
    TextBox description;
    @Inject @DataField
    CheckBox autoGrant;
    @Inject @DataField
    PermissionSelector permissions;
    
    @Inject @DataField
    AsyncActionButton createButton;
    
    /**
     * Constructor.
     */
    public NewRolePage() {
    }

    /**
     * Post construct method.
     */
    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler kph = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onFormChanged();
            }
        };
        name.addKeyUpHandler(kph);
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        name.setFocus(true);
        createButton.reset();
        createButton.setEnabled(false);
    }
    
    /**
     * Called when the user clicks the Create Role button.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        createButton.onActionStarted();
        RoleBean newRole = new RoleBean();
        newRole.setName(name.getValue().trim());
        newRole.setDescription(description.getValue().trim());
        newRole.setAutoGrant(autoGrant.getValue());
        newRole.setPermissions(permissions.getValue());
        rest.createRole(newRole, new IRestInvokerCallback<RoleBean>() {
            @Override
            public void onSuccess(RoleBean response) {
                toRoles.go();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called whenever the user modifies the form.  Checks for form validity and then
     * enables or disables the Create button as appropriate.
     */
    protected void onFormChanged() {
        boolean formComplete = true;
        if (name.getValue() == null || name.getValue().trim().isEmpty()) {
            formComplete = false;
        }
        createButton.setEnabled(formComplete);
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_NEW_ROLE);
    }

}
