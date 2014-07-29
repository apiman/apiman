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
package org.overlord.apiman.dt.ui.client.local.pages.common;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.events.MoveItemDownEvent;
import org.overlord.apiman.dt.ui.client.local.events.MoveItemDownEvent.HasMoveItemDownHandlers;
import org.overlord.apiman.dt.ui.client.local.events.MoveItemUpEvent;
import org.overlord.apiman.dt.ui.client.local.events.MoveItemUpEvent.HasMoveItemUpHandlers;
import org.overlord.apiman.dt.ui.client.local.events.RemovePolicyEvent;
import org.overlord.apiman.dt.ui.client.local.events.RemovePolicyEvent.HasRemovePolicyHandlers;
import org.overlord.apiman.dt.ui.client.local.pages.EditPolicyPage;
import org.overlord.apiman.dt.ui.client.local.pages.UserRedirectPage;
import org.overlord.apiman.dt.ui.client.local.services.NavigationHelperService;
import org.overlord.apiman.dt.ui.client.local.util.Formatting;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a list of policies on the Policies tab of the App, Service, and Plan pages.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class PolicyList extends FlowPanel implements HasValue<List<PolicyBean>>, HasRemovePolicyHandlers,
        HasMoveItemDownHandlers, HasMoveItemUpHandlers {

    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    @Inject
    TransitionAnchorFactory<EditPolicyPage> toEditPolicyFactory;
    @Inject
    TransitionAnchorFactory<UserRedirectPage> toUserFactory;
    
    private List<PolicyBean> policies;
    private boolean filtered;
    
    /**
     * Constructor.
     */
    public PolicyList() {
        getElement().setClassName("apiman-policies"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<PolicyBean>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.MoveItemDownEvent.HasMoveItemDownHandlers#addMoveItemDownHandler(org.overlord.apiman.dt.ui.client.local.events.MoveItemDownEvent.Handler)
     */
    @Override
    public HandlerRegistration addMoveItemDownHandler(MoveItemDownEvent.Handler handler) {
        return super.addHandler(handler, MoveItemDownEvent.getType());
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.MoveItemUpEvent.HasMoveItemUpHandlers#addMoveItemUpHandler(org.overlord.apiman.dt.ui.client.local.events.MoveItemUpEvent.Handler)
     */
    @Override
    public HandlerRegistration addMoveItemUpHandler(MoveItemUpEvent.Handler handler) {
        return super.addHandler(handler, MoveItemUpEvent.getType());
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.events.RemovePolicyEvent.HasRemovePolicyHandlers#addRemovePolicyHandler(org.overlord.apiman.dt.ui.client.local.events.RemovePolicyEvent.Handler)
     */
    @Override
    public HandlerRegistration addRemovePolicyHandler(RemovePolicyEvent.Handler handler) {
        return super.addHandler(handler, RemovePolicyEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<PolicyBean> getValue() {
        return policies;
    }
    
    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<PolicyBean> value) {
        filtered = true;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<PolicyBean> value) {
        filtered = false;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<PolicyBean> value, boolean fireEvents) {
        policies = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (policies != null && !policies.isEmpty()) {
            for (PolicyBean bean : policies) {
                Widget row = createPolicyRow(bean);
                add(row);
            }
        } else {
            add(createNoEntitiesWidget());
        }
    }

    /**
     * @return a widget to show when there are no entities.
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered())
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_POLICIES_MESSAGE), true);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_POLICIES_MESSAGE), true);
    }

    /**
     * Creates a single policy row.
     * @param bean
     */
    private Widget createPolicyRow(PolicyBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row = new FlowPanel();
        container.add(row);
        row.getElement().setClassName("row"); //$NON-NLS-1$
        
        createIconColumn(bean, row);
        createSummaryColumn(bean, row);
        createActionColumn(bean, row);
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
    }

    /**
     * Creates the icon column.
     * @param bean
     * @param row
     */
    protected void createIconColumn(PolicyBean bean, FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.setStyleName("col-md-1"); //$NON-NLS-1$
        col.addStyleName("col-no-padding"); //$NON-NLS-1$
        
        FontAwesomeIcon icon = new FontAwesomeIcon(bean.getDefinition().getIcon(), true);
        icon.getElement().addClassName("movable"); //$NON-NLS-1$
        icon.getElement().addClassName("apiman-policy-icon"); //$NON-NLS-1$
        col.add(icon);
    }

    /**
     * Creates the summary column.
     * @param bean
     * @param row
     */
    protected void createSummaryColumn(final PolicyBean bean, FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.setStyleName("col-md-9"); //$NON-NLS-1$
        col.addStyleName("col-no-padding"); //$NON-NLS-1$
        
        FlowPanel titleDiv = new FlowPanel();
        titleDiv.getElement().setClassName(""); //$NON-NLS-1$
        col.add(titleDiv);
        
        SpanPanel titleSpan = new SpanPanel();
        titleDiv.add(titleSpan);
        titleSpan.getElement().setClassName("title"); //$NON-NLS-1$

        TransitionAnchor<EditPolicyPage> titleAnchor = toEditPolicyFactory.get(MultimapUtil.fromMultiple(
                "org", bean.getOrganizationId(), //$NON-NLS-1$
                "id", bean.getEntityId(), //$NON-NLS-1$
                "ver", bean.getEntityVersion(), //$NON-NLS-1$
                "type", bean.getType().name(), //$NON-NLS-1$
                "policy", String.valueOf(bean.getId()) //$NON-NLS-1$
        ));
        titleSpan.add(titleAnchor);
        titleAnchor.setText(bean.getName());
        
        FlowPanel metaData = new FlowPanel();
        col.add(metaData);
        metaData.setStyleName("metaData"); //$NON-NLS-1$
        metaData.add(new InlineLabel(i18n.format(AppMessages.POLICY_CREATED_BY) + " ")); //$NON-NLS-1$
        SpanPanel sp = new SpanPanel();
        metaData.add(sp);
        
        Anchor a = toUserFactory.get("user", bean.getCreatedBy()); //$NON-NLS-1$
        sp.add(a);
        a.setText(bean.getCreatedBy());
        
        metaData.add(new InlineLabel(" " + i18n.format(AppMessages.ON) + " ")); //$NON-NLS-1$ //$NON-NLS-2$

        FontAwesomeIcon icon = new FontAwesomeIcon("clock-o", true); //$NON-NLS-1$
        metaData.add(icon);
        icon.getElement().addClassName("fa-inline"); //$NON-NLS-1$
        
        metaData.add(new InlineLabel(Formatting.formatShortDate(bean.getCreatedOn())));
        
        Label description = new Label(bean.getDescription());
        col.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
        description.getElement().addClassName("apiman-label-faded"); //$NON-NLS-1$
    }
    
    /**
     * Creates the action column for the single policy row.
     * @param bean
     * @param row
     */
    protected void createActionColumn(final PolicyBean bean, FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.setStyleName("col-md-2"); //$NON-NLS-1$
        col.addStyleName("col-no-padding"); //$NON-NLS-1$
        
        SpanPanel sp = new SpanPanel();
        col.add(sp);
        sp.getElement().setClassName("actions"); //$NON-NLS-1$
        final AsyncActionButton aab = new AsyncActionButton();
        aab.getElement().setClassName("btn"); //$NON-NLS-1$
        aab.getElement().addClassName("btn-default"); //$NON-NLS-1$
        aab.setHTML(i18n.format(AppMessages.REMOVE));
        aab.setActionText(i18n.format(AppMessages.REMOVING));
        aab.setIcon("fa-cog"); //$NON-NLS-1$
        aab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm(i18n.format(AppMessages.CONFIRM_REMOVE_POLICY, bean.getName()))) {
                    aab.onActionStarted();
                    RemovePolicyEvent.fire(PolicyList.this, bean);
                }
            }
        });
        sp.add(aab);
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }

}
