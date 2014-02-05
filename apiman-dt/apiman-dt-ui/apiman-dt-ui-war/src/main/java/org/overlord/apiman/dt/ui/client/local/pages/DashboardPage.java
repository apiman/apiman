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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.BeanUtils;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.users.UserBean;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.AbstractRestException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The "Dashboard" page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/dashboard.html#page")
@Page(path="dashboard", role=DefaultPage.class)
@Dependent
public class DashboardPage extends AbstractPage {

    @Inject
    private Caller<IUserResource> user;
    @Inject
    private Caller<IOrganizationResource> org;

    @Inject @DataField
    private Button testButton;
    @Inject @DataField
    private TextBox testValue;


    @Inject @DataField
    private Button orgAddButton;
    @Inject @DataField
    private Button orgGetButton;
    @Inject @DataField
    private TextBox orgValue;

    
    /**
     * Constructor.
     */
    public DashboardPage() {
    }
    
    /**
     * Called after the page is built.
     */
    @PostConstruct
    protected void postConstruct() {
        final RestErrorCallback errorCallback = new RestErrorCallback() {
            @Override
            public boolean error(Request request, Throwable throwable) {
                try {
                    throw throwable;
                } catch (AbstractRestException e) {
                    Window.alert("REST error: " + e.getMessage());
                } catch (Throwable t) {
                    Window.alert("Unknown Error: " + throwable.getMessage());
                }
                return false;
            }
        };

        testButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                user.call(new RemoteCallback<UserBean>() {
                    @Override
                    public void callback(UserBean response) {
                        Window.alert("User is: " + response.getEmail());
                    }
                }, errorCallback).getUser(testValue.getValue());
            }
        });
        
        orgAddButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                OrganizationBean newOrg = new OrganizationBean();
                newOrg.setName(orgValue.getValue());
                org.call(new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void response) {
                        Window.alert("Created!");
                    }
                }, errorCallback).create(newOrg);
            }
        });
        orgGetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String orgId = BeanUtils.idFromName(orgValue.getValue());
                org.call(new RemoteCallback<OrganizationBean>() {
                    @Override
                    public void callback(OrganizationBean bean) {
                        Window.alert("Organization: " + bean.getName());
                    }
                }, errorCallback).get(orgId);
            }
        });
    }

    @PageShown
    public void onPageShown() {
    }

}
