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
package io.apiman.manager.ui.client.local.pages.admin;

import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.common.NoEntitiesWidget;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.overlord.commons.gwt.client.local.widgets.FontAwesomeIcon;
import org.overlord.commons.gwt.client.local.widgets.SpanPanel;
import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * A table of policy definitions.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class PolicyDefinitionTable extends TemplatedWidgetTable implements TakesValue<List<PolicyDefinitionSummaryBean>> {

    @Inject
    protected TranslationService i18n;

    private List<PolicyDefinitionSummaryBean> policyDefs;
    private boolean filtered;

    /**
     * Constructor.
     */
    public PolicyDefinitionTable() {
    }
    
    /**
     * Filtered version of setValue().
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    public void setFilteredValue(List<PolicyDefinitionSummaryBean> value) {
        filtered = true;
        policyDefs = value;
        clear();
        refresh();
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(List<PolicyDefinitionSummaryBean> value) {
        filtered = false;
        policyDefs = value;
        clear();
        refresh();
    }

    /**
     * Refresh the display with the current value.
     */
    public void refresh() {
        if (policyDefs != null && !policyDefs.isEmpty()) {
            int rowIdx = 0;
            for (PolicyDefinitionSummaryBean bean : policyDefs) {
                addRow(rowIdx++, bean);
            }
        } else {
            Element tdElement = add(0, 0, createNoEntitiesWidget());
            tdElement.setAttribute("colspan", "2"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Adds a row to the table.
     * @param rowIdx
     * @param bean
     */
    private void addRow(int rowIdx, PolicyDefinitionSummaryBean bean) {
        SpanPanel s = new SpanPanel();
        FontAwesomeIcon icon = new FontAwesomeIcon(bean.getIcon(), true);
        s.add(icon);
        InlineLabel span = new InlineLabel(bean.getName());
        s.add(span);
        add(rowIdx, 0, s);
        
        add(rowIdx, 1, new InlineLabel(bean.getPolicyImpl()));
    }

    /**
     * @return a widget to display when no items are found
     */
    protected NoEntitiesWidget createNoEntitiesWidget() {
        if (isFiltered()) {
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_FILTERED_POLICY_DEFS_ADMIN_MESSAGE), false);
        } else {
            return new NoEntitiesWidget(i18n.format(AppMessages.NO_POLICY_DEFS_ADMIN_MESSAGE), false);
        }
    }

    /**
     * @see com.google.gwt.user.client.TakesValue#getValue()
     */
    @Override
    public List<PolicyDefinitionSummaryBean> getValue() {
        return policyDefs;
    }

    /**
     * @return the filtered
     */
    protected boolean isFiltered() {
        return filtered;
    }
}
