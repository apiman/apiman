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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.api.beans.members.MemberRoleBean;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.Breadcrumb;
import org.overlord.apiman.dt.ui.client.local.pages.common.RoleMultiSelector;
import org.overlord.apiman.dt.ui.client.local.pages.org.MemberCard;
import org.overlord.apiman.dt.ui.client.local.services.ContextKeys;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.ParagraphLabel;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The "Organization" page, with the Members tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/org-manage-members.html#page")
@Page(path="org-manage-members")
@Dependent
public class OrgManageMembersPage extends AbstractPage {
    
    @PageState
    protected String org;

    OrganizationBean organizationBean;
    List<MemberBean> memberBeans;
    List<RoleBean> roleBeans;

    @Inject @DataField
    Breadcrumb breadcrumb;

    @Inject @DataField
    Anchor organization;
    @Inject @DataField
    TextBox searchBox;
    @Inject @DataField
    RoleMultiSelector roleSelector;
    @Inject @DataField
    Anchor addMember;
    @Inject @DataField
    FlowPanel cards;
    int cardsVisible = 0;

    @Inject
    Instance<MemberCard> cardFactory;

    /**
     * Constructor.
     */
    public OrgManageMembersPage() {
    }
    
    /**
     * Called after the bean is created.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @PostConstruct
    protected void postConstruct() {
        ValueChangeHandler handler = new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                onFilterChange();
            }
        };
        searchBox.addValueChangeHandler(handler);
        roleSelector.addValueChangeHandler(handler);
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
        rest.getOrganization(org, new IRestInvokerCallback<OrganizationBean>() {
            @Override
            public void onSuccess(OrganizationBean response) {
                organizationBean = response;
                currentContext.setAttribute(ContextKeys.CURRENT_ORGANIZATION, organizationBean);
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        rest.getOrgMembers(org, new IRestInvokerCallback<List<MemberBean>>() {
            @Override
            public void onSuccess(List<MemberBean> response) {
                memberBeans = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 3;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();

        String orgMembersHref = navHelper.createHrefToPage(OrgMembersPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        organization.setHref(orgMembersHref);
        String newMemberHref = navHelper.createHrefToPage(NewMemberPage.class, MultimapUtil.singleItemMap("org", org)); //$NON-NLS-1$
        addMember.setHref(newMemberHref);

        roleSelector.setOptions(roleBeans);
        refreshCards();
        
        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.emptyMap());
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addItem(orgMembersHref, "shield", organizationBean.getName()); //$NON-NLS-1$
        breadcrumb.addActiveItem(null, i18n.format(AppMessages.MANAGE_MEMBERS));
    }

    /**
     * Refresh the cards displayed based on the current filter settings and data.
     */
    private void refreshCards() {
        cards.clear();
        cardsVisible = 0;
        for (final MemberBean memberBean : this.memberBeans) {
            if (matchesFilters(memberBean)) {
                final MemberCard card = cardFactory.get();
                card.setRoles(roleBeans);
                card.setValue(memberBean);
                cards.add(card);
                cardsVisible++;
                card.addValueChangeHandler(new ValueChangeHandler<MemberBean>() {
                    @Override
                    public void onValueChange(ValueChangeEvent<MemberBean> event) {
                        onMemberCardChange(card, memberBean, event.getValue());
                    }
                });
            }
        }
        if (cardsVisible == 0) {
            ParagraphLabel noneFound = new ParagraphLabel();
            noneFound.setText(i18n.format(AppMessages.ORG_MANAGE_MEMBERS_NO_MEMBERS_FOUND));
            noneFound.getElement().setClassName("apiman-no-entities-description"); //$NON-NLS-1$
            cards.add(noneFound);
        }
    }

    /**
     * Called when the user changes the member's card.
     * @param card
     * @param oldValue
     * @param newValue
     */
    protected void onMemberCardChange(final MemberCard card, final MemberBean oldValue, final MemberBean newValue) {
        if (newValue == null) {
            // Revoke all roles
            rest.revokeAll(org, oldValue.getUserId(), new IRestInvokerCallback<Void>() {
                @Override
                public void onSuccess(Void response) {
                    memberBeans.remove(oldValue);
                    refreshCards();
                }
                @Override
                public void onError(Throwable error) {
                    dataPacketError(error);
                }
            });
        } else {
            final Set<String> roleIds = new HashSet<String>();
            List<MemberRoleBean> roles = newValue.getRoles();
            for (MemberRoleBean mrb : roles) {
                roleIds.add(mrb.getRoleId());
            }
            // First revoke all existing roles, then grant the new roles.
            rest.revokeAll(org, oldValue.getUserId(), new IRestInvokerCallback<Void>() {
                @Override
                public void onSuccess(Void response) {
                    rest.grant(org, oldValue.getUserId(), roleIds, new IRestInvokerCallback<Void>() {
                        @Override
                        public void onSuccess(Void response) {
                            oldValue.setRoles(newValue.getRoles());
                            card.onApplySuccess(oldValue);
                        }
                        @Override
                        public void onError(Throwable error) {
                            dataPacketError(error);
                        }
                    });
                }
                @Override
                public void onError(Throwable error) {
                    dataPacketError(error);
                }
            });
        }
    }

    /**
     * Returns true if the member should be displayed to the user based on the
     * filter criteria selected.
     * @param memberBean
     */
    private boolean matchesFilters(MemberBean memberBean) {
        String userFilter = searchBox.getValue();
        Set<String> roleFilter = roleSelector.getValue();
        
        String userId = memberBean.getUserId();
        String userName = memberBean.getUserName();
        List<MemberRoleBean> roles = memberBean.getRoles();
        
        if ( (matches(userFilter, userId) || matches(userFilter, userName)) &&
             (matches(roleFilter, roles)) ) {
            return true;
        }
        
        return false;
    }

    /**
     * Returns true if the value matches the filter.  Case does not matter.  Partial
     * matches are returned.
     * @param userFilter
     * @param value
     */
    private boolean matches(String userFilter, String value) {
        if (userFilter == null || userFilter.trim().length() == 0) {
            return true;
        }
        return value.toLowerCase().contains(userFilter.toLowerCase());
    }

    /**
     * Returns true if the user has a role in the list of filtered roles.
     * @param roleFilter
     * @param roles
     */
    private boolean matches(Set<String> roleFilter, List<MemberRoleBean> roles) {
        if (roleFilter == null || roleFilter.isEmpty()) {
            return true;
        }
        for (MemberRoleBean role : roles) {
            if (roleFilter.contains(role.getRoleId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when the user changes any of the filters.
     */
    protected void onFilterChange() {
        refreshCards();
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_ORG_MANAGE_MEMBERS, organizationBean.getName());
    }

}
