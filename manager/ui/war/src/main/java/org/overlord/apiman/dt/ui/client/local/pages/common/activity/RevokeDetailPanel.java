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
package org.overlord.apiman.dt.ui.client.local.pages.common.activity;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.apiman.dt.api.beans.audit.AuditEntryBean;
import org.overlord.apiman.dt.api.beans.audit.data.MembershipData;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.UserRedirectPage;
import org.overlord.apiman.dt.ui.client.local.widgets.SpanLabel;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;
import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;


/**
 * Shows more information about a membership revoke activity item.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class RevokeDetailPanel extends AbstractDetailPanel {
    
    @Inject
    TransitionAnchorFactory<UserRedirectPage> userLinkFactory;

    /**
     * Constructor.
     */
    public RevokeDetailPanel() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.common.activity.AbstractDetailPanel#render(org.overlord.apiman.dt.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void render(AuditEntryBean entry) {
        MembershipData data = unmarshal(entry.getData(), MembershipData.class);
        Set<String> roles = data.getRoles();
        if (roles.size() == 1 && roles.iterator().next().equals("*")) { //$NON-NLS-1$
            SpanPanel content = new SpanPanel();
            
            
            SpanLabel p1 = new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_ALL_ROLES));
            p1.getElement().setClassName("emphasis"); //$NON-NLS-1$
            content.add(p1);
            content.add(new SpanLabel(" ")); //$NON-NLS-1$
            content.add(new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_ALL_ROLES_WERE_TAKEN)));
            content.add(new SpanLabel(" ")); //$NON-NLS-1$

            TransitionAnchor<UserRedirectPage> userLink = userLinkFactory.get("user", data.getUserId()); //$NON-NLS-1$
            userLink.setText(data.getUserId());
            content.add(userLink);
            content.add(new SpanLabel(".")); //$NON-NLS-1$

            add(content);
        } else {
            UnorderedListPanel list = new UnorderedListPanel();
            for (String roleId : roles) {
                SpanPanel liContent = new SpanPanel();
    
                SpanLabel role = new SpanLabel(roleId);
                role.getElement().setClassName("emphasis"); //$NON-NLS-1$
                liContent.add(role);
    
                liContent.add(new SpanLabel(" ")); //$NON-NLS-1$
                liContent.add(new SpanLabel(i18n.format(AppMessages.ACTIVITY_DATA_ROLE_WAS_TAKEN)));
                liContent.add(new SpanLabel(" ")); //$NON-NLS-1$
    
                TransitionAnchor<UserRedirectPage> userLink = userLinkFactory.get("user", data.getUserId()); //$NON-NLS-1$
                userLink.setText(data.getUserId());
                liContent.add(userLink);
    
                list.add(liContent);
            }
            add(list);
        }
    }

}
