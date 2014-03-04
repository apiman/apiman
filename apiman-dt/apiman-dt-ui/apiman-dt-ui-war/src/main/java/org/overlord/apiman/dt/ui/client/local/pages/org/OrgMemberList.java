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

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.apiman.dt.api.beans.members.MemberBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.UserRedirectPage;
import org.overlord.apiman.dt.ui.client.local.pages.common.NoEntitiesWidget;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.apiman.dt.ui.client.local.util.Formatting;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * List of applications in an organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OrgMemberList extends FlowPanel implements HasValue<List<MemberBean>> {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    @Inject
    protected TransitionAnchorFactory<UserRedirectPage> toUserRedirectFactory;
    
    private List<MemberBean> members;
    private boolean filtered;

    /**
     * Constructor.
     */
    public OrgMemberList() {
        getElement().setClassName("apiman-members"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<MemberBean>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<MemberBean> getValue() {
        return members;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<MemberBean> value) {
        filtered = true;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<MemberBean> value) {
        filtered = false;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<MemberBean> value, boolean fireEvents) {
        members = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (members != null && !members.isEmpty()) {
            for (MemberBean bean : members) {
                Widget row = createRow(bean);
                add(row);
            }
        } else {
            add(createNoEntitiesWidget());
        }
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered())
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_MEMBERS_IN_ORG_MESSAGE), true);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_MEMBERS_IN_ORG_MESSAGE), true);
    }

    /**
     * Creates a single application row.
     * @param bean
     */
    private Widget createRow(MemberBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row1 = new FlowPanel();
        container.add(row1);
        row1.getElement().setClassName("row"); //$NON-NLS-1$
        
        createTitle(bean, row1);
        createJoinedOn(bean, row1);

        FlowPanel row2 = new FlowPanel();
        container.add(row2);
        row2.getElement().setClassName("row"); //$NON-NLS-1$
        createDescription(bean, row2);
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
    }

    /**
     * Creates the description area of the member listing.
     * @param bean
     * @param row2
     */
    protected void createDescription(MemberBean bean, FlowPanel row2) {
        InlineLabel description = new InlineLabel(Formatting.formatRoles(bean));
        row2.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
    }

    /**
     * Creates the title section of a row.
     * @param bean
     * @param row1
     */
    protected void createTitle(MemberBean bean, FlowPanel row1) {
        SpanPanel sp = new SpanPanel();
        row1.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        Anchor a = toUserRedirectFactory.get(MultimapUtil.singleItemMap("user", bean.getUserId())); //$NON-NLS-1$
        sp.add(a);
        a.setText(bean.getUserName());
        InlineLabel span = new InlineLabel();
        sp.add(span);
        span.getElement().setClassName("secondary"); //$NON-NLS-1$
        span.setText(" (" + bean.getUserId() + ") "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * Creates the 'joined on' section of a row.
     * @param bean
     * @param row1
     */
    protected void createJoinedOn(MemberBean bean, FlowPanel row1) {
        if (bean.getJoinedOn() != null) {
            FlowPanel iconDiv = new FlowPanel();
            row1.add(iconDiv);
            iconDiv.getElement().setClassName("apiman-summaryrow-icon"); //$NON-NLS-1$
            FontAwesomeIcon icon = new FontAwesomeIcon("clock-o", true); //$NON-NLS-1$
            iconDiv.add(icon);
            
            InlineLabel label1 = new InlineLabel(i18n.format(AppMessages.JOINED_ON));
            iconDiv.add(label1);
            label1.getElement().setClassName("title-summary-item"); //$NON-NLS-1$
            InlineLabel label2 = new InlineLabel(Formatting.formatShortDate(bean.getJoinedOn()));
            iconDiv.add(label2);
            label2.getElement().setClassName("title-summary-item"); //$NON-NLS-1$
        }
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }

}
