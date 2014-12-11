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
package io.apiman.manager.ui.client.local.pages.common;

import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.events.ConfirmationEvent;
import io.apiman.manager.ui.client.local.events.PoliciesReorderedEvent;
import io.apiman.manager.ui.client.local.events.PoliciesReorderedEvent.Handler;
import io.apiman.manager.ui.client.local.events.PoliciesReorderedEvent.HasPoliciesReorderedHandlers;
import io.apiman.manager.ui.client.local.events.RemovePolicyEvent;
import io.apiman.manager.ui.client.local.events.RemovePolicyEvent.HasRemovePolicyHandlers;
import io.apiman.manager.ui.client.local.pages.EditPolicyPage;
import io.apiman.manager.ui.client.local.pages.UserRedirectPage;
import io.apiman.manager.ui.client.local.services.LoggerService;
import io.apiman.manager.ui.client.local.services.NavigationHelperService;
import io.apiman.manager.ui.client.local.util.Formatting;
import io.apiman.manager.ui.client.local.util.MultimapUtil;
import io.apiman.manager.ui.client.local.widgets.ConfirmationDialog;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchor;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
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
public abstract class PolicyList extends FlowPanel implements HasValue<List<PolicySummaryBean>>, HasRemovePolicyHandlers,
        HasPoliciesReorderedHandlers {

    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    @Inject
    protected TransitionAnchorFactory<EditPolicyPage> toEditPolicyFactory;
    @Inject
    protected TransitionAnchorFactory<UserRedirectPage> toUserFactory;
    @Inject
    protected LoggerService logger;
    @Inject
    Instance<ConfirmationDialog> confirmationDialogFactory;

    private boolean filtered;
    private boolean empty;
    private DropPlaceholder dropHolder;
    
    private String organizationId;
    private String entityId;
    private String entityVersion;
    private PolicyType type;
    
    /**
     * Constructor.
     */
    public PolicyList() {
        getElement().setClassName("apiman-policies"); //$NON-NLS-1$
    }
    
    /**
     * Sets some info about the entity we're displaying policies for.
     * @param organizationId
     * @param entityId
     * @param entityVersion
     * @param type
     */
    public void setEntityInfo(String organizationId, String entityId, String entityVersion, PolicyType type) {
        this.organizationId = organizationId;
        this.entityId = entityId;
        this.entityVersion = entityVersion;
        this.type = type;
    }
    
    @PostConstruct
    protected void postConstruct() {
        dropHolder = new DropPlaceholder();
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<PolicySummaryBean>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.events.PoliciesReorderedEvent.HasPoliciesReorderedHandlers#addPoliciesReorderedHandler(io.apiman.manager.ui.client.local.events.PoliciesReorderedEvent.Handler)
     */
    @Override
    public HandlerRegistration addPoliciesReorderedHandler(Handler handler) {
        return super.addHandler(handler, PoliciesReorderedEvent.getType());
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.events.RemovePolicyEvent.HasRemovePolicyHandlers#addRemovePolicyHandler(io.apiman.manager.ui.client.local.events.RemovePolicyEvent.Handler)
     */
    @Override
    public HandlerRegistration addRemovePolicyHandler(RemovePolicyEvent.Handler handler) {
        return super.addHandler(handler, RemovePolicyEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<PolicySummaryBean> getValue() {
        List<PolicySummaryBean> policies = new ArrayList<PolicySummaryBean>();
        if (!empty) {
            for (int i = 0; i < getWidgetCount(); i++) {
                PolicyRow pr = (PolicyRow) getWidget(i);
                PolicySummaryBean policy = pr.getPolicy();
                policies.add(policy);
            }
        }
        return policies;
    }
    
    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<PolicySummaryBean> value) {
        filtered = true;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<PolicySummaryBean> value) {
        filtered = false;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<PolicySummaryBean> value, boolean fireEvents) {
        clear();
        refresh(value);
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh(List<PolicySummaryBean> policies) {
        if (policies != null && !policies.isEmpty()) {
            for (PolicySummaryBean bean : policies) {
                Widget row = createPolicyRow(bean);
                add(row);
            }
            empty = false;
        } else {
            empty = true;
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
    private Widget createPolicyRow(final PolicySummaryBean bean) {
        PolicyRow container = new PolicyRow(bean);
        
        final FlowPanel row = new FlowPanel();
        row.getElement().setClassName("row"); //$NON-NLS-1$

        // Grabber
        Label grabber = new Label();
        grabber.getElement().setAttribute("data-permission", getRemovePermission()); //$NON-NLS-1$
        grabber.getElement().setDraggable(Element.DRAGGABLE_TRUE);
        grabber.getElement().setClassName("grabber"); //$NON-NLS-1$
        grabber.getElement().getStyle().setHeight(48, Unit.PX);
        row.add(grabber);

        createIconColumn(bean, row);
        createSummaryColumn(bean, row);
        createActionColumn(bean, row);
        
        container.add(row);
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$

        PolicyDragHandler handler = new PolicyDragHandler(grabber, container);
        grabber.addMouseDownHandler(handler);
        grabber.addMouseUpHandler(handler);
        grabber.addMouseMoveHandler(handler);
        
        return container;
    }

    /**
     * Creates the icon column.
     * @param bean
     * @param row
     */
    protected FlowPanel createIconColumn(final PolicySummaryBean bean, final FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.setStyleName("col"); //$NON-NLS-1$
        
        FontAwesomeIcon icon = new FontAwesomeIcon(bean.getIcon(), true);
        icon.getElement().addClassName("apiman-policy-icon"); //$NON-NLS-1$
        col.add(icon);
        
        return col;
    }

    /**
     * Creates the summary column.
     * @param bean
     * @param row
     */
    protected void createSummaryColumn(final PolicySummaryBean bean, FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.getElement().setClassName("col"); //$NON-NLS-1$
        col.getElement().addClassName("col-70"); //$NON-NLS-1$
        
        FlowPanel titleDiv = new FlowPanel();
        titleDiv.getElement().setClassName(""); //$NON-NLS-1$
        col.add(titleDiv);
        
        SpanPanel titleSpan = new SpanPanel();
        titleDiv.add(titleSpan);
        titleSpan.getElement().setClassName("title"); //$NON-NLS-1$

        TransitionAnchor<EditPolicyPage> titleAnchor = toEditPolicyFactory.get(MultimapUtil.fromMultiple(
                "org", this.organizationId, //$NON-NLS-1$
                "id", this.entityId, //$NON-NLS-1$
                "ver", this.entityVersion, //$NON-NLS-1$
                "type", this.type.name(), //$NON-NLS-1$
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
    protected void createActionColumn(final PolicySummaryBean bean, FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.setStyleName("col"); //$NON-NLS-1$
        col.addStyleName("pull-right"); //$NON-NLS-1$
        
        SpanPanel sp = new SpanPanel();
        col.add(sp);
        sp.getElement().setClassName("actions"); //$NON-NLS-1$
        final AsyncActionButton aab = new AsyncActionButton();
        aab.getElement().setClassName("btn"); //$NON-NLS-1$
        aab.getElement().addClassName("btn-default"); //$NON-NLS-1$
        aab.getElement().setAttribute("data-permission", getRemovePermission()); //$NON-NLS-1$
        aab.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        aab.setHTML(i18n.format(AppMessages.REMOVE));
        aab.setActionText(i18n.format(AppMessages.REMOVING));
        aab.setIcon("fa-cog"); //$NON-NLS-1$
        aab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                aab.onActionStarted();
                ConfirmationDialog dialog = confirmationDialogFactory.get();
                dialog.setDialogTitle(i18n.format(AppMessages.CONFIRM_REMOVE_POLICY_TITLE));
                dialog.setDialogMessage(i18n.format(AppMessages.CONFIRM_REMOVE_POLICY_MESSAGE, bean.getName()));
                dialog.addConfirmationHandler(new ConfirmationEvent.Handler() {
                    @Override
                    public void onConfirmation(ConfirmationEvent event) {
                        if (event.isConfirmed()) {
                            RemovePolicyEvent.fire(PolicyList.this, bean);
                        } else {
                            aab.onActionComplete();
                            aab.getElement().getStyle().clearVisibility();
                        }
                    }
                });
                dialog.show();
            }
        });
        sp.add(aab);
    }

    /**
     * @return the permission required for the user to be allowed to remove a policy
     */
    protected abstract String getRemovePermission();

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }
    
    /**
     * Called when the user begins dragging a policy.
     * @param event
     * @param row
     */
    protected void onStartDragging(MouseEvent<?> event, PolicyRow row) {
        int index = getWidgetIndex(row);
        insert(dropHolder, index);
        
        // put the row at the end of the list and then fix its position
        remove(row);
        add(row);
        row.getElement().getStyle().setPosition(Position.FIXED);
        row.getElement().getStyle().setWidth(getElement().getClientWidth(), Unit.PX);
        row.getElement().getStyle().setOpacity(0.85);
        row.getElement().getStyle().setBackgroundColor("white"); //$NON-NLS-1$
        row.getElement().getStyle().setLeft(event.getClientX(), Unit.PX);
    }

    /**
     * Called when the user drags a policy.
     * @param event
     * @param row
     */
    protected void onDragging(MouseEvent<?> event, PolicyRow row) {
        row.getElement().getStyle().setTop(event.getClientY() - 20, Unit.PX);
        
        Widget w = getHoverWidget(event, row);
        if (w == null) {
            Widget firstRow = getWidget(0);
            Widget lastRow = getWidget(getWidgetCount() - 1);
            int top = firstRow.getAbsoluteTop();
            int y = event.getClientY();
            if (y <= top) {
                w = firstRow;
            } else {
                w = lastRow;
            }
        }
        if (w != dropHolder) {
            int index = getWidgetIndex(w);
            remove(dropHolder);
            insert(dropHolder, index);
        }
    }

    /**
     * Figures out which widget in the list is being hovered over (using
     * only the Y coordinate of the event.
     * @param event
     * @param dragRow
     */
    private Widget getHoverWidget(MouseEvent<?> event, PolicyRow dragRow) {
        int y = event.getClientY();
        for (int i = 0; i < getWidgetCount(); i++) {
            Widget widget = getWidget(i);
            if (widget == dragRow) {
                continue;
            }
            int widgetTop = widget.getAbsoluteTop();
            int widgetBottom = widgetTop + widget.getOffsetHeight();
            if (y >= widgetTop && y <= widgetBottom) {
                return widget;
            }
        }
        return null;
    }

    /**
     * Called when the user drops a policy.
     * @param event
     * @param row
     */
    protected void onDrop(MouseEvent<?> event, PolicyRow row) {
        row.getElement().getStyle().clearLeft();
        row.getElement().getStyle().clearTop();
        row.getElement().getStyle().clearPosition();
        row.getElement().getStyle().clearOpacity();
        row.getElement().getStyle().clearBackgroundColor();
        
        int dropIndex = getWidgetIndex(dropHolder);
        remove(dropHolder);
        remove(row);
        if (dropIndex >= getWidgetCount()) {
            add(row);
        } else {
            insert(row, dropIndex);
        }
        
        PoliciesReorderedEvent.fire(this);
    }
    
    /**
     * A single row in the list.
     */
    private class PolicyRow extends FlowPanel {
        
        private PolicySummaryBean policy;
        
        /**
         * Constructor.
         */
        public PolicyRow(PolicySummaryBean policy) {
            setPolicy(policy);
            getElement().setClassName("container-fluid"); //$NON-NLS-1$
            getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        }

        /**
         * @return the policy
         */
        public PolicySummaryBean getPolicy() {
            return policy;
        }

        /**
         * @param policy the policy to set
         */
        public void setPolicy(PolicySummaryBean policy) {
            this.policy = policy;
        }
    }
    
    /**
     * A simple widget showing where a policy would be placed if the
     * user were to drop it.
     *
     * @author eric.wittmann@redhat.com
     */
    private class DropPlaceholder extends FlowPanel {
        
        /**
         * Constructor.
         */
        public DropPlaceholder() {
            getElement().setClassName("container-fluid"); //$NON-NLS-1$
            getElement().addClassName("drop-target"); //$NON-NLS-1$
            
            add(new Label(i18n.format(AppMessages.MOVE_POLICY)));
        }
        
    }
    
    /**
     * A simple drag handler used for dragging around a policy to re-order it.
     */
    private class PolicyDragHandler implements MouseDownHandler, MouseMoveHandler, MouseUpHandler {

        private boolean isClicked;
        private boolean isDragging;
        private Widget dragTarget;
        private PolicyRow dragRow;

        /**
         * Constructor.
         * @param widget
         * @param row
         * @param policy
         */
        public PolicyDragHandler(Widget widget, PolicyRow row) {
            dragTarget = widget;
            dragRow = row;
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
         */
        @Override
        public void onMouseDown(MouseDownEvent event) {
            if (getWidgetCount() <= 1) {
                return;
            }
            isClicked = true;
            isDragging = false;

            // Capture mouse and prevent event from going up
            event.preventDefault();
            Event.setCapture(dragTarget.getElement());
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt.event.dom.client.MouseUpEvent)
         */
        @Override
        public void onMouseUp(MouseUpEvent event) {
            if (isDragging) {
                onDrop(event, dragRow);
            }

            isClicked = false;
            isDragging = false;
            Event.releaseCapture(dragTarget.getElement());
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseMoveHandler#onMouseMove(com.google.gwt.event.dom.client.MouseMoveEvent)
         */
        @Override
        public void onMouseMove(MouseMoveEvent event) {
            // If mouse is not down, ignore
            if (!isClicked)
                return;
            if (!isDragging) {
                isDragging = true;
                onStartDragging(event, dragRow);
            }
            
            onDragging(event, dragRow);
        }
    }

}
