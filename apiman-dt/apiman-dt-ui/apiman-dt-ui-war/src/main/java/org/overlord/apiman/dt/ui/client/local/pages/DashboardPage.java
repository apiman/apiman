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
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.rest.contract.UserResource;

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
    
//    @Inject @DataField
//    private Button testButton;
//    @Inject @DataField
//    private TextBox testValue;

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
//        testButton.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                RemoteCallback<UserBean> callback2 = new RemoteCallback<UserBean>() {
//                    @Override
//                    public void callback(UserBean response) {
//                        Window.alert("User is: " + response.getEmail());
//                    }
//                };
//                try {
//                    user.call(callback2).getUser(testValue.getValue());
//                } catch (UserNotFoundException e) {
//                    Window.alert("Error: " + e.getMessage());
//                }
//            }
//        });
    }

    @PageShown
    public void onPageShown() {
    }

}
