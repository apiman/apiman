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
package org.overlord.apiman.dt.ui.client.local.pages.org;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.idm.RoleBean;
import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.api.beans.members.MemberRoleBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.RoleMultiSelector;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;

/**
 * Models a single 'card' in the manage members page's list of organization
 * members.  Each member is represented by one of these cards.  Each card
 * shows relevant information about the user as well as some actions.  The
 * back of each card allows modifying the user's roles.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/org-manage-members.html#card")
@Dependent
public class MemberCard extends Composite implements HasValue<MemberBean> {
    
    private MemberBean value;
    private List<RoleBean> roleBeans;
    
    @Inject
    TranslationService i18n;
    
    // Front of card
    
    @Inject @DataField
    Label fullName;
    @Inject @DataField
    Label userId;
    @Inject @DataField
    Anchor editButton;
    @Inject @DataField
    Anchor email;
    @Inject @DataField
    Label roles;
    @Inject @DataField
    Label joinedOn;
    
    // Back of card
    
    @Inject @DataField
    Label editExplanation;
    @Inject @DataField
    RoleMultiSelector editRolesSelector;
    @Inject @DataField
    Button cancelButton;
    @Inject @DataField
    Button applyButton;
    @Inject @DataField
    Button revokeButton;
    
    /**
     * Constructor.
     */
    public MemberCard() {
    }
    
    /**
     * Called after an instance of this class is built.
     */
    @PostConstruct
    protected void postConstruct() {
        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                flipCard();
                event.preventDefault();
            }
        });
        revokeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doRevoke();
            }
        });
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refresh();
                flipCard();
            }
        });
        applyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doApply();
            }
        });
    }
    
    /**
     * Sets the available roles.  This must be called before setValue.
     * @param roles
     */
    public void setRoles(List<RoleBean> roles) {
        roleBeans = roles;
        editRolesSelector.setRoles(roles);
    }
    
    /**
     * Flips the card over.
     */
    public void flipCard() {
        flipCard(getElement());
    }

    /**
     * Native helper method for flipping the card.
     */
    private native void flipCard(Element elem) /*-{
        $wnd.jQuery(elem).closest('.apiman-card').find('.front').toggleClass('active');
        $wnd.jQuery(elem).closest('.apiman-card').find('.back').toggleClass('active')
    }-*/;


    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MemberBean> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public MemberBean getValue() {
        return value;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(MemberBean value) {
        setValue(value, false);
        refresh();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(MemberBean value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }
    
    /**
     * Refreshes the presentation of the current value in the UI components.
     */
    private void refresh() {
        fullName.setText(this.value.getUserName());
        userId.setText("(" + value.getUserId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        email.setText(this.value.getEmail());
        email.setHref("mailto:" + this.value.getEmail()); //$NON-NLS-1$
        roles.setText(formatRoles(this.value));
        joinedOn.setText(formatJoinedOn(this.value));
        
        editExplanation.setText(i18n.format(AppMessages.MEMBER_CARD_ASSIGN_ROLES_HELP, this.value.getUserName()));
        Set<String> roleIds = new HashSet<String>();
        for (MemberRoleBean memberRoleBean : this.value.getRoles()) {
            roleIds.add(memberRoleBean.getRoleId());
        }
        editRolesSelector.setValue(roleIds);
    }

    /**
     * Formats the member's roles into a comma separated string.
     * @param member
     */
    private static String formatRoles(MemberBean member) {
        StringBuilder builder = new StringBuilder();
        List<MemberRoleBean> roles = member.getRoles();
        boolean first = true;
        for (MemberRoleBean role : roles) {
            if (first) {
                first = false;
            } else {
                builder.append(", "); //$NON-NLS-1$
            }
            builder.append(role.getRoleName());
        }
        return builder.toString();
    }

    /**
     * Formats the joinedOn field value.
     * @param member
     */
    private static String formatJoinedOn(MemberBean member) {
        DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
        return format.format(member.getJoinedOn());
    }

    /**
     * Called when the user clicks the "Remove" button.
     */
    protected void doRevoke() {
        // TODO replace this with a bootstrap modal yes/no dialog!
        if (Window.confirm("This will remove the user from all roles in the Organization.  Really do this?")) {
            // Firing with a null value is a signal to the page that the user wants to delete the card.
            ValueChangeEvent.fire(this, null);
        }
    }

    /**
     * Called when the user clicks the Apply button.  This will build up
     * a new MemberBean from the info in the UI *and* the current MemberBean
     * and then fire a change event.
     */
    protected void doApply() {
        cancelButton.setEnabled(false);
        applyButton.setEnabled(false);
        
        MemberBean newValue = new MemberBean();
        newValue.setUserId(this.value.getUserId());
        newValue.setEmail(this.value.getEmail());
        newValue.setJoinedOn(this.value.getJoinedOn());
        newValue.setUserName(this.value.getUserName());
        
        List<MemberRoleBean> newRoles = new ArrayList<MemberRoleBean>();
        Set<String> roleIds = this.editRolesSelector.getValue();
        for (String roleId : roleIds) {
            MemberRoleBean newRole = new MemberRoleBean();
            newRole.setRoleId(roleId);
            newRole.setRoleName(getRoleName(roleId, roleBeans));
            newRoles.add(newRole);
        }
        
        newValue.setRoles(newRoles);
        ValueChangeEvent.fire(this, newValue);
    }
    
    /**
     * Called by the page when changes to the member's roles have been
     * successfully saved.
     */
    public void onApplySuccess(MemberBean value) {
        setValue(value);
        cancelButton.setEnabled(true);
        applyButton.setEnabled(true);
        flipCard();
    }
    
    /**
     * Called by the page when the changes failed to save.  The page is 
     * responsible for displaying an appropriate error message.
     */
    public void onApplyFailed() {
        refresh();
        cancelButton.setEnabled(true);
        applyButton.setEnabled(true);
        flipCard();
    }

    /**
     * Resolves a role name from its id.
     * @param roleId
     * @param fromRoles
     */
    private static String getRoleName(String roleId, List<RoleBean> fromRoles) {
        for (RoleBean roleBean : fromRoles) {
            if (roleBean.getId().equals(roleId)) {
                return roleBean.getName();
            }
        }
        return null;
    }

}
