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
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.admin.GatewayTable;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Anchor;

/**
 * The Role Management admin page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/admin-gateways.html#page")
@Page(path="admin-gateways")
@Dependent
public class AdminGatewaysPage extends AbstractAdminPage {

    @Inject @DataField
    Anchor toNewGateway;
    @Inject @DataField
    GatewayTable gateways;

    List<GatewayBean> gatewayBeans;
    
    /**
     * Constructor.
     */
    public AdminGatewaysPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.listGateways(new IRestInvokerCallback<List<GatewayBean>>() {
            @Override
            public void onSuccess(List<GatewayBean> response) {
                gatewayBeans = response;
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractAdminPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();

        String newGatewayHref = navHelper.createHrefToPage(NewGatewayPage.class, MultimapUtil.emptyMap());
        toNewGateway.setHref(newGatewayHref);
        
        gateways.setValue(gatewayBeans);
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_ADMIN_GATEWAYS);
    }

}
