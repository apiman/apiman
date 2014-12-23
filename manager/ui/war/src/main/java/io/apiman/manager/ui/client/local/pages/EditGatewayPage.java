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

import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.RestGatewayConfigBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent.Handler;
import io.apiman.manager.ui.client.local.services.BeanMarshallingService;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.widgets.ConfirmationDialog;

import javax.annotation.PostConstruct;
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
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user edit (or delete) a Gateway.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/edit-gateway.html#page")
@Page(path="edit-gateway")
@Dependent
public class EditGatewayPage extends AbstractPage {

    @Inject
    BeanMarshallingService marshaller;

    @Inject
    TransitionTo<AdminGatewaysPage> toGateways;

    @PageState
    String id;

    @Inject @DataField
    TextBox name;
    @Inject @DataField
    TextArea description;

    @Inject @DataField
    TextBox configEndpoint;
    @Inject @DataField
    TextBox username;
    @Inject @DataField
    TextBox password;
    @Inject @DataField
    TextBox passwordConfirm;

    @Inject @DataField
    AsyncActionButton updateButton;
    @Inject @DataField
    AsyncActionButton deleteButton;

    GatewayBean gatewayBean;
    RestGatewayConfigBean configBean;
    
    /**
     * Constructor.
     */
    public EditGatewayPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler keyUpHandler = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                enableUpdateButtonIfValid();
            }
        };
        description.addKeyUpHandler(keyUpHandler);
        configEndpoint.addKeyUpHandler(keyUpHandler);
        username.addKeyUpHandler(keyUpHandler);
        password.addKeyUpHandler(keyUpHandler);
        passwordConfirm.addKeyUpHandler(keyUpHandler);
    }
    
    /**
     * Enables the update button only if the contents of the form are valid.
     */
    protected void enableUpdateButtonIfValid() {
        String ce = configEndpoint.getValue();
        String u = username.getValue();
        String p1 = password.getValue();
        String p2 = passwordConfirm.getValue();
        boolean valid = true;
        if (ce == null || ce.trim().length() == 0) {
            valid = false;
        }
        if (u == null || u.trim().length() == 0) {
            valid = false;
        }
        if (p1 != null & p1.trim().length() > 0) {
            if (!p1.equals(p2)) {
                valid = false;
            }
        }
        
        boolean dirty = false;
        if (valid) {
            if (!description.getValue().trim().equals(gatewayBean.getDescription())) {
                dirty = true;
            }
            if (!configEndpoint.getValue().trim().equals(configBean.getEndpoint())) {
                dirty = true;
            }
            if (!username.getValue().trim().equals(configBean.getUsername())) {
                dirty = true;
            }
            if (!password.getValue().trim().equals(configBean.getPassword())) {
                dirty = true;
            }
            updateButton.setEnabled(valid && dirty);
        }
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getGateway(id, new IRestInvokerCallback<GatewayBean>() {
            @Override
            public void onSuccess(GatewayBean response) {
                gatewayBean = response;
                String configuration = gatewayBean.getConfiguration();
                configBean = marshaller.unmarshal(configuration, RestGatewayConfigBean.class);
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
        name.setValue(gatewayBean.getName());
        description.setValue(gatewayBean.getDescription());
        configEndpoint.setValue(configBean.getEndpoint());
        username.setValue(configBean.getUsername());
        password.setValue(configBean.getPassword());
        passwordConfirm.setValue(configBean.getPassword());
        updateButton.setEnabled(false);
    }

    /**
     * Called when the user clicks the Update Gateway button.
     * @param event
     */
    @EventHandler("updateButton")
    public void onUpdate(ClickEvent event) {
        updateButton.onActionStarted();
        deleteButton.setEnabled(false);
        GatewayBean gateway = new GatewayBean();
        gateway.setId(id);
        gateway.setDescription(description.getValue().trim());
        RestGatewayConfigBean configBean = new RestGatewayConfigBean();
        configBean.setEndpoint(configEndpoint.getValue().trim());
        configBean.setUsername(username.getValue().trim());
        if (password.getValue() != null && password.getValue().trim().length() > 0) {
            configBean.setPassword(password.getValue().trim());
        }
        gateway.setConfiguration(marshaller.marshal(configBean));
        rest.updateGateway(gateway, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                toGateways.go();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user clicks the Delete Gateway button.
     * @param event
     */
    @EventHandler("deleteButton")
    public void onDelete(ClickEvent event) {
        deleteButton.onActionStarted();
        updateButton.setEnabled(false);
        
        ConfirmationDialog dialog = confirmationDialogFactory.get();
        dialog.setDialogTitle(i18n.format(AppMessages.CONFIRM_GATEWAY_DELETE_TITLE));
        dialog.setDialogMessage(i18n.format(AppMessages.CONFIRM_GATEWAY_DELETE_MESSAGE, gatewayBean.getName()));
        dialog.addConfirmationHandler(new Handler() {
            @Override
            public void onConfirmation(ConfirmationEvent event) {
                if (event.isConfirmed()) {
                    GatewayBean gateway = new GatewayBean();
                    gateway.setId(id);
                    rest.deleteGateway(gateway, new IRestInvokerCallback<Void>() {
                        @Override
                        public void onSuccess(Void response) {
                            toGateways.go();
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
        return i18n.format(AppMessages.TITLE_EDIT_GATEWAY);
    }

}
