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

import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user create a new Organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/new-org.html#page")
@Page(path="new-org")
@Dependent
public class NewOrgPage extends AbstractPage {
    
    @Inject
    TransitionTo<OrgRedirectPage> toOrg;
    
    @Inject @DataField
    TextBox name;
    @Inject @DataField
    TextBox description;
    @Inject @DataField
    AsyncActionButton createButton;
    
    /**
     * Constructor.
     */
    public NewOrgPage() {
    }
    
    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        name.setFocus(true);
        createButton.reset();
    }

    /**
     * Called when the user clicks the Create Organization button.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        createButton.onActionStarted();
        OrganizationBean bean = new OrganizationBean();
        bean.setName(name.getValue());
        bean.setDescription(description.getValue());
        rest.createOrganization(bean, new IRestInvokerCallback<OrganizationBean>() {
            @Override
            public void onSuccess(OrganizationBean response) {
                createButton.onActionComplete();
                String orgId = response.getId();
                // Short circuit page loading lifecycle - redirect to the Org page
                toOrg.go(MultimapUtil.singleItemMap("org", orgId)); //$NON-NLS-1$
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_NEW_ORG);
    }

}
