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
import io.apiman.manager.ui.client.local.services.BeanMarshallingService;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
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
    TextBox httpEndpoint;
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
    
    /**
     * Constructor.
     */
    public EditGatewayPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        ValueChangeHandler<String> handler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                enableUpdateButtonIfValid();
            }
        };
        httpEndpoint.addValueChangeHandler(handler);
        configEndpoint.addValueChangeHandler(handler);
        username.addValueChangeHandler(handler);
        password.addValueChangeHandler(handler);
        passwordConfirm.addValueChangeHandler(handler);
    }
    
    /**
     * Enables the update button only if the contents of the form are valid.
     */
    protected void enableUpdateButtonIfValid() {
        String he = httpEndpoint.getValue();
        String ce = configEndpoint.getValue();
        String u = username.getValue();
        String p1 = password.getValue();
        String p2 = passwordConfirm.getValue();
        boolean valid = true;
        if (he == null || he.trim().length() == 0) {
            valid = false;
        }
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
        updateButton.setEnabled(valid);
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
        description.setValue(gatewayBean.getDescription());
        httpEndpoint.setValue(gatewayBean.getHttpEndpoint());
        String configuration = gatewayBean.getConfiguration();
        RestGatewayConfigBean configBean = marshaller.unmarshal(configuration, RestGatewayConfigBean.class);
        configEndpoint.setValue(configBean.getEndpoint());
        username.setValue(configBean.getUsername());
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
        gateway.setHttpEndpoint(httpEndpoint.getValue().trim());
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
        if (Window.confirm(i18n.format(AppMessages.CONFIRM_GATEWAY_DELETE, gatewayBean.getName()))) {
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
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_EDIT_GATEWAY);
    }

}
