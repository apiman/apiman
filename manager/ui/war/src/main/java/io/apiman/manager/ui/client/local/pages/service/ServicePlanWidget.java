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

import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.ui.client.local.pages.PlanOverviewPage;
import io.apiman.manager.ui.client.local.pages.common.VersionSelectBox;
import io.apiman.manager.ui.client.local.services.NavigationHelperService;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionChangeEvent.HasSelectionChangedHandlers;

/**
 * A single plan in the "available plans" panel.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
@Templated("/io/apiman/manager/ui/client/local/site/service-plans.html#plan-widget")
public class ServicePlanWidget extends Composite implements HasSelectionChangedHandlers {
    
    @Inject
    protected NavigationHelperService navHelper;

    @Inject @DataField
    SimpleCheckBox checkbox;
    @Inject @DataField
    Anchor name;
    @Inject @DataField
    VersionSelectBox versions;
    
    /**
     * Constructor.
     */
    public ServicePlanWidget() {
    }
    
    @PostConstruct
    void postConstruct() {
        checkbox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onSomethingChanged();
            }
        });
        versions.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onSomethingChanged();
            }
        });
    }
    
    /**
     * Called when something changes.  :)
     */
    protected void onSomethingChanged() {
        SelectionChangeEvent.fire(this);
    }

    /**
     * Selects the plan.
     */
    public void select() {
        checkbox.setValue(true);
    }

    /**
     * Deselets this plan.
     */
    public void deselect() {
        this.checkbox.setValue(false);
    }

    /**
     * @return true if the plan is selected
     */
    public boolean isSelected() {
        return checkbox.getValue();
    }
    
    /**
     * @param version sets the version
     */
    public void setVersion(String version) {
        versions.setValue(version);
    }
    
    /**
     * @return the version selected
     */
    public String getVersion() {
        return versions.getValue();
    }
    
    /**
     * @param plan
     */
    public void setPlanBean(PlanSummaryBean plan) {
        name.setText(plan.getName());
        name.setTitle(plan.getDescription());
        name.setHref(navHelper.createHrefToPage(PlanOverviewPage.class, MultimapUtil.fromMultiple(
                "org", plan.getOrganizationId(), "plan", plan.getId()))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Sets the possible versions.  This should be called before 
     * @param versions
     */
    public void setVersions(List<String> versions) {
        this.versions.setOptions(versions);
    }

    /**
     * @see com.google.gwt.view.client.SelectionChangeEvent.HasSelectionChangedHandlers#addSelectionChangeHandler(com.google.gwt.view.client.SelectionChangeEvent.Handler)
     */
    @Override
    public HandlerRegistration addSelectionChangeHandler(Handler handler) {
        return addHandler(handler, SelectionChangeEvent.getType());
    }

}
