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
package io.apiman.manager.ui.client.local.pages.org;

import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.PlanRedirectPage;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;
import io.apiman.manager.ui.client.local.services.NavigationHelperService;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
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
 * List of plans in an organization.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class OrgPlanList extends FlowPanel implements HasValue<List<PlanSummaryBean>> {
    
    @Inject
    protected NavigationHelperService navHelper;
    @Inject
    protected TranslationService i18n;
    @Inject
    protected TransitionAnchorFactory<PlanRedirectPage> toPlanRedirectFactory;
    
    private List<PlanSummaryBean> plans;
    private boolean filtered;

    /**
     * Constructor.
     */
    public OrgPlanList() {
        getElement().setClassName("apiman-plans"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<PlanSummaryBean>> handler) {
        return super.addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public List<PlanSummaryBean> getValue() {
        return plans;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<PlanSummaryBean> value) {
        filtered = true;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<PlanSummaryBean> value) {
        filtered = false;
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(List<PlanSummaryBean> value, boolean fireEvents) {
        plans = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (plans != null && !plans.isEmpty()) {
            for (PlanSummaryBean bean : plans) {
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
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_PLANS_IN_ORG_MESSAGE), true);
        else
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_PLANS_IN_ORG_MESSAGE), true);
    }

    /**
     * Creates a single plan row.
     * @param bean
     */
    private Widget createRow(PlanSummaryBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row1 = new FlowPanel();
        container.add(row1);
        row1.getElement().setClassName("row"); //$NON-NLS-1$
        
        createTitle(bean, row1);

        FlowPanel row2 = new FlowPanel();
        container.add(row2);
        row2.getElement().setClassName("row"); //$NON-NLS-1$
        createDescription(bean, row2);
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
    }

    /**
     * Creates the title section of a row.
     * @param bean
     * @param row1
     */
    protected void createTitle(PlanSummaryBean bean, FlowPanel row1) {
        SpanPanel sp = new SpanPanel();
        row1.add(sp);
        sp.getElement().setClassName("title"); //$NON-NLS-1$
        Anchor a = toPlanRedirectFactory.get(MultimapUtil.fromMultiple("org", bean.getOrganizationId(), "plan", bean.getId())); //$NON-NLS-1$ //$NON-NLS-2$
        sp.add(a);
        a.setText(bean.getName());
    }

    /**
     * Creates the description area of the plan listing.
     * @param bean
     * @param row
     */
    protected void createDescription(PlanSummaryBean bean, FlowPanel row) {
        InlineLabel description = new InlineLabel(bean.getDescription());
        row.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }

}
