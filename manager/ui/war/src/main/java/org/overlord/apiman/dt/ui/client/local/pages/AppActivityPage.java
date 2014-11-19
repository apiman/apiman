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

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.apiman.dt.api.beans.audit.AuditEntryBean;
import org.overlord.apiman.dt.api.beans.search.SearchResultsBean;
import org.overlord.apiman.dt.ui.client.local.AppMessages;
import org.overlord.apiman.dt.ui.client.local.events.MoreActivityItemsEvent;
import org.overlord.apiman.dt.ui.client.local.events.MoreActivityItemsEvent.Handler;
import org.overlord.apiman.dt.ui.client.local.pages.common.ActivityList;
import org.overlord.apiman.dt.ui.client.local.services.rest.IRestInvokerCallback;


/**
 * The "Application" page, with the Activity tab displayed.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/apiman/dt/ui/client/local/site/app-activity.html#page")
@Page(path="app-activity")
@Dependent
public class AppActivityPage extends AbstractAppPage {

    private static final int PAGE_SIZE = 10;
    
    @Inject @DataField
    private ActivityList activity;
    
    private SearchResultsBean<AuditEntryBean> activityData;
    private int page = 1;
    
    /**
     * Constructor.
     */
    public AppActivityPage() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    protected void postConstruct() {
        activity.addMoreActivityItemsHandler(new Handler() {
            @Override
            public void onMoreActivityItems(MoreActivityItemsEvent event) {
                onNextActivityItems();
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractOrgPage#doLoadPageData()
     */
    @Override
    protected int doLoadPageData() {
        int rval = super.doLoadPageData();
        rest.getApplicationActivity(org, app, 1, PAGE_SIZE, new IRestInvokerCallback<SearchResultsBean<AuditEntryBean>>() {
            @Override
            public void onSuccess(SearchResultsBean<AuditEntryBean> response) {
                activityData = response;
                page = 1;
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
     * Called when the user clicks the "Next Items" button (only visible when there are 
     * more activity items to load).
     */
    protected void onNextActivityItems() {
        page++;
        rest.getApplicationActivity(org, app, page, PAGE_SIZE, new IRestInvokerCallback<SearchResultsBean<AuditEntryBean>>() {
            @Override
            public void onSuccess(SearchResultsBean<AuditEntryBean> response) {
                activityData.getBeans().addAll(response.getBeans());
                activity.appendValue(response);
            }
            @Override
            public void onError(Throwable error) {
                dataPacketError(error);
            }
        });
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractUserPage#renderPage()
     */
    @Override
    protected void renderPage() {
        super.renderPage();
        activity.setValue(activityData);
    }

    /**
     * @see org.overlord.apiman.dt.ui.client.local.pages.AbstractPage#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return i18n.format(AppMessages.TITLE_APP_ACTIVITY, applicationBean.getName());
    }

}
