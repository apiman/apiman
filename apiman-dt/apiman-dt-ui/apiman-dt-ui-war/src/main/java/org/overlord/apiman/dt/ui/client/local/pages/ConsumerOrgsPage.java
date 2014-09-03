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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.orgs.OrganizationBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaBean;
import org.overlord.apiman.dt.api.beans.search.SearchCriteriaFilterBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.api.beans.summary.OrganizationSummaryBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.pages.common.Breadcrumb;
import org.overlord.apiman.dt.ui.client.local.pages.consumer.OrganizationList;
import org.overlord.apiman.dt.ui.client.local.services.ConfigurationService;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;
import org.overlord.apiman.dt.ui.client.local.util.MultimapUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;


/**
 * The "Browse Organizations" page - part of the consumer UI.  This page
 * allows users to search for and find Organizations.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/consumer-orgs.html#page")
@Page(path="corgs")
@Dependent
public class ConsumerOrgsPage extends AbstractPage {
    
    @PageState
    protected String query;

    @Inject @DataField
    Breadcrumb breadcrumb;

    @Inject @DataField
    private TextBox searchBox;
    @Inject @DataField
    private Button searchButton;
    @Inject @DataField
    private OrganizationList orgs;
    
    @Inject
    ConfigurationService config;
    @Inject
    TransitionTo<ConsumerOrgsPage> toSelf;

    protected List<OrganizationBean> orgBeans;
    protected Set<String> memberOrgs = new HashSet<String>();

    /**
     * Constructor.
     */
    public ConsumerOrgsPage() {
    }
    
    @PostConstruct
    protected void postConstruct() {
        searchBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                toSelf.go(MultimapUtil.singleItemMap("query", searchBox.getValue())); //$NON-NLS-1$
            }
        });
        searchButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toSelf.go(MultimapUtil.singleItemMap("query", searchBox.getValue())); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#loadPageData()
     */
    @Override
    protected int loadPageData() {
        int rval = super.loadPageData();
        if (query != null && !query.trim().isEmpty()) {
            doQuery(query);
            rval+=2;
        } else {
            orgBeans = new ArrayList<OrganizationBean>();
        }
        return rval;
    }

    /**
     * Invoke the rest service to perform the query and get back the data.
     * @param query
     */
    private void doQuery(String query) {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        criteria.setPageSize(100);
        criteria.setPage(1);
        criteria.setOrder("name", true); //$NON-NLS-1$
        criteria.addFilter("name", "*" + query + "*", SearchCriteriaFilterBean.OPERATOR_LIKE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        rest.findOrganizations(criteria, new IRestInvokerCallback<SearchResultsBean<OrganizationBean>>() {
            @Override
            public void onSuccess(SearchResultsBean<OrganizationBean> response) {
                orgBeans = response.getBeans();
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
        rest.getCurrentUserOrgs(new IRestInvokerCallback<List<OrganizationSummaryBean>>() {
            @Override
            public void onSuccess(List<OrganizationSummaryBean> response) {
                memberOrgs.clear();
                for (OrganizationSummaryBean org : response) {
                    memberOrgs.add(org.getId());
                }
                dataPacketLoaded();
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });

    }
    
    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#renderPage()
     */
    @Override
    protected void renderPage() {
        if (query != null) {
            searchBox.setValue(query);
        } else {
            searchBox.setValue(""); //$NON-NLS-1$
        }
        orgs.setMemberOrgs(memberOrgs);
        orgs.setValue(orgBeans);

        String dashHref = navHelper.createHrefToPage(DashboardPage.class, MultimapUtil.fromMultiple());
        breadcrumb.addItem(dashHref, "home", i18n.format(AppMessages.HOME)); //$NON-NLS-1$
        breadcrumb.addActiveItem("search", i18n.format(AppMessages.ORGANIZATIONS)); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_CONSUME_ORGS);
    }

}
