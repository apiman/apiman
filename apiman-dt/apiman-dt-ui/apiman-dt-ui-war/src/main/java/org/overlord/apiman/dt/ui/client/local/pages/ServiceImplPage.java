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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.services.EndpointType;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.service.EndpointTypeSelectBox;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The "Service" page, with the Implementation tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/service-impl.html#page")
@Page(path="service-impl")
@Dependent
public class ServiceImplPage extends AbstractServicePage {
    
    @Inject @DataField
    TextBox endpoint;
    @Inject @DataField
    EndpointTypeSelectBox endpointType;
    @Inject @DataField
    AsyncActionButton saveButton;
    @Inject @DataField
    Button cancelButton;
    
    /**
     * Constructor.
     */
    public ServiceImplPage() {
    }

    @PostConstruct
    protected void postConstruct() {
        List<EndpointType> types = new ArrayList<EndpointType>();
        types.add(EndpointType.rest);
        types.add(EndpointType.soap);
        endpointType.setOptions(types);
        
        endpoint.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onFormValueChange();
            }
        });
        endpointType.addValueChangeHandler(new ValueChangeHandler<EndpointType>() {
            @Override
            public void onValueChange(ValueChangeEvent<EndpointType> event) {
                onFormValueChange();
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractServicePage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        endpoint.setValue(versionBean.getEndpoint());
        endpointType.setValue(versionBean.getEndpointType());
        saveButton.reset();
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    /**
     * Called when the user clicks the Save button.
     * @param event
     */
    @EventHandler("saveButton")
    public void onSave(ClickEvent event) {
        saveButton.onActionStarted();
        cancelButton.setEnabled(false);
        
        final String endpointValue = this.endpoint.getValue();
        final EndpointType endpointTypeValue = this.endpointType.getValue();
        versionBean.setEndpoint(endpointValue);
        versionBean.setEndpointType(endpointTypeValue);
        rest.getServiceVersion(serviceBean.getOrganizationId(), serviceBean.getId(), versionBean.getVersion(), new IRestInvokerCallback<ServiceVersionBean>() {
            @Override
            public void onSuccess(final ServiceVersionBean response) {
                response.setEndpoint(endpointValue);
                response.setEndpointType(endpointTypeValue);
                rest.updateServiceVersion(serviceBean.getOrganizationId(), serviceBean.getId(),
                        versionBean.getVersion(), response, new IRestInvokerCallback<Void>() {
                    @Override
                    public void onSuccess(Void response) {
                        saveButton.onActionComplete();
                        saveButton.setEnabled(false);
                    }
                    @Override
                    public void onError(Throwable error) {
                        dataPacketError(error);
                    }
                });
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });

    }

    /**
     * Called when the user clicks the Save button.
     * @param event
     */
    @EventHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        endpoint.setValue(versionBean.getEndpoint());
        endpointType.setValue(versionBean.getEndpointType());
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    /**
     * Called when the user modifies something on the page.
     */
    protected void onFormValueChange() {
        saveButton.setEnabled(isFormValid());
        cancelButton.setEnabled(true);
    }
    
    /**
     * @return true if the values in the form are valid
     */
    private boolean isFormValid() {
        return true;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SERVICE_IMPL, serviceBean.getName());
    }

}
