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
import io.apiman.manager.api.beans.services.EndpointType;
import io.apiman.manager.api.beans.services.ServiceGatewayBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.service.EndpointTypeSelectBox;
import io.apiman.manager.ui.client.local.pages.service.GatewaySelectBox;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
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
@Templated("/io/apiman/manager/ui/client/local/site/service-impl.html#page")
@Page(path="service-impl")
@Dependent
public class ServiceImplPage extends AbstractServicePage {
    
    @Inject @DataField
    TextBox endpoint;
    @Inject @DataField
    EndpointTypeSelectBox endpointType;
    @Inject @DataField
    GatewaySelectBox gateway;

    @Inject @DataField
    AsyncActionButton saveButton;
    @Inject @DataField
    Button cancelButton;
    
    List<GatewayBean> gatewayBeans;

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
        gateway.addValueChangeHandler(new ValueChangeHandler<GatewayBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<GatewayBean> event) {
                onFormValueChange();
            }
        });
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.listGateways(new IRestInvokerCallback<List<GatewayBean>>() {
            @Override
            public void onSuccess(List<GatewayBean> response) {
                onGatewaysLoaded(response);
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
     * Called when the gateways are loaded.
     * @param beans
     */
    protected void onGatewaysLoaded(List<GatewayBean> beans) {
        gatewayBeans = beans;
        gateway.setOptions(gatewayBeans);
        if (gatewayBeans.size() > 1) {
            showGateways();
        } else {
            hideGateways();
        }
    }

    protected final native void showGateways() /*-{
        $wnd.jQuery('#gateway-info').show();
    }-*/;

    protected final native void hideGateways() /*-{
        $wnd.jQuery('#gateway-info').hide();
    }-*/;

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        endpoint.setValue(versionBean.getEndpoint());
        endpointType.setValue(versionBean.getEndpointType());
        if (versionBean.getGateways() != null && versionBean.getGateways().size() > 0) {
            gateway.selectGatewayById(versionBean.getGateways().iterator().next().getGatewayId());
        }
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

        ServiceVersionBean update = new ServiceVersionBean();
        update.setEndpoint(endpointValue);
        update.setEndpointType(endpointTypeValue);

        if (gatewayBeans.size() > 1) {
            GatewayBean gb = gateway.getValue();
            Set<ServiceGatewayBean> sgateways = new HashSet<ServiceGatewayBean>();
            ServiceGatewayBean sgb = new ServiceGatewayBean();
            sgb.setGatewayId(gb.getId());
            sgateways.add(sgb);
            versionBean.setGateways(sgateways);
            update.setGateways(sgateways);
        }
        
        rest.updateServiceVersion(serviceBean.getOrganizationId(), serviceBean.getId(),
                versionBean.getVersion(), update, new IRestInvokerCallback<Void>() {
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

    /**
     * Called when the user clicks the Save button.
     * @param event
     */
    @EventHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        endpoint.setValue(versionBean.getEndpoint());
        endpointType.setValue(versionBean.getEndpointType());
        if (versionBean.getGateways() != null && versionBean.getGateways().size() > 0) {
            gateway.selectGatewayById(versionBean.getGateways().iterator().next().getGatewayId());
        }

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
        boolean valid = true;
        if (endpoint.getValue() == null || endpoint.getValue().trim().isEmpty()) {
            valid = false;
        }
        return valid;
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SERVICE_IMPL, serviceBean.getName());
    }

}
