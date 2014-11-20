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

import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.user.UserOrganizationList;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The "User" page, with the Applications tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/user-orgs.html#page")
@Page(path="user-orgs")
@Dependent
public class UserOrgsPage extends AbstractUserPage {
    
    List<OrganizationSummaryBean> orgs;
    
    @Inject @DataField
    TextBox orgFilter;
    @Inject @DataField
    UserOrganizationList organizations;
    
    @Inject @DataField
    TransitionAnchor<NewOrgPage> toNewOrg;

    /**
     * Constructor.
     */
    public UserOrgsPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        orgFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                filterOrgs();
            }
        });
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractUserPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getUserOrgs(user, new IRestInvokerCallback<List<OrganizationSummaryBean>>() {
            @Override
            public void onSuccess(List<OrganizationSummaryBean> response) {
                orgs = response;
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        organizations.setValue(orgs);
        super.renderPage();
    }

    /**
     * Apply a filter to the list of organizations.
     */
    protected void filterOrgs() {
        List<OrganizationSummaryBean> filtered = new ArrayList<OrganizationSummaryBean>();
        for (OrganizationSummaryBean org : orgs) {
            if (matchesFilter(org)) {
                filtered.add(org);
            }
        }
        organizations.setFilteredValue(filtered);
    }

    /**
     * Returns true if the given org matches the current filter.
     * @param org
     */
    private boolean matchesFilter(OrganizationSummaryBean org) {
        if (orgFilter.getValue() == null || orgFilter.getValue().trim().length() == 0)
            return true;
        if (org.getName().toUpperCase().contains(orgFilter.getValue().toUpperCase()))
            return true;
        return false;
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_USER_ORGS, userBean.getFullName());
    }

}
