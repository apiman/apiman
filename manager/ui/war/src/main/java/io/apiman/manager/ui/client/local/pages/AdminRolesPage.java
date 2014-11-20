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

import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.admin.RoleList;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextBox;

/**
 * The Role Management admin page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/admin-roles.html#page")
@Page(path="admin-roles")
@Dependent
public class AdminRolesPage extends AbstractAdminPage {
    
    @Inject @DataField
    TextBox roleFilter;
    @Inject @DataField
    Anchor toNewRole;
    @Inject @DataField
    RoleList roles;
    
    List<RoleBean> roleBeans;
    
    /**
     * Constructor.
     */
    public AdminRolesPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        roleFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                filterRoles();
            }
        });
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractAdminPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();

        String newRoleHref = navHelper.createHrefToPage(NewRolePage.class, MultimapUtil.emptyMap());
        toNewRole.setHref(newRoleHref);
        
        roles.setValue(roleBeans);
    }

    /**
     * Apply a filter to the list of applications.
     */
    protected void filterRoles() {
        if (roleFilter.getValue() == null || roleFilter.getValue().trim().length() == 0) {
            roles.setValue(roleBeans);
        } else {
            List<RoleBean> filtered = new ArrayList<RoleBean>();
            for (RoleBean role : roleBeans) {
                if (matchesFilter(role)) {
                    filtered.add(role);
                }
            }
            roles.setFilteredValue(filtered);
        }
    }

    /**
     * Returns true if the given role matches the current filter.
     * @param role
     */
    private boolean matchesFilter(RoleBean role) {
        if (role.getName().toUpperCase().contains(roleFilter.getValue().toUpperCase())) {
            return true;
        }
        return false;
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_ADMIN_ROLES);
    }

}
