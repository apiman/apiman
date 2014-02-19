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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.apps.ApplicationBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.ui.client.local.pages.common.OrganizationSelector;
import org.overlord.apiman.dt.ui.client.local.services.ContextKeys;
import org.overlord.apiman.dt.ui.client.local.services.CurrentContextService;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user create a new Organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/new-app.html#page")
@Page(path="new-app")
@Dependent
public class NewAppPage extends AbstractPage {
    
    @Inject
    CurrentContextService context;
    @Inject
    TransitionTo<AppOverviewPage> toAppOverview;
    
    List<OrganizationSummaryBean> organizations;
    
    @Inject @DataField
    OrganizationSelector orgSelector;
    @Inject @DataField
    TextBox name;
    @Inject @DataField
    TextBox description;
    @Inject @DataField
    Button createButton;
    
    /**
     * Constructor.
     */
    public NewAppPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        orgSelector.addValueChangeHandler(new ValueChangeHandler<OrganizationSummaryBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<OrganizationSummaryBean> event) {
                name.setFocus(true);
            }
        });
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        rest.getCurrentUserOrgs(new IRestInvokerCallback<List<OrganizationSummaryBean>>() {
            @Override
            public void onSuccess(List<OrganizationSummaryBean> response) {
                organizations = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval+1;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        orgSelector.setOrganizations(organizations);
        OrganizationBean org = (OrganizationBean) context.getAttribute(ContextKeys.CURRENT_ORGANIZATION);
        if (org != null) {
            for (OrganizationSummaryBean bean : organizations) {
                if (bean.getId().equals(org.getId())) {
                    orgSelector.setValue(bean);
                    break;
                }
            }
        }
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        name.setFocus(true);
        createButton.setEnabled(true);
    }
    
    /**
     * Called when the user clicks the Create Organization button.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        createButton.setEnabled(false);
        createButton.setFocus(false);
        final String orgId = orgSelector.getValue().getId();
        ApplicationBean bean = new ApplicationBean();
        bean.setName(name.getValue());
        bean.setDescription(description.getValue());
        rest.createApplication(orgId, bean, new IRestInvokerCallback<ApplicationBean>() {
            @Override
            public void onSuccess(ApplicationBean response) {
                String orgId = response.getOrganizationId();
                String appId = response.getId();
                // Short circuit page loading lifecycle - redirect to the Org page
                toAppOverview.go(MultimapUtil.fromMultiple("org", orgId, "app", appId)); //$NON-NLS-1$ //$NON-NLS-2$
            }
            @Override
            public void onError(Throwable error) {
                // TODO do something interesting here!
                Window.alert("App creation failed: " + error.getMessage()); //$NON-NLS-1$
            }
        });
    }

}
