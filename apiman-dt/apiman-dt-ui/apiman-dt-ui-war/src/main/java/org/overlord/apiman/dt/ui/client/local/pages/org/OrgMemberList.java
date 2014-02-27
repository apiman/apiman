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
import org.overlord.apiman.dt.ui.client.local.pages.UserRedirectPage;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
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
    
    private List<MemberBean> apps;

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
        return apps;
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
        apps = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (apps != null && !apps.isEmpty()) {
            for (MemberBean bean : apps) {
                Widget row = createRow(bean);
                add(row);
            }
        }
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
        InlineLabel description = new InlineLabel(bean.getUserName());
        row2.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
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
        a.setText(bean.getUserId());
    }
    
    /**
     * Creates the 'joined on' section of a row.
     * @param bean
     * @param row1
     */
    protected void createJoinedOn(MemberBean bean, FlowPanel row1) {
        if (bean.getJoinedOn() != null) {
            DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
    
            FlowPanel iconDiv = new FlowPanel();
            row1.add(iconDiv);
            iconDiv.getElement().setClassName("apiman-summaryrow-icon"); //$NON-NLS-1$
            FontAwesomeIcon icon = new FontAwesomeIcon("clock-o", true); //$NON-NLS-1$
            iconDiv.add(icon);
            
            InlineLabel label1 = new InlineLabel("Joined on");
            iconDiv.add(label1);
            label1.getElement().setClassName("title-summary-item"); //$NON-NLS-1$
            InlineLabel label2 = new InlineLabel(format.format(bean.getJoinedOn()));
            iconDiv.add(label2);
            label2.getElement().setClassName("title-summary-item"); //$NON-NLS-1$
        }
    }
}
