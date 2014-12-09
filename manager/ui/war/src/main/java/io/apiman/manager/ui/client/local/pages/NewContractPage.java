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

import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.contracts.NewContractBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.ServicePlanSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.ui.client.local.AppMessages;
import io.apiman.manager.ui.client.local.pages.common.VersionSelectBox;
import io.apiman.manager.ui.client.local.pages.contract.ApplicationSelectBox;
import io.apiman.manager.ui.client.local.pages.contract.PlanSelectBox;
import io.apiman.manager.ui.client.local.pages.contract.ServiceSelector;
import io.apiman.manager.ui.client.local.services.ContextKeys;
import io.apiman.manager.ui.client.local.services.rest.IRestInvokerCallback;
import io.apiman.manager.ui.client.local.util.MultimapUtil;

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
@Templated("/io/apiman/manager/ui/client/local/site/new-contract.html#page")
@Page(path="new-contract")
@Dependent
public class NewContractPage extends AbstractPage {

    private static final String APP_ROW = "application-row"; //$NON-NLS-1$
    private static final String APP_VERSION_ROW = "application-version-row"; //$NON-NLS-1$
    private static final String SERVICE_SEARCH_ROW = "service-search-row"; //$NON-NLS-1$
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
    String planid;
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
    Anchor createButton;
    @Inject @DataField
    Anchor cancelButton;
    @Inject @DataField
    AsyncActionButton agreeButton;
    
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
        hideRows(APP_VERSION_ROW, SERVICE_SEARCH_ROW, SERVICE_ROW, SERVICE_VERSION_ROW, PLAN_ROW);
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
                        if (avb.getApplication().getOrganization().getId().equals(apporg)
                                && avb.getApplication().getOrganization().getId().equals(apporg)
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
            getSpecificService();
        } else {
            showRows(SERVICE_SEARCH_ROW, SERVICE_ROW);
        }
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
                    if (svb.getService().getOrganization().getId().equals(svcorg) && svb.getService().getId().equals(svc) && svbVersion.equals(svcv)) {
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
                    ServicePlanSummaryBean selected = null;
                    if (planid != null) {
                        for (ServicePlanSummaryBean pb : response) {
                            if (pb.getPlanId().equals(planid)) {
                                selected = pb;
                                break;
                            }
                        }
                    }
                    plan.setValue(selected);
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        hideRows(APP_VERSION_ROW, SERVICE_SEARCH_ROW, SERVICE_ROW, SERVICE_VERSION_ROW, PLAN_ROW, SPINNER_ROW);
        agreeButton.reset();
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
        if (app == null) {
            ApplicationVersionBean ctxapp = (ApplicationVersionBean) currentContext.getAttribute(ContextKeys.CURRENT_APPLICATION_VERSION);
            if (ctxapp != null) {
                for (ApplicationSummaryBean appBean : applicationBeans) {
                    if (appBean.getOrganizationId().equals(ctxapp.getApplication().getOrganization().getId())
                            && appBean.getId().equals(ctxapp.getApplication().getId())) {
                        return appBean;
                    }
                }
            }
        } else {
            for (ApplicationSummaryBean appBean : applicationBeans) {
                if (appBean.getOrganizationId().equals(apporg) && appBean.getId().equals(app)) {
                    return appBean;
                }
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
     * Gets a specific service rather than searching for one.  This happens when
     * the new-contract page has both a svc and svcorg param.
     */
    protected void getSpecificService() {
        services.clear();
        hideRows(SERVICE_VERSION_ROW, PLAN_ROW, SPINNER_ROW);
        
        rest.getService(svcorg, svc, new IRestInvokerCallback<ServiceBean>() {
            @Override
            public void onSuccess(ServiceBean response) {
                ServiceSummaryBean summary = new ServiceSummaryBean();
                summary.setId(response.getId());
                summary.setDescription(response.getDescription());
                summary.setName(response.getName());
                summary.setCreatedOn(response.getCreatedOn());
                summary.setOrganizationId(response.getOrganization().getId());
                summary.setOrganizationName(response.getOrganization().getId());
                List<ServiceSummaryBean> svcBeans = new ArrayList<ServiceSummaryBean>();
                svcBeans.add(summary);
                services.setServices(svcBeans);
                services.setValue(summary);
                searchBox.setValue(summary.getName());
                showRows(SERVICE_SEARCH_ROW, SERVICE_ROW);
                onServiceSelected();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
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
     * Called when the user clicks the Create Contract button.  This will hide the form
     * and show the Terms & Conditions screen - the user must agree to them.
     * @param event
     */
    @EventHandler("createButton")
    public void onCreate(ClickEvent event) {
        showTermsAndConditions();
    }

    /**
     * Native helper method for showing the Terms & Conditions with a bit of an animation.
     */
    private native void showTermsAndConditions() /*-{
        $wnd.jQuery("#new-contract-form").animate( { "margin-left" : "-1000px" }, 250, function() { 
            $wnd.jQuery("#new-contract-form").hide();
            $wnd.jQuery("#terms-conditions").show();
            $wnd.jQuery("#terms-conditions").animate( { "margin-left" : "0px" }, 250);
        });
    }-*/;

    /**
     * Called when the user clicks the I Agree button.
     * @param event
     */
    @EventHandler("agreeButton")
    public void onAgree(ClickEvent event) {
        agreeButton.onActionStarted();
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
     * @see io.apiman.manager.ui.client.local.pages.AbstractPage#getPageTitle()
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
