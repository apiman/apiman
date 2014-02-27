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
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.idm.UserBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaFilterBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.RoleMultiSelector;
import org.overlord.apiman.dt.ui.client.local.pages.org.UserSelector;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user add a member to an organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/new-member.html#page")
@Page(path="new-member")
@Dependent
public class NewMemberPage extends AbstractPage {
    
    @PageState
    protected String org;
    
    List<RoleBean> roleBeans;

    @Inject @DataField
    TextBox searchBox;
    @Inject @DataField
    Button searchButton;
    @Inject @DataField
    UserSelector users;
    @Inject @DataField
    RoleMultiSelector roles;
    @Inject @DataField
    Button addButton;

    private int waitForIt;

    
    /**
     * Constructor.
     */
    public NewMemberPage() {
    }
    
    /**
     * Called after the page is built.
     */
    @PostConstruct
    protected void postConstruct() {
        roles.addValueChangeHandler(new ValueChangeHandler<Set<String>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<String>> event) {
                onFormUpdated();
            }
        });
        users.addValueChangeHandler(new ValueChangeHandler<UserBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<UserBean> event) {
                onFormUpdated();
            }
        });
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        rest.getRoles(new IRestInvokerCallback<List<RoleBean>>() {
            @Override
            public void onSuccess(List<RoleBean> response) {
                roleBeans = response;
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
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        addButton.setEnabled(false);
        addButton.setHTML(i18n.format(AppMessages.NEW_MEMBER_ADD_BUTTON_ADD));
        unlockPage();
        roles.setRoles(roleBeans);
        users.add(new Label("(" + i18n.format(AppMessages.NEW_MEMBER_SEARCH_TEXT) + ")")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Locks the page - UI elements are disabled.
     */
    private void lockPage() {
        searchBox.setEnabled(false);
        searchButton.setEnabled(false);
        users.setEnabled(false);
    }

    /**
     * Unlocks the page - UI elements are enabled.
     */
    private void unlockPage() {
        searchBox.setEnabled(true);
        searchButton.setEnabled(true);
        users.setEnabled(true);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#onPageLoaded()
     */
    @Override
    protected void onPageLoaded() {
        searchBox.setFocus(true);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_NEW_MEMBER);
    }

    /**
     * Called when the user clicks the Search button to find users.
     * @param event
     */
    @EventHandler("searchButton")
    public void onSearch(ClickEvent event) {
        if (searchBox.getValue().trim().length() == 0)
            return;
        
        searchBox.setEnabled(false);
        searchButton.setEnabled(false);
        searchButton.setHTML("<i class=\"fa fa-cog fa-spin\"></i> " + i18n.format(AppMessages.NEW_MEMBER_SEARCH_BUTTON_SEARCHING)); //$NON-NLS-1$
        users.clear();
        onFormUpdated();
        
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        criteria.setPageSize(50);
        criteria.setPage(1);
        criteria.setOrder("fullName", true); //$NON-NLS-1$
        criteria.addFilter("fullName", "*" + searchBox.getValue() + "*", SearchCriteriaFilterBean.OPERATOR_LIKE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        rest.findUsers(criteria, new IRestInvokerCallback<SearchResultsBean<UserBean>>() {
            @Override
            public void onSuccess(SearchResultsBean<UserBean> response) {
                users.setUsers(response.getBeans());
                searchButton.setEnabled(true);
                searchBox.setEnabled(true);
                searchButton.setHTML(i18n.format(AppMessages.NEW_MEMBER_SEARCH_BUTTON_SEARCH));
            }
            @Override
            public void onError(Throwable error) {
                // TODO report this error in some sensible way!
                Window.alert("Error searching for users: " + error.getMessage()); //$NON-NLS-1$
                searchButton.setEnabled(true);
                searchBox.setEnabled(true);
                searchButton.setHTML(i18n.format(AppMessages.NEW_MEMBER_SEARCH_BUTTON_SEARCH));
            }
        });
    }

    /**
     * Called when the user clicks the Add Member button.
     * @param event
     */
    @EventHandler("addButton")
    public void onAdd(ClickEvent event) {
        lockPage();
        addButton.setHTML("<i class=\"fa fa-cog fa-spin\"></i> " + i18n.format(AppMessages.NEW_MEMBER_ADD_BUTTON_ADDING)); //$NON-NLS-1$

        UserBean user = users.getValue();
        Set<String> roleIds = roles.getValue();
        String orgId = org;
        waitForIt = 0;
        for (String roleId : roleIds) {
            rest.grant(orgId, user.getUsername(), roleId, new IRestInvokerCallback<Void>() {
                @Override
                public void onSuccess(Void response) {
                    waitForIt++;
                    onAddComplete();
                }
                @Override
                public void onError(Throwable error) {
                    // TODO need a way to display errors like this in some sensible way
                    Window.alert("Error granting role(s): " + error.getMessage());
                    unlockPage();
                    searchButton.setHTML(i18n.format(AppMessages.NEW_MEMBER_ADD_BUTTON_ADD));
                }
            });
        }
    }

    /**
     * Called when the grant is complete.
     */
    protected void onAddComplete() {
        navigation.goTo(OrgManageMembersPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
    }

    /**
     * Called when the user makes any change to the form.  This will figure out
     * if the form is complete and then enable or disable the Add button as 
     * appropriate.
     */
    protected void onFormUpdated() {
        boolean formComplete = true;
        if (users.getValue() == null)
            formComplete = false;
        if (roles.getValue() == null || roles.getValue().isEmpty())
            formComplete = false;
        
        addButton.setEnabled(formComplete);
    }

}
