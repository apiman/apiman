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
import io.apiman.manager.ui.client.local.events.ConfirmationEvent;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent.Handler;
import io.apiman.manager.ui.client.local.pages.admin.PermissionSelector;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.widgets.ConfirmationDialog;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user edit a role.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/edit-role.html#page")
@Page(path="edit-role")
@Dependent
public class EditRolePage extends AbstractPage {

    @Inject
    TransitionTo<AdminRolesPage> toRoles;

    @PageState
    String id;

    @Inject @DataField
    TextBox description;
    @Inject @DataField
    CheckBox autoGrant;
    @Inject @DataField
    PermissionSelector permissions;

    @Inject @DataField
    AsyncActionButton updateButton;
    @Inject @DataField
    AsyncActionButton deleteButton;

    RoleBean roleBean;
    
    /**
     * Constructor.
     */
    public EditRolePage() {
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getRole(id, new IRestInvokerCallback<RoleBean>() {
            @Override
            public void onSuccess(RoleBean response) {
                roleBean = response;
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
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        updateButton.reset();
        deleteButton.reset();
        description.setFocus(true);
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        description.setValue(roleBean.getDescription());
        autoGrant.setValue(roleBean.getAutoGrant());
        permissions.setValue(roleBean.getPermissions());
    }

    /**
     * Called when the user clicks the Update Role button.
     * @param event
     */
    @EventHandler("updateButton")
    public void onUpdate(ClickEvent event) {
        updateButton.onActionStarted();
        deleteButton.setEnabled(false);
        RoleBean role = new RoleBean();
        role.setId(id);
        role.setDescription(description.getValue().trim());
        role.setAutoGrant(autoGrant.getValue());
        role.setPermissions(permissions.getValue());
        rest.updateRole(role, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                toRoles.go();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user clicks the Delete Role button.
     * @param event
     */
    @EventHandler("deleteButton")
    public void onDelete(ClickEvent event) {
        deleteButton.onActionStarted();
        updateButton.setEnabled(false);
        
        ConfirmationDialog dialog = confirmationDialogFactory.get();
        dialog.setDialogTitle(i18n.format(AppMessages.CONFIRM_ROLE_DELETE_TITLE));
        dialog.setDialogMessage(i18n.format(AppMessages.CONFIRM_ROLE_DELETE_MESSAGE, roleBean.getName()));
        dialog.addConfirmationHandler(new Handler() {
            @Override
            public void onConfirmation(ConfirmationEvent event) {
                if (event.isConfirmed()) {
                    RoleBean role = new RoleBean();
                    role.setId(id);
                    rest.deleteRole(role, new IRestInvokerCallback<Void>() {
                        @Override
                        public void onSuccess(Void response) {
                            toRoles.go();
                        }
                        @Override
                        public void onError(Throwable error) {
                            dataPacketError(error);
                        }
                    });
                } else {
                    deleteButton.reset();
                    updateButton.reset();
                }
            }
        });
        dialog.show();
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_EDIT_ROLE);
    }

}
