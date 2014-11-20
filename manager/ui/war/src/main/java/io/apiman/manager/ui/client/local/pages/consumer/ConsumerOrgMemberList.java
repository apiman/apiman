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
package io.apiman.manager.ui.client.local.pages.consumer;

import io.apiman.manager.api.beans.members.MemberBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.UserRedirectPage;
import io.apiman.manager.ui.client.local.services.NavigationHelperService;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * List of members in an organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ConsumerOrgMemberList extends FlowPanel implements HasValue<List<MemberBean>> {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    @Inject
    protected TransitionAnchorFactory<UserRedirectPage> toUserRedirectFactory;
    
    private List<MemberBean> members;

    /**
     * Constructor.
     */
    public ConsumerOrgMemberList() {
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
    @Override
    public void setValue(List<MemberBean> value) {
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
    protected Widget createNoEntitiesWidget() {
        InlineLabel label = new InlineLabel();
        label.setText(i18n.format(AppMessages.NO_MEMBERS_MESSAGE));
        return label;
    }

    /**
     * Creates a single member row.
     * @param bean
     */
    private Widget createRow(MemberBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row = new FlowPanel();
        container.add(row);
        row.getElement().setClassName("row"); //$NON-NLS-1$
        
        createTitle(bean, row);
        
        return container;
    }

    /**
     * Creates the title section of a row.
     * @param bean
     * @param row
     */
    protected void createTitle(MemberBean bean, FlowPanel row) {
        SpanPanel sp = new SpanPanel();
        row.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        
        FontAwesomeIcon icon = new FontAwesomeIcon("user", true); //$NON-NLS-1$
        sp.add(icon);
        icon.getElement().addClassName("icon"); //$NON-NLS-1$
        
        Anchor a = toUserRedirectFactory.get(MultimapUtil.singleItemMap("user", bean.getUserId())); //$NON-NLS-1$
        sp.add(a);
        a.setText(bean.getUserName());
        InlineLabel span = new InlineLabel();
        sp.add(span);
        span.getElement().setClassName("secondary"); //$NON-NLS-1$
        span.setText(" (" + bean.getUserId() + ") "); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
