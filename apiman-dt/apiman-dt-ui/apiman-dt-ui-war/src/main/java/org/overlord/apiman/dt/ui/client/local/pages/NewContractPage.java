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
package org.overlord.apiman.dt.ui.client.local.pages;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageShown;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.contracts.ContractBean;
import org.overlord.apiman.dt.api.beans.contracts.NewContractBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaFilterBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ApplicationSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServicePlanSummaryBean;
import org.overlord.apiman.dt.api.beans.summary.ServiceSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.VersionSelectBox;
import org.overlord.apiman.dt.ui.client.local.pages.contract.ApplicationSelectBox;
import org.overlord.apiman.dt.ui.client.local.pages.contract.PlanSelectBox;
import org.overlord.apiman.dt.ui.client.local.pages.contract.ServiceSelector;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;
import org.overlord.commons.gwt.client.local.widgets.AsyncActionButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextBox;


/**
 * Page that lets the user create a new Contract.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/new-contract.html#page")
@Page(path="new-contract")
@Dependent
public class NewContractPage extends AbstractPage {
   
    private static final String APP_ROW = "application-row"; //$NON-NLS-1$
    private static final String APP_VERSION_ROW = "application-version-row"; //$NON-NLS-1$
    private static final String SERVICE_ROW = "service-row"; //$NON-NLS-1$
    private static final String SERVICE_VERSION_ROW = "service-version-row"; //$NON-NLS-1$
    private static final String PLAN_ROW = "plan-row"; //$NON-NLS-1$
    private static final String SPINNER_ROW = "spinner-row"; //$NON-NLS-1$
    
    @PageState
    String svcorg;
    @PageState
    String svc;
    @PageState
    String svcv;
    @PageState
    String apporg;
    @PageState
    String app;
    @PageState
    String appv;
    
    @Inject
    TransitionTo<AppContractsPage> toApp;
    
    @Inject @DataField
    ApplicationSelectBox applicationSelector;
    @Inject @DataField
    VersionSelectBox applicationVersion;
    @Inject @DataField
    TextBox searchBox;
    @Inject @DataField
    AsyncActionButton searchButton;
    @Inject @DataField
    ServiceSelector services;
    @Inject @DataField
    VersionSelectBox serviceVersion;
    @Inject @DataField
    PlanSelectBox plan;
    
    @Inject @DataField
    AsyncActionButton createButton;
    @Inject @DataField
    Anchor cancelButton;
    
    private List<ApplicationSummaryBean> applicationBeans;
    
    /**
     * Constructor.
     */
    public NewContractPage() {
    }

    @PostConstruct
    protected void postConstruct() {
        applicationSelector.addValueChangeHandler(new ValueChangeHandler<ApplicationSummaryBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<ApplicationSummaryBean> event) {
                onApplicationSelected();
            }
        });
        applicationVersion.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onApplicationVersionSelected();
            }
        });
        searchBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                boolean enterPressed = KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode();
                if (enterPressed) {
                    onSearch(null);
                }
            }
        });
        services.addValueChangeHandler(new ValueChangeHandler<ServiceSummaryBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<ServiceSummaryBean> event) {
                onServiceSelected();
            }
        });
        serviceVersion.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                onServiceVersionSelected();
            }
        });
        plan.addValueChangeHandler(new ValueChangeHandler<ServicePlanSummaryBean>() {
            @Override
            public void onValueChange(ValueChangeEvent<ServicePlanSummaryBean> event) {
                onPlanSelected();
            }
        });
    }

    /**
     * Called when the user selects an application.
     */
    protected void onApplicationSelected() {
        hideRows(APP_VERSION_ROW, SERVICE_ROW, SERVICE_VERSION_ROW, PLAN_ROW);
        showRow(SPINNER_ROW);
        
        searchBox.setValue(""); //$NON-NLS-1$
        services.clear();
        
        ApplicationSummaryBean app = applicationSelector.getValue();
        if (app != null) {
            rest.getApplicationVersions(app.getOrganizationId(), app.getId(), new IRestInvokerCallback<List<ApplicationVersionBean>>() {
                @Override
                public void onSuccess(List<ApplicationVersionBean> response) {
                    List<String> versions = new ArrayList<String>(response.size());
                    String contextVersion = null;
                    for (ApplicationVersionBean avb : response) {
                        String avbVersion = avb.getVersion();
                        versions.add(avbVersion);
                        if (avb.getApplication().getOrganizationId().equals(apporg)
                                && avb.getApplication().getOrganizationId().equals(apporg)
                                && avbVersion.equals(appv)) {
                            contextVersion = avbVersion;
                        }
                    }
                    applicationVersion.setOptions(versions);
                    applicationVersion.setValue(contextVersion );
                    onApplicationVersionSelected();
                    showRow(APP_VERSION_ROW);
                    hideRow(SPINNER_ROW);
                }
                @Override
                public void onError(Throwable error) {
                    dataPacketError(error);
                }
            });
        }
    }

    /**
     * Called when the user selects an application version.
     */
    protected void onApplicationVersionSelected() {
        if (svc != null) {
            this.searchBox.setValue(svc);
            onSearch(null);
        }
        showRow(SERVICE_ROW);
    }

    /**
     * Called when the user selects a service.
     */
    protected void onServiceSelected() {
        hideRows(SERVICE_VERSION_ROW, PLAN_ROW);
        showRow(SPINNER_ROW);
        ServiceSummaryBean service = services.getValue();
        rest.getServiceVersions(service.getOrganizationId(), service.getId(), new IRestInvokerCallback<List<ServiceVersionBean>>() {
            @Override
            public void onSuccess(List<ServiceVersionBean> response) {
                List<String> versions = new ArrayList<String>(response.size());
                String initialContextServiceVersion = null;
                for (ServiceVersionBean svb : response) {
                    String svbVersion = svb.getVersion();
                    versions.add(svbVersion);
                    if (svb.getService().getOrganizationId().equals(svcorg) && svb.getService().getId().equals(svc) && svbVersion.equals(svcv)) {
                        initialContextServiceVersion = svbVersion;
                    }
                }
                serviceVersion.setOptions(versions);
                serviceVersion.setValue(initialContextServiceVersion);
                onServiceVersionSelected();
                showRow(SERVICE_VERSION_ROW);
                hideRow(SPINNER_ROW);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user selects a service version.
     */
    protected void onServiceVersionSelected() {
        hideRow(PLAN_ROW);
        showRow(SPINNER_ROW);
        ServiceSummaryBean service = services.getValue();
        String version = serviceVersion.getValue();
        rest.getServiceVersionPlans(service.getOrganizationId(), service.getId(), version, new IRestInvokerCallback<List<ServicePlanSummaryBean>>() {
            @Override
            public void onSuccess(List<ServicePlanSummaryBean> response) {
                plan.setOptions(response);
                if (response == null || response.isEmpty()) {
                } else {
                    plan.setValue(null);
                    onPlanSelected();
                }
                hideRow(SPINNER_ROW);
                showRow(PLAN_ROW);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user selects a plan.
     */
    protected void onPlanSelected() {
        createButton.setEnabled(true);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        rest.getCurrentUserApps(new IRestInvokerCallback<List<ApplicationSummaryBean>>() {
            @Override
            public void onSuccess(List<ApplicationSummaryBean> response) {
                applicationBeans = response;
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        return rval + 1;
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        hideRows(APP_VERSION_ROW, SERVICE_ROW, SERVICE_VERSION_ROW, PLAN_ROW, SPINNER_ROW);
        createButton.reset();
        createButton.setEnabled(false);
        applicationSelector.setOptions(applicationBeans);
        ApplicationSummaryBean contextApp = getInitialContextApp();
        applicationSelector.setValue(contextApp);
        
        onApplicationSelected();
    }
    
    /**
     * Gets the application that should be initially selected, based on the 
     * current page state.  The initial application to select may or may not
     * be provided in the page state (depending on how the user got to this 
     * page).
     * @return the initial application to select based on the page state
     */
    private ApplicationSummaryBean getInitialContextApp() {
        for (ApplicationSummaryBean appBean : applicationBeans) {
            if (appBean.getOrganizationId().equals(apporg) && appBean.getId().equals(app)) {
                return appBean;
            }
        }
        return null;
    }

    /**
     * Called once the page is shown.
     */
    @PageShown
    protected void onPageShown() {
        applicationSelector.setFocus(true);
    }

    /**
     * Called when the user clicks the Search button to find services.
     * @param event
     */
    @EventHandler("searchButton")
    public void onSearch(ClickEvent event) {
        if (searchBox.getValue().trim().length() == 0)
            return;

        searchBox.setEnabled(false);
        searchButton.onActionStarted();
        services.clear();
        hideRows(SERVICE_VERSION_ROW, PLAN_ROW, SPINNER_ROW);
        
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        criteria.setPageSize(50);
        criteria.setPage(1);
        criteria.setOrder("name", true); //$NON-NLS-1$
        criteria.addFilter("name", "*" + searchBox.getValue() + "*", SearchCriteriaFilterBean.OPERATOR_LIKE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        rest.findServices(criteria, new IRestInvokerCallback<SearchResultsBean<ServiceSummaryBean>>() {
            @Override
            public void onSuccess(SearchResultsBean<ServiceSummaryBean> response) {
                List<ServiceSummaryBean> svcBeans = response.getBeans();
                services.setServices(svcBeans);
                ServiceSummaryBean initialContextService = null;
                if (svcorg != null && svc != null) {
                    for (ServiceSummaryBean serviceBean : svcBeans) {
                        if (serviceBean.getOrganizationId().equals(svcorg) && serviceBean.getId().equals(svc)) {
                            initialContextService = serviceBean;
                            break;
                        }
                    }
                }
                services.setValue(initialContextService);
                if (initialContextService != null) {
                    onServiceSelected();
                }
                searchBox.setEnabled(true);
                searchButton.onActionComplete();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * Called when the user clicks the Create Organization button.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        createButton.onActionStarted();
        cancelButton.setEnabled(false);
        ApplicationSummaryBean app = applicationSelector.getValue();
        String appVersion = applicationVersion.getValue();
        ServiceSummaryBean service = services.getValue();
        String svcVersion = serviceVersion.getValue();
        ServicePlanSummaryBean planSummary = plan.getValue();
        
        final String orgId = app.getOrganizationId();
        final String appId = app.getId();
        final String version = appVersion;
        NewContractBean bean = new NewContractBean();
        bean.setServiceOrgId(service.getOrganizationId());
        bean.setServiceId(service.getId());
        bean.setServiceVersion(svcVersion);
        bean.setPlanId(planSummary.getPlanId());
        rest.createContract(orgId, appId, version, bean, new IRestInvokerCallback<ContractBean>() {
            @Override
            public void onSuccess(ContractBean response) {
                toApp.go(MultimapUtil.fromMultiple("org", orgId, "app", appId, "version", version)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_NEW_CONTRACT);
    }

    /**
     * Hides the given rows.
     * @param rows
     */
    private void hideRows(String ... rows) {
        for (String row : rows) {
            hideRow(row);
        }
    }
    
    /**
     * Shows the given rows.
     * @param rows
     */
    private void showRows(String ... rows) {
        for (String row : rows) {
            showRow(row);
        }
    }
    
    /**
     * Hides a single row.
     * @param row
     */
    private native void hideRow(String row) /*-{
      $wnd.jQuery('.apiman-new-contract .' + row).addClass('hide');
    }-*/;

    /**
     * Shows a single row.
     * @param row
     */
    private native void showRow(String row) /*-{
      $wnd.jQuery('.apiman-new-contract .' + row).removeClass('hide');
    }-*/;

}
