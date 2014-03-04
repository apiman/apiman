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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.org.OrgMemberList;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The "Organization" page, with the Members tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/org-members.html#page")
@Page(path="org-members")
@Dependent
public class OrgMembersPage extends AbstractOrgPage {
    
    @Inject @DataField
    Anchor toManageMembers;

    @Inject @DataField
    TextBox memberFilter;
    @Inject @DataField
    OrgMemberList members;

    /**
     * Constructor.
     */
    public OrgMembersPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        memberFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                filterMembers();
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        return rval + 0;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        members.setValue(memberBeans);
        String manageMembersHref = navHelper.createHrefToPage(OrgManageMembersPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        toManageMembers.setHref(manageMembersHref);
    }

    /**
     * Apply a filter to the list of members.
     */
    protected void filterMembers() {
        List<MemberBean> filtered = new ArrayList<MemberBean>();
        for (MemberBean member : memberBeans) {
            if (matchesFilter(member)) {
                filtered.add(member);
            }
        }
        members.setFilteredValue(filtered);
    }

    /**
     * Returns true if the given member matches the current filter.
     * @param member
     */
    private boolean matchesFilter(MemberBean member) {
        if (memberFilter.getValue() == null || memberFilter.getValue().trim().length() == 0)
            return true;
        if (member.getUserName().toUpperCase().contains(memberFilter.getValue().toUpperCase()))
            return true;
        if (member.getUserId().toUpperCase().contains(memberFilter.getValue().toUpperCase()))
            return true;
        return false;
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_ORG_MEMBERS, organizationBean.getName());
    }

}
