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
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
    AsyncActionButton searchButton;
    @Inject @DataField
    UserSelector users;
    @Inject @DataField
    RoleMultiSelector roles;
    @Inject @DataField
    AsyncActionButton addButton;
    
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
        searchBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                boolean enterPressed = KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode();
                if (enterPressed) {
                    onSearch(null);
                }
            }
        });
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
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
        addButton.reset();
        addButton.setEnabled(false);
        unlockPage();
        roles.setOptions(roleBeans);
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
        searchButton.onActionStarted();
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
                searchBox.setEnabled(true);
                searchButton.onActionComplete();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
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
        addButton.onActionStarted();

        UserBean user = users.getValue();
        final Set<String> roleIds = roles.getValue();
        String orgId = org;
        rest.grant(orgId, user.getUsername(), roleIds, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                onAddComplete();
            }
            @Override
            public void onError(Throwable error) {
                addButton.onActionComplete();
                unlockPage();
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when all the grants are complete.
     */
    protected void onAddComplete() {
        addButton.onActionComplete();
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
