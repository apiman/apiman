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
package io.apiman.manager.ui.client.local.pages;

import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.services.ServicePlanBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.service.ServicePlansSelector;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;


/**
 * The "Service" page, with the Plans tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/io/apiman/manager/ui/client/local/site/service-plans.html#page")
@Page(path="service-plans")
@Dependent
public class ServicePlansPage extends AbstractServicePage {
    
    List<PlanSummaryBean> planBeans;
    Map<PlanSummaryBean, List<PlanVersionBean>> planVersions = new HashMap<PlanSummaryBean, List<PlanVersionBean>>();
    
    @Inject @DataField
    ServicePlansSelector plans;
    @Inject @DataField
    AsyncActionButton saveButton;
    @Inject @DataField
    Button cancelButton;

    /**
     * Constructor.
     */
    public ServicePlansPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        plans.addValueChangeHandler(new ValueChangeHandler<Set<ServicePlanBean>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Set<ServicePlanBean>> event) {
                onPlansChange();
            }
        });
    }

    /**
     * Called when the user changes something in the plan list.
     */
    protected void onPlansChange() {
        saveButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }

    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getOrgPlans(org, new IRestInvokerCallback<List<PlanSummaryBean>>() {
            @Override
            public void onSuccess(List<PlanSummaryBean> response) {
                planBeans = response;
                planVersions.clear();
                increaseExpectedDataPackets(response.size());
                dataPacketLoaded();
                for (final PlanSummaryBean planSummaryBean : response) {
                    rest.getPlanVersions(org, planSummaryBean.getId(), new IRestInvokerCallback<List<PlanVersionBean>>() {
                        @Override
                        public void onSuccess(List<PlanVersionBean> response) {
                            planVersions.put(planSummaryBean, response);
                            dataPacketLoaded();
                        }
                        @Override
                        public void onError(Throwable error) {
                            dataPacketError(error);
                        }
                    });
                }
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 1;
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractServicePage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        saveButton.reset();
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        plans.setChoices(planBeans, planVersions);
        Set<ServicePlanBean> theplans = new HashSet<ServicePlanBean>();
        if (versionBean.getPlans() != null) {
            theplans.addAll(versionBean.getPlans());
        }
        plans.setValue(theplans);
    }
    
    /**
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_SERVICE_PLANS, serviceBean.getName());
    }
    
    /**
     * Called when the user clicks the Save button.
     */
    @EventHandler("saveButton")
    protected void onSave(ClickEvent event) {
        saveButton.onActionStarted();
        cancelButton.setEnabled(false);
        
        final Set<ServicePlanBean> newplans = plans.getValue();
        versionBean.setPlans(newplans);
        
        ServiceVersionBean update = new ServiceVersionBean();
        update.setPlans(newplans);
        rest.updateServiceVersion(serviceBean.getOrganization().getId(), serviceBean.getId(),
                versionBean.getVersion(), update, new IRestInvokerCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                saveButton.onActionComplete();
                saveButton.setEnabled(false);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user clicks the Cancel button.
     */
    @EventHandler("cancelButton")
    protected void onCancel(ClickEvent event) {
        saveButton.reset();
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);
        Set<ServicePlanBean> resetValues = new HashSet<ServicePlanBean>();
        if (versionBean.getPlans() != null) {
            resetValues.addAll(versionBean.getPlans());
        }
        plans.setValue(resetValues);
    }

}
