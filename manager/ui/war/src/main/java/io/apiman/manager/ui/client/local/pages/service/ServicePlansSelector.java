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
package io.apiman.manager.ui.client.local.pages.service;

import io.apiman.manager.api.beans.services.ServicePlanBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * Shows the user a list of plans that can be chosen for a service.  Choosing
 * a plan allows applications to create contracts with that service via that
 * plan.  Multiple plans can be selected.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class ServicePlansSelector extends FlowPanel implements HasValue<Set<ServicePlanBean>> {

    @Inject
    Instance<ServicePlanWidget> widgetFactory;
    
    private Set<ServicePlanBean> value;
    private List<PlanSummaryBean> plans;
    private Map<PlanSummaryBean, List<PlanVersionSummaryBean>> versions;
    private Map<PlanSummaryBean, ServicePlanWidget> widgets = new HashMap<PlanSummaryBean, ServicePlanWidget>();

    /**
     * Constructor.
     */
    public ServicePlansSelector() {
    }
    
    /**
     * Sets the plan choices.
     * @param plans
     * @param versions
     */
    public void setChoices(List<PlanSummaryBean> plans, Map<PlanSummaryBean, List<PlanVersionSummaryBean>> versions) {
        clear();
        this.plans = plans;
        this.versions = versions;
        refresh();
    }
    
    /**
     * Refresh the UI with the new choices.
     */
    private void refresh() {
        for (final PlanSummaryBean planSummaryBean : this.plans) {
            final ServicePlanWidget planWidget = widgetFactory.get();
            planWidget.setPlanBean(planSummaryBean);
            List<PlanVersionSummaryBean> versionBeans = versions.get(planSummaryBean);
            List<String> planVersions = new ArrayList<String>();
            for (PlanVersionSummaryBean planVersionBean : versionBeans) {
                planVersions.add(planVersionBean.getVersion());
            }
            planWidget.setVersions(planVersions);
            planWidget.setVersion(null);
            widgets.put(planSummaryBean, planWidget);
            planWidget.addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    onItemChange(planSummaryBean, planWidget);
                }
            });
            add(planWidget);
        }
    }

    /**
     * Called when something changes on the plan item.  Possibly the user deselected
     * it or changed the version number.
     * @param planSummaryBean
     * @param planWidget
     */
    protected void onItemChange(PlanSummaryBean planSummaryBean, ServicePlanWidget planWidget) {
        ServicePlanBean spb = new ServicePlanBean();
        spb.setPlanId(planSummaryBean.getId());
        spb.setVersion(planWidget.getVersion());
        value.remove(spb);
        if (planWidget.isSelected()) {
            value.add(spb);
        }
        setValue(value, true);
    }

    /**
     * @see com.google.gwt.user.client.ui.FlowPanel#clear()
     */
    @Override
    public void clear() {
        super.clear();
        plans = null;
        versions = null;
        this.widgets.clear();
    }
    
    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Set<ServicePlanBean>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
    
    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Set<ServicePlanBean> value) {
        setValue(value, false);
        // First deselect all
        for (Entry<PlanSummaryBean, ServicePlanWidget> entry : widgets.entrySet()) {
            ServicePlanWidget widget = entry.getValue();
            widget.deselect();
        }
        // Now select the appropriate values and set their version #s
        for (ServicePlanBean servicePlanBean : value) {
            for (Entry<PlanSummaryBean, ServicePlanWidget> entry : widgets.entrySet()) {
                PlanSummaryBean bean = entry.getKey();
                ServicePlanWidget widget = entry.getValue();
                if (bean.getId().equals(servicePlanBean.getPlanId())) {
                    widget.select();
                    widget.setVersion(servicePlanBean.getVersion());
                }
            }
        }
    }
    
    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Set<ServicePlanBean> value, boolean fireEvents) {
        this.value = value;
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Set<ServicePlanBean> getValue() {
        return value;
    }

}
