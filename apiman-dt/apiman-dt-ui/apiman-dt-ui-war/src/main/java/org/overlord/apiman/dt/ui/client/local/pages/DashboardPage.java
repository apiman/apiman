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
import org.overlord.apiman.dt.api.beans.users.UserBean;
import org.overlord.apiman.dt.api.rest.contract.UserResource;
import org.overlord.apiman.dt.api.rest.exceptions.UserNotFoundException;

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
    private Caller<UserResource> user;

    @Inject @DataField
    private Button testButton;
    @Inject @DataField
    private TextBox testValue;

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
        testButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                RemoteCallback<UserBean> callback = new RemoteCallback<UserBean>() {
                    @Override
                    public void callback(UserBean response) {
                        Window.alert("User is: " + response.getEmail());
                    }
                };
                RestErrorCallback errorCallback = new RestErrorCallback() {
                    @Override
                    public boolean error(Request request, Throwable throwable) {
                        try {
                            throw throwable;
                        } catch (UserNotFoundException e) {
                            Window.alert("User not found: " + e.getMessage());
                        } catch (Throwable t) {
                            Window.alert("Unknown Error: " + throwable.getMessage());
                        }
                        return false;
                    }
                };
                user.call(callback, errorCallback).getUser(testValue.getValue());
            }
        });
    }

    @PageShown
    public void onPageShown() {
    }

}
