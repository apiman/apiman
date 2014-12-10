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

import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Models a list of policies for a given plan on the consumer service page.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class PolicyChain extends FlowPanel implements TakesValue<List<PolicySummaryBean>> {

    @Inject
    protected TranslationService i18n;

    private List<PolicySummaryBean> policies;
    
    /**
     * Constructor.
     */
    public PolicyChain() {
        getElement().setClassName("apiman-policies"); //$NON-NLS-1$
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public List<PolicySummaryBean> getValue() {
        return policies;
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<PolicySummaryBean> value) {
        policies = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (policies != null && !policies.isEmpty()) {
            for (PolicySummaryBean bean : policies) {
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
        return new NoEntitiesWidget(i18n.format(AppMessages.EMPTY_POLICY_CHAIN_MESSAGE), false);
    }

    /**
     * Creates a single policy row.
     * @param bean
     */
    private Widget createPolicyRow(PolicySummaryBean bean) {
        FlowPanel container = new FlowPanel();
        container.getElement().setClassName("container-fluid"); //$NON-NLS-1$
        container.getElement().addClassName("apiman-summaryrow"); //$NON-NLS-1$
        
        FlowPanel row = new FlowPanel();
        container.add(row);
        row.getElement().setClassName("row"); //$NON-NLS-1$
        
        createIconColumn(bean, row);
        createSummaryColumn(bean, row);
        
        container.add(new HTMLPanel("<hr/>")); //$NON-NLS-1$
        
        return container;
    }

    /**
     * Creates the icon column.
     * @param bean
     * @param row
     */
    protected void createIconColumn(PolicySummaryBean bean, FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.setStyleName("col-md-1"); //$NON-NLS-1$
        col.addStyleName("col-no-padding"); //$NON-NLS-1$
        
        FontAwesomeIcon icon = new FontAwesomeIcon(bean.getIcon(), true);
        icon.getElement().addClassName("apiman-policy-icon"); //$NON-NLS-1$
        col.add(icon);
    }

    /**
     * Creates the summary column.
     * @param bean
     * @param row
     */
    protected void createSummaryColumn(final PolicySummaryBean bean, FlowPanel row) {
        FlowPanel col = new FlowPanel();
        row.add(col);
        col.setStyleName("col-md-11"); //$NON-NLS-1$
        col.addStyleName("col-no-padding"); //$NON-NLS-1$
        
        FlowPanel titleDiv = new FlowPanel();
        titleDiv.getElement().setClassName(""); //$NON-NLS-1$
        col.add(titleDiv);
        
        InlineLabel titleSpan = new InlineLabel(bean.getName());
        titleDiv.add(titleSpan);
        titleSpan.getElement().setClassName("title"); //$NON-NLS-1$
        titleSpan.getElement().addClassName("apiman-label-faded"); //$NON-NLS-1$
        
        Label description = new Label(bean.getDescription());
        col.add(description);
        description.getElement().setClassName("description"); //$NON-NLS-1$
        description.getElement().addClassName("apiman-label-faded"); //$NON-NLS-1$
    }

}
