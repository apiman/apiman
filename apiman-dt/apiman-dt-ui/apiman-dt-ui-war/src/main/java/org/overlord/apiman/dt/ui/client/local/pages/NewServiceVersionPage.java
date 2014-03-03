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

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user create a new Service Version.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/new-serviceversion.html#page")
@Page(path="new-serviceversion")
@Dependent
public class NewServiceVersionPage extends AbstractPage {
    
    @PageState
    String org;
    @PageState
    String service;
    
    @Inject
    TransitionTo<ServiceOverviewPage> toService;
    
    @Inject @DataField
    TextBox version;
    @Inject @DataField
    AsyncActionButton createButton;
    
    /**
     * Constructor.
     */
    public NewServiceVersionPage() {
    }

    @PostConstruct
    protected void postConstruct() {
        KeyUpHandler kph = new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onFormUpdated();
            }
        };
        version.addKeyUpHandler(kph);
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        return rval;
    }
    
    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        version.setFocus(true);
        createButton.reset();
        createButton.setEnabled(false);
    }

    /**
     * Called when the user clicks the Create Organization button.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        createButton.onActionStarted();
        ServiceVersionBean newVersion = new ServiceVersionBean();
        final String ver = version.getValue();
        newVersion.setVersion(ver);
        rest.createServiceVersion(org, service, newVersion, new IRestInvokerCallback<ServiceVersionBean>() {
            @Override
            public void onSuccess(ServiceVersionBean response) {
                toService.go(MultimapUtil.fromMultiple("org", org, "service", service, "version", ver)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_NEW_SERVICE_VERSION);
    }

    /**
     * Called whenever the user modifies the form.  Checks for form validity and then
     * enables or disables the Create button as appropriate.
     */
    protected void onFormUpdated() {
        boolean formComplete = true;
        if (version.getValue() == null || version.getValue().trim().length() == 0)
            formComplete = false;
        createButton.setEnabled(formComplete);
    }

}
