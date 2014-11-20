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
package io.apiman.manager.ui.client.local.pages.common.activity;

import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.data.MembershipData;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.UserRedirectPage;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;
import org.overlord.commons.gwt.client.local.widgets.UnorderedListPanel;

import com.google.gwt.user.client.ui.InlineLabel;


/**
 * Shows more information about a membership grant activity item.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class GrantDetailPanel extends AbstractDetailPanel {
    
    @Inject
    TransitionAnchorFactory<UserRedirectPage> userLinkFactory;

    /**
     * Constructor.
     */
    public GrantDetailPanel() {
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.common.activity.AbstractDetailPanel#render(io.apiman.manager.api.beans.audit.AuditEntryBean)
     * 
     *   <ul>
     *     <li><a href="">Bruce Wayne</a> <span>now has role</span> <span class="emphasis">Application Developer</span></li>
     *     <li><a href="">Bruce Wayne</a> <span>now has role</span> <span class="emphasis">Service Provider</span></li>
     *   </ul>
     */
    @Override
    public void render(AuditEntryBean entry) {
        MembershipData data = unmarshal(entry.getData(), MembershipData.class);
        UnorderedListPanel list = new UnorderedListPanel();
        Set<String> roles = data.getRoles();
        for (String roleId : roles) {
            SpanPanel liContent = new SpanPanel();
            TransitionAnchor<UserRedirectPage> userLink = userLinkFactory.get("user", data.getUserId()); //$NON-NLS-1$
            userLink.setText(data.getUserId());
            liContent.add(userLink);
            liContent.add(new InlineLabel(" ")); //$NON-NLS-1$
            liContent.add(new InlineLabel(i18n.format(AppMessages.ACTIVITY_DATA_WAS_GIVEN_ROLE)));
            liContent.add(new InlineLabel(" ")); //$NON-NLS-1$
            InlineLabel role = new InlineLabel(roleId);
            role.getElement().setClassName("emphasis"); //$NON-NLS-1$
            liContent.add(role);
            list.add(liContent);
        }
        
        add(list);
    }

}
